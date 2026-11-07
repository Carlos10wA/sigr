package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.dto.AperturaCajaForm;
import co.edu.tecnologicaoriente.sigr.dto.CierreCajaForm;
import co.edu.tecnologicaoriente.sigr.dto.PagoForm;
import co.edu.tecnologicaoriente.sigr.model.*;
import co.edu.tecnologicaoriente.sigr.repository.ComprobanteRepository;
import co.edu.tecnologicaoriente.sigr.repository.ConfiguracionRestauranteRepository;
import co.edu.tecnologicaoriente.sigr.repository.PagoRepository;
import co.edu.tecnologicaoriente.sigr.repository.SesionCajaRepository;
import co.edu.tecnologicaoriente.sigr.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class CajaService {

    public static class CajaInvalidaException extends RuntimeException {
        public CajaInvalidaException(String m) { super(m); }
    }

    public static class PagoDuplicadoException extends RuntimeException {
        public PagoDuplicadoException(String m) { super(m); }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Caja no encontrada")
    public static class CajaNoEncontradaException extends RuntimeException {
        public CajaNoEncontradaException(String m) { super(m); }
    }

    private final SesionCajaRepository sesiones;
    private final PagoRepository pagos;
    private final ComprobanteRepository comprobantes;
    private final UsuarioRepository usuarios;
    private final ConfiguracionRestauranteRepository configuracion;
    private final Clock clock;

    public CajaService(SesionCajaRepository sesiones, PagoRepository pagos,
                       ComprobanteRepository comprobantes, UsuarioRepository usuarios,
                       ConfiguracionRestauranteRepository configuracion, Clock clock) {
        this.sesiones = sesiones; this.pagos = pagos; this.comprobantes = comprobantes;
        this.usuarios = usuarios; this.configuracion = configuracion; this.clock = clock;
    }

    public Usuario resolverActual(String username) {
        return usuarios.findByUsuario(username)
                .orElseThrow(() -> new CajaNoEncontradaException("Usuario no encontrado."));
    }

    private ZoneId zona() {
        var cfg = configuracion.findById(1L).orElse(new ConfiguracionRestaurante());
        try { return ZoneId.of(cfg.getZonaHoraria()); }
        catch (Exception e) { return ZoneId.of(ConfiguracionRestaurante.ZONA_POR_DEFECTO); }
    }

    private LocalDateTime ahora() {
        return java.time.ZonedDateTime.now(clock).withZoneSameInstant(zona()).toLocalDateTime().withNano(0);
    }

    @Transactional(readOnly = true)
    public Optional<SesionCaja> abierta() {
        return sesiones.findByEstado(EstadoSesionCaja.ABIERTA);
    }

    @Transactional(readOnly = true)
    public List<SesionCaja> listarSesiones() {
        return sesiones.findAllByOrderByFechaAperturaDesc();
    }

    @Transactional
    public SesionCaja abrir(Usuario responsable, AperturaCajaForm form) {
        BigDecimal base = form.getBaseInicial() == null ? null : form.getBaseInicial().setScale(2, java.math.RoundingMode.HALF_UP);
        if (base == null || base.signum() < 0) {
            throw new CajaInvalidaException("La base inicial no puede ser negativa.");
        }
        // Bloqueo de configuración como mutex de BD: serializa aperturas simultáneas.
        configuracion.findById(1L).orElseGet(() -> configuracion.save(new ConfiguracionRestaurante()));
        synchronized (this) {
            if (sesiones.findByEstado(EstadoSesionCaja.ABIERTA).isPresent()) {
                throw new CajaInvalidaException("Ya hay una sesión de caja abierta.");
            }
            SesionCaja s = new SesionCaja();
            s.setResponsable(responsable);
            s.setFechaApertura(ahora());
            s.setZonaHoraria(zona().getId());
            s.setBaseInicial(base);
            s.setEstado(EstadoSesionCaja.ABIERTA);
            return sesiones.save(s);
        }
    }

    @Transactional
    public Pago registrarPago(Usuario responsable, PagoForm form) {
        MetodoPago metodo = MetodoPago.valueOf(form.getMetodo());
        // Bloqueo de sesión: ningún pago entra en sesión cerrada (cierre también bloquea).
        SesionCaja sesion = sesiones.findByEstado(EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new CajaInvalidaException("No hay caja abierta."));
        sesion = sesiones.findByIdForUpdate(sesion.getId()).orElseThrow();
        if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
            throw new CajaInvalidaException("La sesión de caja está cerrada.");
        }
        Comprobante comp = comprobantes.findById(form.getComprobanteId())
                .orElseThrow(() -> new CajaInvalidaException("El comprobante no existe."));
        if (comp.getEstado() != EstadoPago.PENDIENTE_DE_PAGO) {
            throw new PagoDuplicadoException("El comprobante ya está pagado.");
        }
        if (pagos.existsByComprobante_Id(comp.getId())) {
            throw new PagoDuplicadoException("El comprobante ya tiene un pago registrado.");
        }
        BigDecimal importe = comp.getTotal().setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal recibido = null;
        BigDecimal cambio = null;
        String referencia = form.getReferencia() == null ? "" : form.getReferencia().trim();
        if (metodo == MetodoPago.EFECTIVO) {
            if (form.getValorRecibido() == null || form.getValorRecibido().compareTo(importe) < 0) {
                throw new CajaInvalidaException("El valor recibido debe ser mayor o igual al total ($" + importe.toPlainString() + " COP).");
            }
            recibido = form.getValorRecibido().setScale(2, java.math.RoundingMode.HALF_UP);
            cambio = recibido.subtract(importe).setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            if (referencia.isBlank()) {
                throw new CajaInvalidaException("La referencia es obligatoria para tarjeta y transferencia.");
            }
        }
        Pago p = new Pago();
        p.setComprobante(comp);
        p.setSesion(sesion);
        p.setMetodo(metodo);
        p.setImporte(importe); // ingreso = total cobrado, no el recibido antes del cambio
        p.setValorRecibido(recibido);
        p.setCambio(cambio);
        p.setReferencia(referencia.isBlank() ? null : referencia);
        p.setResponsable(responsable);
        p.setFechaHora(ahora());
        try {
            p = pagos.saveAndFlush(p);
        } catch (DataIntegrityViolationException e) {
            throw new PagoDuplicadoException("El comprobante ya tiene un pago registrado.");
        }
        comp.setEstado(EstadoPago.PAGADO);
        comp.setPago(p);
        comprobantes.save(comp);
        return p;
    }

    @Transactional
    public SesionCaja cerrar(Usuario responsable, CierreCajaForm form) {
        SesionCaja sesion = sesiones.findByEstado(EstadoSesionCaja.ABIERTA)
                .orElseThrow(() -> new CajaInvalidaException("No hay caja abierta."));
        sesion = sesiones.findByIdForUpdate(sesion.getId()).orElseThrow();
        if (sesion.getEstado() != EstadoSesionCaja.ABIERTA) {
            throw new CajaInvalidaException("La sesión ya está cerrada.");
        }
        BigDecimal efectivoContado = form.getEfectivoContado() == null ? null
                : form.getEfectivoContado().setScale(2, java.math.RoundingMode.HALF_UP);
        if (efectivoContado == null || efectivoContado.signum() < 0) {
            throw new CajaInvalidaException("El efectivo contado no puede ser negativo.");
        }
        List<Pago> lista = pagos.findBySesion_Id(sesion.getId());
        BigDecimal ef = BigDecimal.ZERO, tar = BigDecimal.ZERO, tra = BigDecimal.ZERO;
        for (Pago p : lista) {
            switch (p.getMetodo()) {
                case EFECTIVO -> ef = ef.add(p.getImporte());
                case TARJETA -> tar = tar.add(p.getImporte());
                case TRANSFERENCIA -> tra = tra.add(p.getImporte());
            }
        }
        BigDecimal esperado = sesion.getBaseInicial().add(ef).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal diferencia = efectivoContado.subtract(esperado).setScale(2, java.math.RoundingMode.HALF_UP);
        String obs = form.getObservacion() == null ? "" : form.getObservacion().trim();
        if (diferencia.signum() != 0 && obs.isBlank()) {
            throw new CajaInvalidaException("Con diferencia se exige observación.");
        }
        sesion.setEstado(EstadoSesionCaja.CERRADA);
        sesion.setFechaCierre(ahora());
        sesion.setResponsableCierre(responsable.getNombreCompleto());
        sesion.setZonaCierre(zona().getId());
        sesion.setCobrosEfectivo(ef.setScale(2, java.math.RoundingMode.HALF_UP));
        sesion.setCobrosTarjeta(tar.setScale(2, java.math.RoundingMode.HALF_UP));
        sesion.setCobrosTransferencia(tra.setScale(2, java.math.RoundingMode.HALF_UP));
        sesion.setTotalCobrado(ef.add(tar).add(tra).setScale(2, java.math.RoundingMode.HALF_UP));
        sesion.setEfectivoEsperado(esperado);
        sesion.setEfectivoContado(efectivoContado);
        sesion.setDiferencia(diferencia);
        sesion.setObservacion(obs.isBlank() ? null : obs);
        return sesiones.save(sesion);
    }
}
