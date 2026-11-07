package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.MesaConfiguracionForm;
import co.edu.tecnologicaoriente.sigr.dto.MesaForm;
import co.edu.tecnologicaoriente.sigr.dto.ReservaForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoReserva;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.repository.ReservaRepository;
import co.edu.tecnologicaoriente.sigr.service.ReservaService;
import co.edu.tecnologicaoriente.sigr.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReservaIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReservaService reservas;
    @Autowired UsuarioService usuarios;
    @Autowired ReservaRepository reservaRepo;

    private static final String ADMIN_PW = "Admin1234";
    private static final String PW = "Mesero1234";
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private co.edu.tecnologicaoriente.sigr.model.Mesa mesa(String nombre, int capacidad) {
        MesaForm f = new MesaForm();
        f.setNombre(nombre); f.setCapacidad(capacidad); f.setActiva(true);
        return reservas.crearMesa(f);
    }

    private void crearUsuario(String user, Rol rol) {
        co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm f = new co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm();
        f.setNombreCompleto(user); f.setUsuario(user); f.setContrasena(PW); f.setRol(rol); f.setActivo(true);
        usuarios.crear(f);
    }

    private ReservaForm form(Long mesaId, String titular, LocalDate fecha, String ini, String fin, int personas) {
        ReservaForm f = new ReservaForm();
        f.setMesaId(mesaId); f.setClienteNombre(titular); f.setFecha(fecha);
        f.setHoraInicio(LocalTime.parse(ini)); f.setHoraFin(LocalTime.parse(fin));
        f.setCantidadPersonas(personas);
        return f;
    }

    @Test
    void crearValidaCapacidadFechaYHorario() {
        var m = mesa("Mesa capacidad", 4);
        crearUsuario("cliente-cap", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-cap");
        LocalDate manana = LocalDate.now(BOGOTA).plusDays(1);

        assertThatThrownBy(() -> reservas.crear(actor, form(m.getId(), "x", manana, "12:00", "13:00", 5)))
                .isInstanceOf(ReservaService.ReservaInvalidaException.class);
        assertThatThrownBy(() -> reservas.crear(actor, form(m.getId(), "x", manana, "13:00", "12:00", 2)))
                .isInstanceOf(ReservaService.ReservaInvalidaException.class);
        assertThatThrownBy(() -> reservas.crear(actor, form(m.getId(), "x", LocalDate.now(BOGOTA).minusDays(1), "12:00", "13:00", 2)))
                .isInstanceOf(ReservaService.ReservaInvalidaException.class);

        var creada = reservas.crear(actor, form(m.getId(), "Cliente Cap", manana, "12:00", "13:00", 4));
        assertThat(creada.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        // CLIENTE: titular forzado a su nombre, no al enviado
        assertThat(creada.getClienteNombre()).isEqualTo("cliente-cap");
    }

    @Test
    void previeneSuperposicionYPermiteCancelada() {
        var m = mesa("Mesa solapada", 6);
        crearUsuario("cliente-solapado", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-solapado");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(2);
        var primera = reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "14:00", 2));

        assertThatThrownBy(() -> reservas.crear(actor, form(m.getId(), "x", dia, "13:00", "15:00", 2)))
                .isInstanceOf(ReservaService.ReservaNoDisponibleException.class);
        // Horario idéntico (12:00-14:00) también se rechaza.
        assertThatThrownBy(() -> reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "14:00", 2)))
                .isInstanceOf(ReservaService.ReservaNoDisponibleException.class);
        // Sin superposición (colindante) sí permite
        reservas.crear(actor, form(m.getId(), "x", dia, "14:00", "15:00", 2));
        // Tras cancelar, el hueco queda libre
        reservas.cancelar(actor, primera.getId());
        reservas.crear(actor, form(m.getId(), "x", dia, "12:30", "13:30", 2));
    }

    @Test
    void cancelarYClienteSoloLoSuyoIdor404() throws Exception {
        var m = mesa("Mesa idor", 4);
        crearUsuario("cliente-r1", Rol.CLIENTE);
        crearUsuario("cliente-r2", Rol.CLIENTE);
        var u1 = reservas.resolverActual("cliente-r1");
        var u2 = reservas.resolverActual("cliente-r2");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(3);
        var propia = reservas.crear(u1, form(m.getId(), "x", dia, "10:00", "11:00", 2));
        var ajena = reservas.crear(u2, form(m.getId(), "x", dia, "12:00", "13:00", 2));

        assertThat(reservas.listarPara(u1)).extracting(r -> r.getId()).containsExactly(propia.getId());
        assertThatThrownBy(() -> reservas.buscarPara(u1, ajena.getId()))
                .isInstanceOf(ReservaService.ReservaNoEncontradaException.class);

        var s1 = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-r1").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses1 = (org.springframework.mock.web.MockHttpSession) s1;
        mockMvc.perform(get("/reservas/{id}", propia.getId()).session(ses1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mesa idor")))
                .andExpect(content().string(containsString("Confirmada")));
        mockMvc.perform(get("/reservas/{id}", ajena.getId()).session(ses1))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/reservas/{id}/cancelar", propia.getId()).with(csrf()).session(ses1))
                .andExpect(status().is3xxRedirection());
        assertThat(reservaRepo.findById(propia.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        mockMvc.perform(get("/reservas").session(ses1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Reservas")));
    }

    @Test
    void meseroYAdminVeTodasYMesasSoloAdmin() throws Exception {
        crearUsuario("mesero-res", Rol.MESERO);
        crearUsuario("cliente-res", Rol.CLIENTE);
        var mesa = mesa("Mesa roles", 4);
        reservas.crear(reservas.resolverActual("cliente-res"),
                form(mesa.getId(), "x", LocalDate.now(BOGOTA).plusDays(4), "10:00", "11:00", 2));
        assertThat(reservas.listarPara(reservas.resolverActual("mesero-res"))).isNotEmpty();

        var sMesero = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-res").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesMesero = (org.springframework.mock.web.MockHttpSession) sMesero;
        mockMvc.perform(get("/reservas/nueva").session(sesMesero)
                        .param("fecha", LocalDate.now(BOGOTA).plusDays(10).toString())
                        .param("horaInicioHora", "10").param("horaInicioMinuto", "0")
                        .param("horaFinHora", "11").param("horaFinMinuto", "0")
                        .param("cantidadPersonas", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nueva reserva")))
                .andExpect(content().string(containsString("Mesa roles")));
        mockMvc.perform(get("/mesas").session(sesMesero)).andExpect(status().isForbidden());

        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesAdmin = (org.springframework.mock.web.MockHttpSession) sAdmin;
        mockMvc.perform(get("/mesas").session(sesAdmin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mesa roles")));
        mockMvc.perform(get("/reservas").session(sesAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/reservas")).andExpect(status().is3xxRedirection());
    }

    @Test
    void claridadHorariosNumeracionYFechas() throws Exception {
        var m = mesa("Mesa claridad", 4);
        crearUsuario("cliente-claridad", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-claridad");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(5);
        var creada = reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "13:30", 2));
        String fechaFmt = dia.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-claridad").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;

        // Formulario: selección explícita 24 h, ayuda, sin precarga arbitraria, selector con capacidad.
        // Sin fecha/horario aún: la mesa se pide después y no se precargan opciones.
        mockMvc.perform(get("/reservas/nueva").session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("12:00 = mediodía; 00:00 = medianoche")))
                .andExpect(content().string(containsString("Hora de inicio — hora (24 horas)")))
                .andExpect(content().string(containsString("Hora de inicio — minutos")))
                .andExpect(content().string(containsString("Hora de fin — hora (24 horas)")))
                .andExpect(content().string(containsString("Hora de fin — minutos")))
                .andExpect(content().string(not(containsString("type=\"time\""))))
                .andExpect(content().string(containsString("-- Hora --")))
                .andExpect(content().string(containsString("-- Minutos --")));
        // Con fecha/horario/personas: solo mesas disponibles (capacidad + libres en todo el intervalo).
        mockMvc.perform(get("/reservas/nueva").session(ses)
                        .param("fecha", dia.plusDays(1).toString())
                        .param("horaInicioHora", "12").param("horaInicioMinuto", "0")
                        .param("horaFinHora", "13").param("horaFinMinuto", "0")
                        .param("cantidadPersonas", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Mesa claridad — 4 personas")));

        // Listado: encabezado N.º, columna Mesa independiente, fecha dd/MM/yyyy y HH:mm.
        mockMvc.perform(get("/reservas").session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("N.º de reserva")))
                .andExpect(content().string(not(containsString("<th>#</th>"))))
                .andExpect(content().string(containsString("Mesa")))
                .andExpect(content().string(containsString(fechaFmt)))
                .andExpect(content().string(containsString("12:00")))
                .andExpect(content().string(containsString("13:30")));

        // Detalle: misma presentación, sin renumerar el ID.
        mockMvc.perform(get("/reservas/{id}", creada.getId()).session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(fechaFmt)))
                .andExpect(content().string(containsString("12:00")))
                .andExpect(content().string(containsString("13:30")));

        // Creación vía formulario con hora/minutos 24 h (fin> inicio, sin superposición).
        mockMvc.perform(post("/reservas/nueva").with(csrf()).session(ses)
                        .param("mesaId", m.getId().toString())
                        .param("fecha", dia.plusDays(1).toString())
                        .param("horaInicioHora", "12").param("horaInicioMinuto", "0")
                        .param("horaFinHora", "13").param("horaFinMinuto", "0")
                        .param("cantidadPersonas", "2"))
                .andExpect(status().is3xxRedirection());
        // Fin anterior al inicio por formulario se rechaza (400 de negocio en el formulario, sin crear).
        mockMvc.perform(post("/reservas/nueva").with(csrf()).session(ses)
                        .param("mesaId", m.getId().toString())
                        .param("fecha", dia.plusDays(2).toString())
                        .param("horaInicioHora", "14").param("horaInicioMinuto", "0")
                        .param("horaFinHora", "12").param("horaFinMinuto", "0")
                        .param("cantidadPersonas", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nueva reserva")));
    }

    @Test
    void disponibilidadFiltraCapacidadYCruces() throws Exception {
        var m1 = mesa("Mesa filtro A", 4);
        var m2 = mesa("Mesa filtro B", 2);
        crearUsuario("cliente-filtro", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-filtro");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(6);
        // 12:00-14:00 en A: bloquea cruces, no horarios separados/feliz de otra mesa/otra fecha.
        reservas.crear(actor, form(m1.getId(), "x", dia, "12:00", "14:00", 2));

        // Cruce: mismo mesa/fecha solapada no disponible.
        assertThat(reservas.listarDisponibles(dia, LocalTime.parse("13:00"), LocalTime.parse("15:00"), 2))
                .extracting(m -> m.getNombre()).contains("Mesa filtro B").doesNotContain("Mesa filtro A");
        // Horario separado (colindante 14:00) sí disponible.
        assertThat(reservas.listarDisponibles(dia, LocalTime.parse("14:00"), LocalTime.parse("15:00"), 2))
                .extracting(m -> m.getNombre()).contains("Mesa filtro A");
        // Fecha distinta sí disponible.
        assertThat(reservas.listarDisponibles(dia.plusDays(1), LocalTime.parse("13:00"), LocalTime.parse("15:00"), 2))
                .extracting(m -> m.getNombre()).contains("Mesa filtro A");
        // Dos mesas diferentes a la misma hora: B libre a las 13:00.
        assertThat(reservas.listarDisponibles(dia, LocalTime.parse("13:00"), LocalTime.parse("15:00"), 2))
                .extracting(m -> m.getNombre()).contains("Mesa filtro B");
        // Capacidad: mesa de 2 no sirve para 4.
        assertThat(reservas.listarDisponibles(dia.plusDays(1), LocalTime.parse("12:00"), LocalTime.parse("13:00"), 4))
                .extracting(m -> m.getNombre()).doesNotContain("Mesa filtro B");

        // Cancelada libera el horario y permanece en el historial.
        var segunda = reservas.crear(actor, form(m2.getId(), "x", dia, "10:00", "11:00", 2));
        reservas.cancelar(actor, segunda.getId());
        assertThat(reservas.listarDisponibles(dia, LocalTime.parse("10:30"), LocalTime.parse("11:30"), 2))
                .extracting(m -> m.getNombre()).contains("Mesa filtro B");
        assertThat(reservaRepo.findById(segunda.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void sinDisponiblesInformaYPermiteCambiar() throws Exception {
        var m = mesa("Mesa llena", 2);
        crearUsuario("cliente-llena", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-llena");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(7);
        reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "13:00", 2));
        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-llena").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;
        mockMvc.perform(get("/reservas/nueva").session(ses)
                        .param("fecha", dia.toString())
                        .param("horaInicioHora", "12").param("horaInicioMinuto", "0")
                        .param("horaFinHora", "13").param("horaFinMinuto", "0")
                        .param("cantidadPersonas", "50"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ninguna mesa activa admite esa cantidad")))
                .andExpect(content().string(not(containsString("Mesa llena"))));
    }

    @Test
    void cambioDeHorarioLimpiaSeleccionInvalida() throws Exception {
        var m = mesa("Mesa limpia", 4);
        crearUsuario("cliente-limpia", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-limpia");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(8);
        reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "13:00", 2));
        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-limpia").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;
        // Al pedir 12:30-13:30 (cruza) la mesa ocupada no aparece (selección limpia).
        mockMvc.perform(get("/reservas/nueva").session(ses)
                        .param("fecha", dia.toString())
                        .param("horaInicioHora", "12").param("horaInicioMinuto", "30")
                        .param("horaFinHora", "13").param("horaFinMinuto", "30")
                        .param("cantidadPersonas", "2")
                        .param("mesaId", m.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Mesa limpia — 4 personas"))));
    }

    @Test
    void errorConservaDatosIntroducidos() throws Exception {
        var m = mesa("Mesa conserva", 4);
        crearUsuario("cliente-conserva", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-conserva");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(9);
        reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "13:00", 2));
        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-conserva").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;
        // POST cruzado (12:30-13:30 sobre 12:00-13:00): re-render con error y datos conservados.
        mockMvc.perform(post("/reservas/nueva").with(csrf()).session(ses)
                        .param("mesaId", m.getId().toString())
                        .param("fecha", dia.toString())
                        .param("horaInicioHora", "12").param("horaInicioMinuto", "30")
                        .param("horaFinHora", "13").param("horaFinMinuto", "30")
                        .param("cantidadPersonas", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ya est")))
                .andExpect(content().string(containsString(dia.toString())))
                .andExpect(content().string(containsString("value=\"3\"")))
                .andExpect(content().string(containsString("value=\"12\" selected")))
                .andExpect(content().string(containsString("Actualizar mesas disponibles")));
    }

    @Test
    void indicadoresTemporalesEnListadoYDetalle() throws Exception {
        var m = mesa("Mesa indicadores", 4);
        crearUsuario("cliente-indicador", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-indicador");
        LocalDate manana = LocalDate.now(BOGOTA).plusDays(1);
        var futura = reservas.crear(actor, form(m.getId(), "x", manana, "12:00", "13:00", 2));
        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-indicador").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;
        mockMvc.perform(get("/reservas").session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Próxima")))
                .andExpect(content().string(containsString("Leyenda")))
                .andExpect(content().string(containsString("situacion-proxima")));
        mockMvc.perform(get("/reservas/{id}", futura.getId()).session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Próxima")))
                .andExpect(content().string(containsString("situacion-proxima")));
        reservas.cancelar(actor, futura.getId());
        mockMvc.perform(get("/reservas/{id}", futura.getId()).session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cancelada")))
                .andExpect(content().string(containsString("situacion-cancelada")));
        // Estado persistido intacto tras ver situación.
        assertThat(reservaRepo.findById(futura.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void vencimientoAutomaticoConRelojControlado() {
        var m = mesa("Mesa vence", 4);
        crearUsuario("cliente-vence", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-vence");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(1);
        var r = reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "13:00", 2));
        java.time.ZonedDateTime fin = java.time.ZonedDateTime.of(dia, java.time.LocalTime.parse("13:00"), BOGOTA);
        // Antes del fin: no vence (se verifica la propia reserva, no el conteo global).
        reservas.vencerReservas(fin.minusMinutes(1));
        assertThat(reservaRepo.findById(r.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        // Fin exacto: vence.
        reservas.vencerReservas(fin);
        var vencida = reservaRepo.findById(r.getId()).orElseThrow();
        assertThat(vencida.getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        assertThat(vencida.getMotivoCancelacion()).isEqualTo(co.edu.tecnologicaoriente.sigr.model.MotivoCancelacion.VENCIMIENTO);
        assertThat(vencida.getFechaCancelacion()).isNotNull();
        assertThat(vencida.getEstadoConMotivo()).isEqualTo("Cancelada por vencimiento");
        // Repetir no altera motivo ni fecha.
        var f1 = vencida.getFechaCancelacion();
        reservas.vencerReservas(fin.plusHours(2));
        var rep = reservaRepo.findById(r.getId()).orElseThrow();
        assertThat(rep.getMotivoCancelacion()).isEqualTo(co.edu.tecnologicaoriente.sigr.model.MotivoCancelacion.VENCIMIENTO);
        assertThat(rep.getFechaCancelacion()).isEqualTo(f1);
    }

    @Test
    void vencimientoNoCancelaFuturaNiManualPrevia() {
        var m = mesa("Mesa novence", 4);
        crearUsuario("cliente-novence", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-novence");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(2);
        var futura = reservas.crear(actor, form(m.getId(), "x", dia, "08:00", "09:00", 2));
        var manual = reservas.crear(actor, form(m.getId(), "x", dia, "10:00", "11:00", 2));
        reservas.cancelar(actor, manual.getId());
        var m1 = reservaRepo.findById(manual.getId()).orElseThrow();
        assertThat(m1.getMotivoCancelacion()).isEqualTo(co.edu.tecnologicaoriente.sigr.model.MotivoCancelacion.MANUAL);
        var f1 = m1.getFechaCancelacion();
        // Ahora es dia-1 20:00 Bogota: futura (fin dia 09:00) no debe vencer aunque 09:00 < 20:00.
        var ahora = java.time.ZonedDateTime.of(dia.minusDays(1), java.time.LocalTime.parse("20:00"), BOGOTA);
        reservas.vencerReservas(ahora);
        assertThat(reservaRepo.findById(futura.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        // Manual previa intacta.
        var m2 = reservaRepo.findById(manual.getId()).orElseThrow();
        assertThat(m2.getMotivoCancelacion()).isEqualTo(co.edu.tecnologicaoriente.sigr.model.MotivoCancelacion.MANUAL);
        assertThat(m2.getFechaCancelacion()).isEqualTo(f1);
        // Fecha pasada vencida sí vence.
        var pasada = reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "13:00", 2));
        // Fecha pasada ya no se puede crear por crear(); simular vencida futura -> mover fecha en DB no permitido.
        // Se verifica con reloj posterior al fin de la futura.
        reservas.vencerReservas(java.time.ZonedDateTime.of(dia, java.time.LocalTime.parse("09:00"), BOGOTA));
        assertThat(reservaRepo.findById(futura.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    void mesasDemoRepetibleSinDuplicarPorHttp() throws Exception {
        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesAdmin = (org.springframework.mock.web.MockHttpSession) sAdmin;
        reservas.cargarMesasDemo();
        long antes = reservas.listarMesas().size();
        mockMvc.perform(post("/mesas/cargar-demo").with(csrf()).session(sesAdmin))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/mesas/cargar-demo").with(csrf()).session(sesAdmin))
                .andExpect(status().is3xxRedirection());
        assertThat(reservas.listarMesas()).hasSize((int) antes);
    }

    @Test
    void mesasDemoIdempotente() {
        int n1 = reservas.cargarMesasDemo();
        assertThat(n1).isGreaterThanOrEqualTo(0);
        int n2 = reservas.cargarMesasDemo();
        assertThat(n2).isEqualTo(0);
        assertThat(reservas.listarMesasActivas()).extracting(m -> m.getNombre())
                .contains("Mesa 1", "Mesa 2", "Mesa 3", "Mesa 4", "Terraza 1");
    }

    @Test
    void tresMesasMesa1OcupadaOfrece2y3() throws Exception {
        var m1 = mesa("Tres 1", 4);
        var m2 = mesa("Tres 2", 4);
        var m3 = mesa("Tres 3", 4);
        crearUsuario("cliente-tres", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-tres");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(11);
        reservas.crear(actor, form(m1.getId(), "x", dia, "12:00", "16:42", 4));
        var disp = reservas.listarDisponibles(dia, LocalTime.parse("13:00"), LocalTime.parse("14:00"), 4);
        assertThat(disp).extracting(m -> m.getNombre()).contains("Tres 2", "Tres 3").doesNotContain("Tres 1");
        // Vía HTTP: el formulario ofrece 2 y 3, no la 1.
        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-tres").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;
        mockMvc.perform(get("/reservas/nueva").session(ses)
                        .param("fecha", dia.toString())
                        .param("horaInicioHora", "13").param("horaInicioMinuto", "0")
                        .param("horaFinHora", "14").param("horaFinMinuto", "0")
                        .param("cantidadPersonas", "4"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tres 2 — 4 personas")))
                .andExpect(content().string(containsString("Tres 3 — 4 personas")))
                .andExpect(content().string(not(containsString("Tres 1 — 4 personas"))));
    }

    @Test
    void configuracionAmpliarReduceProtegeReactiva() throws Exception {
        crearUsuario("mesero-cfg", Rol.MESERO);
        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesAdmin = (org.springframework.mock.web.MockHttpSession) sAdmin;
        var sMesero = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-cfg").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesMesero = (org.springframework.mock.web.MockHttpSession) sMesero;
        mockMvc.perform(get("/mesas").session(sesMesero)).andExpect(status().isForbidden());

        // Ampliar: 2 -> 5 activas con numeración única y capacidad indicada.
        long activas0 = reservas.resumenMesas().activas();
        var cfg = new MesaConfiguracionForm();
        cfg.setCantidadActivas((int) activas0 + 3);
        cfg.setCapacidadNuevas(4);
        reservas.ajustarCantidadActivas(cfg);
        assertThat(reservas.resumenMesas().activas()).isEqualTo(activas0 + 3);
        assertThat(reservas.listarMesas()).extracting(m -> m.getNombre()).doesNotHaveDuplicates();
        long capacidadAntes = reservas.resumenMesas().capacidadTotalActiva();
        assertThat(capacidadAntes).isGreaterThan(0);

        // Reducción protegida: mesa con pendiente no se desactiva.
        var m = mesa("Mesa protegida", 4);
        crearUsuario("cliente-prot", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-prot");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(12);
        var r = reservas.crear(actor, form(m.getId(), "x", dia, "12:00", "13:00", 2));
        assertThatThrownBy(() -> reservas.cambiarEstadoMesa(m.getId(), false))
                .isInstanceOf(ReservaService.ReservaInvalidaException.class)
                .hasMessageContaining("reserva");
        var cfg2 = new MesaConfiguracionForm();
        cfg2.setCantidadActivas((int) reservas.resumenMesas().activas() - 1);
        cfg2.setCapacidadNuevas(4);
        cfg2.setMesasADesactivar(java.util.List.of(m.getId()));
        assertThatThrownBy(() -> reservas.ajustarCantidadActivas(cfg2))
                .isInstanceOf(ReservaService.ReservaInvalidaException.class);
        // Historial conservado y reactivación posible sin borrar.
        assertThat(reservaRepo.findById(r.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        reservas.cancelar(actor, r.getId());
        reservas.cambiarEstadoMesa(m.getId(), false);
        assertThat(reservas.buscarMesa(m.getId()).isActiva()).isFalse();
        reservas.cambiarEstadoMesa(m.getId(), true);
        assertThat(reservas.buscarMesa(m.getId()).isActiva()).isTrue();
        assertThat(reservaRepo.findById(r.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        // Listado muestra resumen calculado.
        mockMvc.perform(get("/mesas").session(sesAdmin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registradas")))
                .andExpect(content().string(containsString("Activas")))
                .andExpect(content().string(containsString("Inactivas")))
                .andExpect(content().string(containsString("Capacidad total activa")));
    }
}
