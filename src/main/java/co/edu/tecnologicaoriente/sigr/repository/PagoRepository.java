package co.edu.tecnologicaoriente.sigr.repository;

import co.edu.tecnologicaoriente.sigr.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findBySesion_Id(Long sesionId);
    Optional<Pago> findByComprobante_Id(Long comprobanteId);
    boolean existsByComprobante_Id(Long comprobanteId);
}
