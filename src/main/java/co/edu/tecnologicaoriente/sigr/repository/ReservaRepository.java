package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.EstadoReserva;
import co.edu.tecnologicaoriente.sigr.model.Reserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @EntityGraph(attributePaths = {"usuario", "mesa"})
    Optional<Reserva> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "mesa"})
    List<Reserva> findAllByOrderByFechaAscHoraInicioAsc();

    @EntityGraph(attributePaths = {"usuario", "mesa"})
    List<Reserva> findByUsuario_IdOrderByFechaAscHoraInicioAsc(Long usuarioId);

    List<Reserva> findByMesa_IdAndFechaAndEstado(Long mesaId, LocalDate fecha, EstadoReserva estado);

    List<Reserva> findByMesa_IdAndEstadoAndFechaGreaterThanEqualOrderByFechaAsc(Long mesaId, EstadoReserva estado, LocalDate desde);
}
