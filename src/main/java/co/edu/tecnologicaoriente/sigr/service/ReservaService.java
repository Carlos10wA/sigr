package co.edu.tecnologicaoriente.sigr.service;

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
import java.time.ZoneId;
import java.util.List;

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

    @Transactional(readOnly = true)
    public Mesa buscarMesa(Long id) {
        return mesas.findById(id).orElseThrow(() -> new ReservaInvalidaException("La mesa no existe."));
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
        m.setNombre(form.getNombre().trim());
        m.setCapacidad(form.getCapacidad());
        m.setActiva(form.isActiva());
        return mesas.save(m);
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
        Mesa mesa = mesas.findById(form.getMesaId())
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
