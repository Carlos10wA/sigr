package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.dto.MesaConfiguracionForm;
import co.edu.tecnologicaoriente.sigr.dto.MesaForm;
import co.edu.tecnologicaoriente.sigr.dto.ReservaForm;
import co.edu.tecnologicaoriente.sigr.model.*;
import co.edu.tecnologicaoriente.sigr.repository.MesaRepository;
import co.edu.tecnologicaoriente.sigr.repository.ReservaRepository;
import co.edu.tecnologicaoriente.sigr.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
public class ReservaService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    public static class MesaDuplicadaException extends RuntimeException {
        public MesaDuplicadaException(String m) { super(m); }
    }
    public static class ReservaInvalidaException extends RuntimeException {
        public ReservaInvalidaException(String m) { super(m); }
    }
    public static class ReservaNoDisponibleException extends RuntimeException {
        public ReservaNoDisponibleException(String m) { super(m); }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Reserva no encontrada")
    public static class ReservaNoEncontradaException extends RuntimeException {
        public ReservaNoEncontradaException(String m) { super(m); }
    }

    private final MesaRepository mesas;
    private final ReservaRepository reservas;
    private final UsuarioRepository usuarios;

    public ReservaService(MesaRepository mesas, ReservaRepository reservas, UsuarioRepository usuarios) {
        this.mesas = mesas; this.reservas = reservas; this.usuarios = usuarios;
    }

    public Usuario resolverActual(String username) {
        return usuarios.findByUsuario(username)
                .orElseThrow(() -> new ReservaNoEncontradaException("Usuario no encontrado."));
    }

    // ---- Mesas (solo ADMINISTRADOR lo invoca desde el controlador) ----
    @Transactional(readOnly = true)
    public List<Mesa> listarMesas() { return mesas.findAll(); }

    @Transactional(readOnly = true)
    public List<Mesa> listarMesasActivas() { return mesas.findByActivaTrueOrderByNombreAsc(); }

    /**
     * Mesas activas, con capacidad suficiente y libres durante todo el intervalo.
     * Las canceladas no bloquean. Sin datos completos devuelve vacío.
     */
    @Transactional(readOnly = true)
    public List<Mesa> listarDisponibles(LocalDate fecha, LocalTime inicio, LocalTime fin, Integer personas) {
        if (fecha == null || inicio == null || fin == null || personas == null
                || !fin.isAfter(inicio) || personas < 1) {
            return List.of();
        }
        return mesas.findByActivaTrueOrderByNombreAsc().stream()
                .filter(m -> m.getCapacidad() >= personas)
                .filter(m -> reservas.findByMesa_IdAndFechaAndEstado(m.getId(), fecha, EstadoReserva.CONFIRMADA).stream()
                        .noneMatch(ex -> inicio.isBefore(ex.getHoraFin()) && fin.isAfter(ex.getHoraInicio())))
                .toList();
    }

    /** Convierte los campos hora/minutos del formulario a LocalTime (o null si incompletos). */
    public LocalTime resolverHora(Integer hora, Integer minuto) {
        if (hora == null || minuto == null || hora < 0 || hora > 23 || minuto < 0 || minuto > 59) {
            return null;
        }
        return LocalTime.of(hora, minuto);
    }

    /** Resumen calculado desde las mesas reales (sin contador independiente). */
    public record ResumenMesas(long registradas, long activas, long inactivas, int capacidadTotalActiva) {}

    @Transactional(readOnly = true)
    public ResumenMesas resumenMesas() {
        List<Mesa> todas = mesas.findAll();
        long activas = todas.stream().filter(Mesa::isActiva).count();
        int capacidad = todas.stream().filter(Mesa::isActiva).mapToInt(Mesa::getCapacidad).sum();
        return new ResumenMesas(todas.size(), activas, todas.size() - activas, capacidad);
    }

    @Transactional(readOnly = true)
    public List<Reserva> pendientesConfirmadas(Long mesaId) {
        return reservas.findByMesa_IdAndEstadoAndFechaGreaterThanEqualOrderByFechaAsc(
                mesaId, EstadoReserva.CONFIRMADA, LocalDate.now(BOGOTA));
    }

    @Transactional(readOnly = true)
    public Mesa buscarMesa(Long id) {
        return mesas.findById(id).orElseThrow(() -> new ReservaInvalidaException("La mesa no existe."));
    }

    /** Situación temporal actual (America/Bogota), sin cambiar el estado persistido. */
    @Transactional(readOnly = true)
    public SituacionReserva situacion(Reserva r) {
        return r.situacionEn(java.time.ZonedDateTime.now(BOGOTA));
    }

    @Transactional(readOnly = true)
    public java.util.Map<Long, SituacionReserva> situaciones(Iterable<Reserva> reservas) {
        var ahora = java.time.ZonedDateTime.now(BOGOTA);
        java.util.Map<Long, SituacionReserva> mapa = new java.util.LinkedHashMap<>();
        reservas.forEach(r -> mapa.put(r.getId(), r.situacionEn(ahora)));
        return mapa;
    }

    @Transactional
    public Mesa crearMesa(MesaForm form) {
        if (mesas.existsByNombre(form.getNombre().trim())) {
            throw new MesaDuplicadaException("Ya existe una mesa con ese nombre.");
        }
        return mesas.save(new Mesa(form.getNombre().trim(), form.getCapacidad(), form.isActiva()));
    }

    @Transactional
    public Mesa editarMesa(Long id, MesaForm form) {
        Mesa m = buscarMesa(id);
        if (mesas.existsByNombreAndIdNot(form.getNombre().trim(), id)) {
            throw new MesaDuplicadaException("Ya existe otra mesa con ese nombre.");
        }
        if (m.isActiva() && !form.isActiva()) {
            var pendientes = pendientesConfirmadas(id);
            if (!pendientes.isEmpty()) {
                StringBuilder detalle = new StringBuilder();
                for (Reserva r : pendientes) {
                    detalle.append(" reserva ").append(r.getId()).append(" del ").append(r.getFecha()).append(";");
                }
                throw new ReservaInvalidaException(
                        "No se puede desactivar " + m.getNombre() + " porque tiene " + pendientes.size()
                                + " reserva(s) confirmada(s) pendiente(s):" + detalle
                                + " Cancélelas o resuélvalas antes de desactivar.");
            }
        }
        m.setNombre(form.getNombre().trim());
        m.setCapacidad(form.getCapacidad());
        m.setActiva(form.isActiva());
        return mesas.save(m);
    }

    @Transactional
    public Mesa cambiarEstadoMesa(Long id, boolean activa) {
        Mesa m = buscarMesa(id);
        if (m.isActiva() && !activa) {
            var pendientes = pendientesConfirmadas(id);
            if (!pendientes.isEmpty()) {
                StringBuilder detalle = new StringBuilder();
                for (Reserva r : pendientes) {
                    detalle.append(" reserva ").append(r.getId()).append(" del ").append(r.getFecha()).append(";");
                }
                throw new ReservaInvalidaException(
                        "No se puede desactivar " + m.getNombre() + " porque tiene " + pendientes.size()
                                + " reserva(s) confirmada(s) pendiente(s):" + detalle
                                + " Cancélelas o resuélvalas antes de desactivar.");
            }
        }
        m.setActiva(activa);
        return mesas.save(m);
    }

    /**
     * Ajusta la cantidad total de mesas activas sin borrar ni renumerar:
     * crea faltantes (Mesa N único) o desactiva las elegidas.
     */
    @Transactional
    public String ajustarCantidadActivas(MesaConfiguracionForm form) {
        int deseadas = form.getCantidadActivas();
        int capacidadNuevas = form.getCapacidadNuevas();
        List<Mesa> activas = mesas.findByActivaTrueOrderByNombreAsc();
        int faltan = deseadas - activas.size();
        if (faltan > 0) {
            Set<String> usados = new java.util.HashSet<>();
            mesas.findAll().forEach(m -> usados.add(m.getNombre()));
            int siguientes = 1;
            while (faltan > 0) {
                String nombre = "Mesa " + siguientes++;
                if (usados.contains(nombre)) continue;
                mesas.save(new Mesa(nombre, capacidadNuevas, true));
                usados.add(nombre);
                faltan--;
            }
            return "Se crearon mesas hasta alcanzar " + deseadas + " activas.";
        }
        if (faltan == 0) {
            return "La cantidad de mesas activas ya es " + deseadas + ".";
        }
        int aDesactivar = -faltan;
        List<Long> elegidas = form.getMesasADesactivar() == null ? List.of() : form.getMesasADesactivar();
        if (elegidas.size() != aDesactivar) {
            throw new ReservaInvalidaException(
                    "Para bajar a " + deseadas + " activas seleccione exactamente " + aDesactivar + " mesa(s) para desactivar.");
        }
        // Validez y protección de pendientes para cada elección.
        List<Mesa> candidatas = new java.util.ArrayList<>();
        for (Long id : elegidas) {
            Mesa m = mesas.findById(id).orElseThrow(() -> new ReservaInvalidaException("Una mesa seleccionada no existe."));
            if (!m.isActiva()) {
                throw new ReservaInvalidaException("La mesa " + m.getNombre() + " ya está inactiva.");
            }
            candidatas.add(m);
        }
        StringBuilder bloqueadas = new StringBuilder();
        for (Mesa m : candidatas) {
            var pendientes = pendientesConfirmadas(m.getId());
            if (!pendientes.isEmpty()) {
                bloqueadas.append(m.getNombre()).append(" (").append(pendientes.size()).append(" pendiente(s):");
                for (Reserva r : pendientes) {
                    bloqueadas.append(" reserva ").append(r.getId()).append(" del ").append(r.getFecha()).append(";");
                }
                bloqueadas.append(") ");
            }
        }
        if (bloqueadas.length() > 0) {
            throw new ReservaInvalidaException(
                    "No se puede desactivar: " + bloqueadas.toString().trim()
                            + " Cancélelas o resuélvalas antes de desactivar.");
        }
        for (Mesa m : candidatas) {
            m.setActiva(false);
            mesas.save(m);
        }
        return "Se desactivaron " + aDesactivar + " mesa(s) hasta quedar " + deseadas + " activas. El historial se conserva.";
    }

    /** Mesas de demostración idempotentes (no duplican). */
    @Transactional
    public int cargarMesasDemo() {
        int nuevas = 0;
        nuevas += crearSiNo("Mesa 1", 2);
        nuevas += crearSiNo("Mesa 2", 4);
        nuevas += crearSiNo("Mesa 3", 6);
        nuevas += crearSiNo("Mesa 4", 8);
        nuevas += crearSiNo("Terraza 1", 4);
        return nuevas;
    }

    private int crearSiNo(String nombre, int capacidad) {
        if (mesas.existsByNombre(nombre)) return 0;
        mesas.save(new Mesa(nombre, capacidad, true));
        return 1;
    }

    // ---- Reservas ----
    @Transactional(readOnly = true)
    public List<Reserva> listarPara(Usuario actor) {
        if (actor.getRol() == Rol.CLIENTE) {
            return reservas.findByUsuario_IdOrderByFechaAscHoraInicioAsc(actor.getId());
        }
        return reservas.findAllByOrderByFechaAscHoraInicioAsc();
    }

    @Transactional(readOnly = true)
    public Reserva buscarPara(Usuario actor, Long id) {
        Reserva r = reservas.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException("Reserva no encontrada."));
        if (actor.getRol() == Rol.CLIENTE && !r.getUsuario().getId().equals(actor.getId())) {
            throw new ReservaNoEncontradaException("Reserva no encontrada.");
        }
        return r;
    }

    @Transactional
    public Reserva crear(Usuario actor, ReservaForm form) {
        // Bloqueo pesimista: serializa reservas sobre la misma mesa ante solicitudes simultáneas.
        Mesa mesa = mesas.findByIdForUpdate(form.getMesaId())
                .orElseThrow(() -> new ReservaInvalidaException("La mesa seleccionada no existe."));
        if (!mesa.isActiva()) {
            throw new ReservaInvalidaException("La mesa seleccionada está desactivada.");
        }
        if (form.getFecha() == null || form.getHoraInicio() == null || form.getHoraFin() == null) {
            throw new ReservaInvalidaException("Fecha y horarios son obligatorios.");
        }
        if (form.getFecha().isBefore(LocalDate.now(BOGOTA))) {
            throw new ReservaInvalidaException("La fecha no puede ser en el pasado.");
        }
        if (!form.getHoraFin().isAfter(form.getHoraInicio())) {
            throw new ReservaInvalidaException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        int personas = form.getCantidadPersonas() == null ? 0 : form.getCantidadPersonas();
        if (personas < 1) {
            throw new ReservaInvalidaException("Al menos una persona.");
        }
        if (personas > mesa.getCapacidad()) {
            throw new ReservaInvalidaException(
                    "La mesa " + mesa.getNombre() + " solo admite " + mesa.getCapacidad() + " personas.");
        }
        String titular;
        if (actor.getRol() == Rol.CLIENTE) {
            titular = actor.getNombreCompleto();
        } else {
            titular = form.getClienteNombre() == null ? "" : form.getClienteNombre().trim();
            if (titular.isBlank()) {
                throw new ReservaInvalidaException("El nombre del titular es obligatorio.");
            }
        }
        for (Reserva existente : reservas.findByMesa_IdAndFechaAndEstado(mesa.getId(), form.getFecha(), EstadoReserva.CONFIRMADA)) {
            if (form.getHoraInicio().isBefore(existente.getHoraFin())
                    && form.getHoraFin().isAfter(existente.getHoraInicio())) {
                throw new ReservaNoDisponibleException(
                        "La mesa ya está reservada en ese horario.");
            }
        }
        Reserva r = new Reserva();
        r.setUsuario(actor);
        r.setMesa(mesa);
        r.setClienteNombre(titular);
        r.setFecha(form.getFecha());
        r.setHoraInicio(form.getHoraInicio());
        r.setHoraFin(form.getHoraFin());
        r.setCantidadPersonas(personas);
        r.setEstado(EstadoReserva.CONFIRMADA);
        return reservas.save(r);
    }

    @Transactional
    public Reserva cancelar(Usuario actor, Long id) {
        Reserva r = buscarPara(actor, id);
        if (r.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReservaInvalidaException("La reserva ya está cancelada.");
        }
        r.setEstado(EstadoReserva.CANCELADA);
        return reservas.save(r);
    }
}
