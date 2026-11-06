package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.Mesa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findByActivaTrueOrderByNombreAsc();
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);

    /** Bloqueo pesimista de la fila de mesa para impedir cruces simultáneos. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mesa m where m.id = :id")
    Optional<Mesa> findByIdForUpdate(@Param("id") Long id);
}
