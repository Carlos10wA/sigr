package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.model.*;
import co.edu.tecnologicaoriente.sigr.repository.ComprobanteRepository;
import co.edu.tecnologicaoriente.sigr.repository.PedidoRepository;
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

@Service
public class FacturacionService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    public static class FacturacionNoPermitidaException extends RuntimeException {
        public FacturacionNoPermitidaException(String m) { super(m); }
    }

    public static class FacturacionInvalidaException extends RuntimeException {
        public FacturacionInvalidaException(String m) { super(m); }
    }

    public static class YaFacturadoException extends RuntimeException {
        private final Long comprobanteId;
        public YaFacturadoException(String m, Long comprobanteId) {
            super(m); this.comprobanteId = comprobanteId;
        }
        public Long getComprobanteId() { return comprobanteId; }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Comprobante no encontrado")
    public static class ComprobanteNoEncontradoException extends RuntimeException {
        public ComprobanteNoEncontradoException(String m) { super(m); }
    }

    private final ComprobanteRepository comprobantes;
    private final PedidoRepository pedidos;
    private final UsuarioRepository usuarios;
    private final Clock clock;
    private final ConfiguracionService configuracion;

    public FacturacionService(ComprobanteRepository comprobantes, PedidoRepository pedidos,
                              UsuarioRepository usuarios, Clock clock, ConfiguracionService configuracion) {
        this.comprobantes = comprobantes; this.pedidos = pedidos; this.usuarios = usuarios; this.clock = clock;
        this.configuracion = configuracion;
    }

    public Usuario resolverActual(String username) {
        return usuarios.findByUsuario(username)
                .orElseThrow(() -> new ComprobanteNoEncontradoException("Usuario no encontrado."));
    }

    @Transactional
    public Comprobante emitir(Usuario actor, Long pedidoId) {
        if (actor.getRol() == Rol.CLIENTE) {
            throw new FacturacionNoPermitidaException("Solo el personal del restaurante puede emitir comprobantes.");
        }
        // Bloqueo pesimista del pedido: impide doble emisión simultánea (doble clic).
        Pedido pedido = pedidos.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new FacturacionInvalidaException("El pedido no existe."));
        if (pedido.getEstado() != EstadoPedido.ENTREGADO) {
            throw new FacturacionInvalidaException(
                    "Solo se puede facturar un pedido ENTREGADO (actual: " + pedido.getEstado().nombreES() + ").");
        }
        var existente = comprobantes.findByPedido_Id(pedidoId);
        if (existente.isPresent()) {
            throw new YaFacturadoException("Este pedido ya tiene un comprobante.", existente.get().getId());
        }
        Comprobante c = new Comprobante();
        c.setPedido(pedido);
        c.setClienteNombre(pedido.getUsuario().getNombreCompleto());
        c.setClienteUsuario(pedido.getUsuario().getUsuario());
        var cfg = configuracion.actual();
        c.setPais(cfg.getPais());
        c.setCiudad(cfg.getCiudad());
        c.setDireccion(cfg.getDireccion());
        c.setZonaHoraria(cfg.getZonaHoraria());
        ZoneId zona = configuracion.zonaActual();
        c.setFechaEmision(java.time.ZonedDateTime.now(clock).withZoneSameInstant(zona).toLocalDateTime().withNano(0));
        // Copia histórica desde el pedido; total calculado en el servidor, sin impuestos/propinas/descuentos.
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido d : pedido.getDetalles()) {
            var linea = new ComprobanteLinea(c, d.getNombrePlato(), d.getPrecioUnitario(), d.getCantidad());
            c.getLineas().add(linea);
            total = total.add(linea.getSubtotal());
        }
        if (c.getLineas().isEmpty()) {
            throw new FacturacionInvalidaException("El pedido no tiene líneas para facturar.");
        }
        c.setTotal(total.setScale(2, java.math.RoundingMode.HALF_UP));
        // Verificación server-side contra el total del pedido (no confía en el navegador).
        if (pedido.getTotal() != null && pedido.getTotal().compareTo(c.getTotal()) != 0) {
            throw new FacturacionInvalidaException("El total calculado no coincide con el total del pedido.");
        }
        c.setEstado(EstadoPago.PENDIENTE_DE_PAGO);
        try {
            c = comprobantes.saveAndFlush(c);
        } catch (DataIntegrityViolationException e) {
            // Restricción única pedido_id: otro hilo ganó.
            var ganador = comprobantes.findByPedido_Id(pedidoId);
            if (ganador.isPresent()) {
                throw new YaFacturadoException("Este pedido ya tiene un comprobante.", ganador.get().getId());
            }
            throw e;
        }
        c.setNumero(String.format("FAC-%06d", c.getId()));
        return comprobantes.save(c);
    }

    @Transactional(readOnly = true)
    public List<Comprobante> listarPara(Usuario actor) {
        if (actor.getRol() == Rol.CLIENTE) {
            return comprobantes.findByPedido_Usuario_IdOrderByFechaEmisionDesc(actor.getId());
        }
        return comprobantes.findAllByOrderByFechaEmisionDesc();
    }

    @Transactional(readOnly = true)
    public Comprobante buscarPara(Usuario actor, Long id) {
        Comprobante c = comprobantes.findById(id)
                .orElseThrow(() -> new ComprobanteNoEncontradoException("Comprobante no encontrado."));
        if (actor.getRol() == Rol.CLIENTE && !c.getPedido().getUsuario().getId().equals(actor.getId())) {
            throw new ComprobanteNoEncontradoException("Comprobante no encontrado.");
        }
        return c;
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Comprobante> porPedido(Long pedidoId) {
        return comprobantes.findByPedido_Id(pedidoId);
    }
}
