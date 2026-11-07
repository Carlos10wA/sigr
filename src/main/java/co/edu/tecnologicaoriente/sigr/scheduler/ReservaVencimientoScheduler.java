package co.edu.tecnologicaoriente.sigr.scheduler;

import co.edu.tecnologicaoriente.sigr.service.ReservaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Vence reservas confirmadas expiradas sin depender de que alguien abra la página. */
@Component
public class ReservaVencimientoScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservaVencimientoScheduler.class);
    private final ReservaService reservas;

    public ReservaVencimientoScheduler(ReservaService reservas) { this.reservas = reservas; }

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        ejecutar("al iniciar");
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000, zone = "America/Bogota")
    public void periodico() {
        ejecutar("periódico");
    }

    private void ejecutar(String cuando) {
        try {
            int n = reservas.vencerReservas();
            if (n > 0) {
                log.info("SIGR: {} reserva(s) vencida(s) cancelada(s) {}.", n, cuando);
            }
        } catch (Exception e) {
            log.warn("SIGR: no se pudo vencer reservas {}. {}", cuando, e.getMessage());
        }
    }
}
