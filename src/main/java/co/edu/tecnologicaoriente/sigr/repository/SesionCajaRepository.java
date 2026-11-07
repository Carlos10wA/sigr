package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.EstadoSesionCaja;
import co.edu.tecnologicaoriente.sigr.model.SesionCaja;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    @EntityGraph(attributePaths = {"responsable", "pagos"})
    Optional<SesionCaja> findById(Long id);

    @EntityGraph(attributePaths = {"responsable", "pagos"})
    List<SesionCaja> findAllByOrderByFechaAperturaDesc();

    Optional<SesionCaja> findByEstado(EstadoSesionCaja estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SesionCaja s where s.id = :id")
    Optional<SesionCaja> findByIdForUpdate(@Param("id") Long id);
}
