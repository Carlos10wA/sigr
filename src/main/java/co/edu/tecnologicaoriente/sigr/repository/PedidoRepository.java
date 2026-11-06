package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.plato"})
    Optional<Pedido> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.plato"})
    List<Pedido> findAllByOrderByFechaCreacionDesc();

    @EntityGraph(attributePaths = {"usuario", "detalles", "detalles.plato"})
    List<Pedido> findByUsuario_IdOrderByFechaCreacionDesc(Long usuarioId);
}
