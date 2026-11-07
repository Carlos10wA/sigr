package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.model.*;
import co.edu.tecnologicaoriente.sigr.repository.ComprobanteRepository;
import co.edu.tecnologicaoriente.sigr.repository.ConfiguracionRestauranteRepository;
import co.edu.tecnologicaoriente.sigr.repository.PagoRepository;
import co.edu.tecnologicaoriente.sigr.repository.ReservaRepository;
import co.edu.tecnologicaoriente.sigr.repository.SesionCajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
public class ReporteService {

    public static class RangoInvalidoException extends RuntimeException {
        public RangoInvalidoException(String m) { super(m); }
    }

    public record Rango(LocalDate desde, LocalDate hasta, ZoneId zona) {}

    public record ResumenCobros(BigDecimal total, long cantidad, BigDecimal efectivo, BigDecimal tarjeta, BigDecimal transferencia, List<Pago> detalle) {}
    public record PlatoAgregado(String nombre, long cantidad, BigDecimal importe) {}
    public record ResumenReservas(long confirmadas, long manuales, long vencimiento, long sinMotivo, List<Reserva> detalle) {}

    private final PagoRepository pagos;
    private final ReservaRepository reservas;
    private final SesionCajaRepository sesiones;
    private final ComprobanteRepository comprobantes;
    private final ConfiguracionRestauranteRepository configuracion;

    public ReporteService(PagoRepository pagos, ReservaRepository reservas, SesionCajaRepository sesiones,
                          ComprobanteRepository comprobantes, ConfiguracionRestauranteRepository configuracion) {
        this.pagos = pagos; this.reservas = reservas; this.sesiones = sesiones;
        this.comprobantes = comprobantes; this.configuracion = configuracion;
    }

    public ZoneId zona() {
        var cfg = configuracion.findById(1L).orElse(new ConfiguracionRestaurante());
        try { return ZoneId.of(cfg.getZonaHoraria()); }
        catch (Exception e) { return ZoneId.of(ConfiguracionRestaurante.ZONA_POR_DEFECTO); }
    }

    public ZoneId zonaActual() { return zona(); }

    public Rango validar(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new RangoInvalidoException("Seleccione fecha inicial y fecha final.");
        }
        if (hasta.isBefore(desde)) {
            throw new RangoInvalidoException("La fecha final no puede ser anterior a la inicial.");
        }
        return new Rango(desde, hasta, zona());
    }

    private void rangoPago(LocalDate desde, LocalDate hasta) {}

    @Transactional(readOnly = true)
    public ResumenCobros cobros(LocalDate desde, LocalDate hasta) {
        var r = validar(desde, hasta);
        LocalDateTime ini = r.desde().atStartOfDay();
        LocalDateTime finExcl = r.hasta().plusDays(1).atStartOfDay();
        List<Pago> todos = pagos.findAll();
        List<Pago> enRango = todos.stream()
                .filter(p -> !p.getFechaHora().isBefore(ini) && p.getFechaHora().isBefore(finExcl))
                .toList();
        BigDecimal total = BigDecimal.ZERO, ef = BigDecimal.ZERO, tar = BigDecimal.ZERO, tra = BigDecimal.ZERO;
        for (Pago p : enRango) {
            total = total.add(p.getImporte());
            switch (p.getMetodo()) {
                case EFECTIVO -> ef = ef.add(p.getImporte());
                case TARJETA -> tar = tar.add(p.getImporte());
                case TRANSFERENCIA -> tra = tra.add(p.getImporte());
            }
        }
        enRango.forEach(p -> { p.getComprobante().getPedido().getId(); });
        return new ResumenCobros(total.setScale(2), enRango.size(), ef.setScale(2), tar.setScale(2), tra.setScale(2), enRango);
    }

    @Transactional(readOnly = true)
    public List<PlatoAgregado> platosVendidos(LocalDate desde, LocalDate hasta) {
        var cobros = cobros(desde, hasta);
        Map<String, long[]> cant = new HashMap<>();
        Map<String, BigDecimal> imp = new HashMap<>();
        for (Pago p : cobros.detalle()) {
            for (ComprobanteLinea l : p.getComprobante().getLineas()) {
                cant.computeIfAbsent(l.getNombrePlato(), k -> new long[]{0});
                cant.get(l.getNombrePlato())[0] += l.getCantidad();
                imp.merge(l.getNombrePlato(), l.getSubtotal(), BigDecimal::add);
            }
        }
        List<PlatoAgregado> lista = new ArrayList<>();
        cant.forEach((n, v) -> lista.add(new PlatoAgregado(n, v[0], imp.get(n).setScale(2))));
        lista.sort(Comparator.comparingLong(PlatoAgregado::cantidad).reversed()
                .thenComparing(Comparator.comparing(PlatoAgregado::nombre)));
        return lista;
    }

    @Transactional(readOnly = true)
    public List<co.edu.tecnologicaoriente.sigr.model.Mesa> mesasDisponibles() {
        return sesiones.findAllByOrderByFechaAperturaDesc().isEmpty()
                ? List.of() : List.of();
    }

    @Transactional(readOnly = true)
    public ResumenReservas reservas(LocalDate desde, LocalDate hasta, Long mesaId) {
        var r = validar(desde, hasta);
        List<Reserva> todas = reservas.findAllByOrderByFechaAscHoraInicioAsc();
        List<Reserva> enRango = todas.stream()
                .filter(x -> !x.getFecha().isBefore(r.desde()) && !x.getFecha().isAfter(r.hasta()))
                .filter(x -> mesaId == null || x.getMesa().getId().equals(mesaId))
                .toList();
        long conf = enRango.stream().filter(x -> x.getEstado() == EstadoReserva.CONFIRMADA).count();
        long manuales = enRango.stream()
                .filter(x -> x.getEstado() == EstadoReserva.CANCELADA && x.getMotivoCancelacion() == MotivoCancelacion.MANUAL).count();
        long venc = enRango.stream()
                .filter(x -> x.getEstado() == EstadoReserva.CANCELADA && x.getMotivoCancelacion() == MotivoCancelacion.VENCIMIENTO).count();
        long sin = enRango.stream()
                .filter(x -> x.getEstado() == EstadoReserva.CANCELADA && x.getMotivoCancelacion() == null).count();
        return new ResumenReservas(conf, manuales, venc, sin, enRango);
    }

    @Transactional(readOnly = true)
    public List<SesionCaja> cierres(LocalDate desde, LocalDate hasta) {
        var r = validar(desde, hasta);
        return sesiones.findAllByOrderByFechaAperturaDesc().stream()
                .filter(s -> s.getEstado() == EstadoSesionCaja.CERRADA && s.getFechaCierre() != null)
                .filter(s -> !s.getFechaCierre().toLocalDate().isBefore(r.desde()) && !s.getFechaCierre().toLocalDate().isAfter(r.hasta()))
                .toList();
    }
}
