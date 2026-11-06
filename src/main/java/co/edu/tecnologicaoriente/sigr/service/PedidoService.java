package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.model.*;
import co.edu.tecnologicaoriente.sigr.repository.PedidoRepository;
import co.edu.tecnologicaoriente.sigr.repository.PlatoRepository;
import co.edu.tecnologicaoriente.sigr.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PedidoService {

    public static class PedidoInvalidoException extends RuntimeException {
        public PedidoInvalidoException(String m) { super(m); }
    }

    public static class TransicionInvalidaException extends RuntimeException {
        public TransicionInvalidaException(String m) { super(m); }
    }

    /** No encontrado o ajeno para CLIENTE (IDOR): 404 para no revelar existencia. */
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Pedido no encontrado")
    public static class PedidoNoEncontradoException extends RuntimeException {
        public PedidoNoEncontradoException(String m) { super(m); }
    }

    private static final Map<EstadoPedido, Set<EstadoPedido>> TRANSICIONES = Map.of(
            EstadoPedido.PENDIENTE, Set.of(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO),
            EstadoPedido.EN_PREPARACION, Set.of(EstadoPedido.LISTO, EstadoPedido.CANCELADO),
            EstadoPedido.LISTO, Set.of(EstadoPedido.ENTREGADO),
            EstadoPedido.ENTREGADO, Set.of(),
            EstadoPedido.CANCELADO, Set.of()
    );

    private final PedidoRepository pedidos;
    private final PlatoRepository platos;
    private final UsuarioRepository usuarios;

    public PedidoService(PedidoRepository pedidos, PlatoRepository platos, UsuarioRepository usuarios) {
        this.pedidos = pedidos;
        this.platos = platos;
        this.usuarios = usuarios;
    }

    public Usuario resolverActual(String username) {
        return usuarios.findByUsuario(username)
                .orElseThrow(() -> new PedidoNoEncontradoException("Usuario no encontrado."));
    }

    @Transactional
    public Pedido crear(Usuario solicitante, PedidoForm form) {
        if (form == null || form.getLineas() == null || form.getLineas().isEmpty()) {
            throw new PedidoInvalidoException("El pedido debe tener al menos una línea.");
        }
        Map<Long, Integer> porPlato = new LinkedHashMap<>();
        for (PedidoLineaForm linea : form.getLineas()) {
            if (linea.getPlatoId() == null) {
                continue; // fila vacía del formulario
            }
            Integer cant = linea.getCantidad();
            if (cant == null || cant < 1 || cant > 100) {
                throw new PedidoInvalidoException("La cantidad debe ser positiva (entre 1 y 100).");
            }
            Plato plato = platos.findById(linea.getPlatoId())
                    .orElseThrow(() -> new PedidoInvalidoException("Uno de los platos no existe."));
            if (!plato.isActivo()) {
                throw new PedidoInvalidoException("El plato «" + plato.getNombre() + "» está desactivado y no se puede pedir.");
            }
            if (!plato.isDisponible()) {
                throw new PedidoInvalidoException("El plato «" + plato.getNombre() + "» no está disponible temporalmente.");
            }
            porPlato.merge(linea.getPlatoId(), cant, Integer::sum);
        }
        if (porPlato.isEmpty()) {
            throw new PedidoInvalidoException("El pedido debe tener al menos un plato.");
        }
        Pedido pedido = new Pedido();
        pedido.setUsuario(solicitante);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> e : porPlato.entrySet()) {
            Plato plato = platos.findById(e.getKey()).orElseThrow();
            int cantidad = e.getValue();
            if (cantidad < 1 || cantidad > 100) {
                throw new PedidoInvalidoException("La cantidad total por plato debe estar entre 1 y 100.");
            }
            DetallePedido detalle = new DetallePedido(pedido, plato, plato.getNombre(), plato.getPrecio(), cantidad);
            pedido.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }
        pedido.setTotal(total.setScale(2, java.math.RoundingMode.HALF_UP));
        return pedidos.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPara(Usuario actor) {
        if (actor.getRol() == Rol.CLIENTE) {
            return pedidos.findByUsuario_IdOrderByFechaCreacionDesc(actor.getId());
        }
        return pedidos.findAllByOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public Pedido buscarPara(Usuario actor, Long id) {
        Pedido p = pedidos.findById(id)
                .orElseThrow(() -> new PedidoNoEncontradoException("Pedido no encontrado."));
        if (actor.getRol() == Rol.CLIENTE && !p.getUsuario().getId().equals(actor.getId())) {
            throw new PedidoNoEncontradoException("Pedido no encontrado.");
        }
        return p;
    }

    @Transactional
    public Pedido cambiarEstado(Usuario actor, Long id, EstadoPedido nuevo) {
        Pedido pedido = buscarPara(actor, id);
        // CLIENTE solo puede cancelar sus pedidos pendientes/en preparación.
        if (actor.getRol() == Rol.CLIENTE) {
            if (nuevo != EstadoPedido.CANCELADO) {
                throw new TransicionInvalidaException("Solo el personal del restaurante puede avanzar el estado del pedido.");
            }
            if (pedido.getEstado() == EstadoPedido.LISTO) {
                throw new TransicionInvalidaException("Un pedido listo ya no puede cancelarse.");
            }
        }
        Set<EstadoPedido> permitidos = TRANSICIONES.getOrDefault(pedido.getEstado(), Set.of());
        if (!permitidos.contains(nuevo)) {
            throw new TransicionInvalidaException(
                    "Transición no permitida: de " + pedido.getEstado().nombreES() + " a " + nuevo.nombreES() + ".");
        }
        pedido.setEstado(nuevo);
        return pedidos.save(pedido);
    }
}
