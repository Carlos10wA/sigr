package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.Comprobante;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    @EntityGraph(attributePaths = {"pedido", "pedido.usuario", "lineas", "pago", "pago.sesion", "pago.responsable"})
    Optional<Comprobante> findById(Long id);

    @EntityGraph(attributePaths = {"pedido", "pedido.usuario", "lineas", "pago"})
    List<Comprobante> findAllByOrderByFechaEmisionDesc();

    @EntityGraph(attributePaths = {"pedido", "pedido.usuario", "lineas", "pago"})
    List<Comprobante> findByPedido_Usuario_IdOrderByFechaEmisionDesc(Long usuarioId);

    Optional<Comprobante> findByPedido_Id(Long pedidoId);

    boolean existsByPedido_Id(Long pedidoId);
}
