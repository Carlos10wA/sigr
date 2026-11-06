package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

public interface PlatoRepository extends JpaRepository<Plato, Long> {
    List<Plato> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    List<Plato> findByCategoriaIdAndActivoTrue(Long categoriaId);
    List<Plato> findByNombreContainingIgnoreCaseAndCategoriaIdAndActivoTrue(String nombre, Long categoriaId);
    List<Plato> findByActivoTrueOrderByNombreAsc();
    List<Plato> findByCategoria_IdInAndActivoTrue(Collection<Long> categoriaIds);
    boolean existsByNombre(String nombre);
}
