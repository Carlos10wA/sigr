package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.Plato;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlatoRepository extends JpaRepository<Plato, Long> {
    // LAZY se mantiene en Plato.categoria; @EntityGraph carga la categoría
    // solo en las consultas que la vista necesita (catálogo y detalle),
    // evitando LazyInitializationException fuera de la transacción.
    @EntityGraph(attributePaths = "categoria")
    Optional<Plato> findById(Long id);

    @EntityGraph(attributePaths = "categoria")
    List<Plato> findAll();

    @EntityGraph(attributePaths = "categoria")
    List<Plato> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    @EntityGraph(attributePaths = "categoria")
    List<Plato> findByCategoriaIdAndActivoTrue(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    List<Plato> findByNombreContainingIgnoreCaseAndCategoriaIdAndActivoTrue(String nombre, Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    List<Plato> findByActivoTrueOrderByNombreAsc();

    @EntityGraph(attributePaths = "categoria")
    List<Plato> findByCategoria_IdInAndActivoTrue(Collection<Long> categoriaIds);

    List<Plato> findByActivoTrueAndDisponibleTrueOrderByNombreAsc();

    boolean existsByNombre(String nombre);
}
