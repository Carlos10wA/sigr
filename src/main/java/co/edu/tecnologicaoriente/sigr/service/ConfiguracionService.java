package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.dto.UbicacionForm;
import co.edu.tecnologicaoriente.sigr.model.ConfiguracionRestaurante;
import co.edu.tecnologicaoriente.sigr.model.EstadoReserva;
import co.edu.tecnologicaoriente.sigr.repository.ConfiguracionRestauranteRepository;
import co.edu.tecnologicaoriente.sigr.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class ConfiguracionService {

    public static class ConfiguracionInvalidaException extends RuntimeException {
        public ConfiguracionInvalidaException(String m) { super(m); }
    }

    public record CiudadZona(String pais, String ciudad, String zona) {}

    public static final java.util.List<CiudadZona> CIUDADES = java.util.List.of(
            new CiudadZona("Colombia", "Bogotá", "America/Bogota"),
            new CiudadZona("Colombia", "Medellín", "America/Bogota"),
            new CiudadZona("Colombia", "Cali", "America/Bogota"),
            new CiudadZona("Colombia", "Barranquilla", "America/Bogota"),
            new CiudadZona("Colombia", "Cartagena", "America/Bogota"),
            new CiudadZona("Colombia", "Bucaramanga", "America/Bogota"),
            new CiudadZona("México", "Ciudad de México", "America/Mexico_City"),
            new CiudadZona("Perú", "Lima", "America/Lima"),
            new CiudadZona("Chile", "Santiago", "America/Santiago"),
            new CiudadZona("Argentina", "Buenos Aires", "America/Argentina/Buenos_Aires"),
            new CiudadZona("Panamá", "Ciudad de Panamá", "America/Panama"),
            new CiudadZona("Ecuador", "Quito", "America/Guayaquil"),
            new CiudadZona("Venezuela", "Caracas", "America/Caracas"),
            new CiudadZona("España", "Madrid", "Europe/Madrid")
    );

    private final ConfiguracionRestauranteRepository repo;
    private final ReservaRepository reservas;
    private final java.time.Clock clock;

    public ConfiguracionService(ConfiguracionRestauranteRepository repo, ReservaRepository reservas,
                                java.time.Clock clock) {
        this.repo = repo; this.reservas = reservas; this.clock = clock;
    }

    @Transactional
    public ConfiguracionRestaurante obtener() {
        return repo.findById(1L).orElseGet(() -> {
            var c = new ConfiguracionRestaurante();
            return repo.save(c);
        });
    }

    @Transactional(readOnly = true)
    public ConfiguracionRestaurante actual() {
        return repo.findById(1L).orElse(new ConfiguracionRestaurante());
    }

    public ZoneId zonaActual() {
        try {
            return ZoneId.of(actual().getZonaHoraria());
        } catch (Exception e) {
            return ZoneId.of(ConfiguracionRestaurante.ZONA_POR_DEFECTO);
        }
    }

    public java.util.List<CiudadZona> ciudades() { return CIUDADES; }

    @Transactional
    public ConfiguracionRestaurante actualizar(UbicacionForm form) {
        // La ciudad es la única selección: país y zona se derivan del catálogo (no del navegador).
        CiudadZona elegida = CIUDADES.stream()
                .filter(c -> c.ciudad().equalsIgnoreCase(form.getCiudad().trim()))
                .findFirst()
                .orElseThrow(() -> new ConfiguracionInvalidaException(
                        "Seleccione una ciudad del catálogo."));
        try {
            ZoneId.of(elegida.zona());
        } catch (Exception e) {
            throw new ConfiguracionInvalidaException("La zona horaria no es válida.");
        }
        ConfiguracionRestaurante c = obtener();
        String nuevaZona = elegida.zona();
        String zonaAnterior = c.getZonaHoraria();
        if (!nuevaZona.equals(zonaAnterior)) {
            // No reinterpretar reservas pendientes: si hay confirmadas futuras, bloquear.
            var hoy = LocalDate.now(clock);
            var pendientes = reservas.findByEstado(EstadoReserva.CONFIRMADA).stream()
                    .filter(r -> !r.getFecha().isBefore(hoy))
                    .toList();
            if (!pendientes.isEmpty()) {
                throw new ConfiguracionInvalidaException(
                        "No se puede cambiar la zona horaria (" + zonaAnterior + " → " + nuevaZona
                                + ") porque desplazaría los horarios de " + pendientes.size()
                                + " reserva(s) pendiente(s). Resuélvalas (cancelar o esperar a que terminen) antes de cambiar la zona. "
                                + "Los horarios ya registrados no se reinterpretan.");
            }
        }
        c.setPais(elegida.pais());
        c.setCiudad(elegida.ciudad());
        c.setDireccion(form.getDireccion() == null ? "" : form.getDireccion().trim());
        c.setZonaHoraria(nuevaZona);
        return repo.save(c);
    }
}
