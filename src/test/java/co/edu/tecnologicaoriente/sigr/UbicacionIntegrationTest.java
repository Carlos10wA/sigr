package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.dto.UbicacionForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.service.ConfiguracionService;
import co.edu.tecnologicaoriente.sigr.service.FacturacionService;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import co.edu.tecnologicaoriente.sigr.service.PedidoService;
import co.edu.tecnologicaoriente.sigr.service.UsuarioService;
import co.edu.tecnologicaoriente.sigr.service.ReservaService;
import co.edu.tecnologicaoriente.sigr.dto.MesaForm;
import co.edu.tecnologicaoriente.sigr.dto.ReservaForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UbicacionIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ConfiguracionService configuracion;
    @Autowired MenuService menu;
    @Autowired PedidoService pedidos;
    @Autowired FacturacionService facturas;
    @Autowired UsuarioService usuarios;
    @Autowired ReservaService reservas;

    private static final String ADMIN_PW = "Admin1234";
    private static final String PW = "Mesero1234";
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private UbicacionForm ubicacion(String pais, String ciudad, String zona) {
        UbicacionForm f = new UbicacionForm();
        f.setPais(pais); f.setCiudad(ciudad); f.setDireccion("Calle 10"); f.setZonaHoraria(zona);
        return f;
    }

    private void crearUsuario(String user, Rol rol) {
        co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm f = new co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm();
        f.setNombreCompleto(user); f.setUsuario(user); f.setContrasena(PW); f.setRol(rol); f.setActivo(true);
        usuarios.crear(f);
    }

    @Test
    void soloAdminConfiguraYCiudadExigeSuZona() throws Exception {
        crearUsuario("mesero-ub", Rol.MESERO);
        var sMesero = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-ub").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesMesero = (org.springframework.mock.web.MockHttpSession) sMesero;
        mockMvc.perform(get("/configuracion").session(sesMesero)).andExpect(status().isForbidden());

        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesAdmin = (org.springframework.mock.web.MockHttpSession) sAdmin;
        mockMvc.perform(get("/configuracion").session(sesAdmin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Configuración del restaurante")));
        // Zona manipulada se ignora: la ciudad única manda (Bogotá -> America/Bogota).
        var f = ubicacion("Colombia", "Bogotá", "America/Mexico_City");
        configuracion.actualizar(f);
        assertThat(configuracion.actual().getCiudad()).isEqualTo("Bogotá");
        assertThat(configuracion.actual().getZonaHoraria()).isEqualTo("America/Bogota");
    }

    @Test
    void caliMuestraSoloSuZonaSinOtraCiudad() throws Exception {
        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesAdmin = (org.springframework.mock.web.MockHttpSession) sAdmin;
        // Se envían pais/zona manipulados: el servidor los ignora y deriva de Cali.
        mockMvc.perform(post("/configuracion").with(csrf()).session(sesAdmin)
                        .param("ciudad", "Cali").param("direccion", "Calle 1")
                        .param("pais", "España").param("zonaHoraria", "Europe/Madrid"))
                .andExpect(status().is3xxRedirection());
        assertThat(configuracion.actual().getCiudad()).isEqualTo("Cali");
        assertThat(configuracion.actual().getPais()).isEqualTo("Colombia");
        assertThat(configuracion.actual().getZonaHoraria()).isEqualTo("America/Bogota");
        assertThat(configuracion.actual().getDireccion()).isEqualTo("Calle 1");
        mockMvc.perform(get("/configuracion").session(sesAdmin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("readonly")))
                .andExpect(content().string(containsString("America/Bogota")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Bucaramanga — America/Bogota"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("type=\"select\""))));
    }

    @Test
    void comprobanteGuardaCopiaYCambioNoAlteraEmitido() throws Exception {
        configuracion.actualizar(ubicacion("Colombia", "Bogotá", "America/Bogota"));
        var catForm = new CategoriaForm();
        catForm.setNombre("Ub-cat"); catForm.setActiva(true);
        var cat = menu.crearCategoria(catForm);
        var pf = new PlatoForm();
        pf.setNombre("Plato ub"); pf.setDescripcion("Desc"); pf.setCategoriaId(cat.getId());
        pf.setPrecio(new BigDecimal("10000")); pf.setFoto("empanadas.jpg"); pf.setDisponible(true); pf.setActivo(true);
        var plato = menu.crearPlato(pf);
        crearUsuario("mesero-ub2", Rol.MESERO);
        var mesero = facturas.resolverActual("mesero-ub2");
        var pedidoForm = new PedidoForm();
        var l = new PedidoLineaForm(); l.setPlatoId(plato.getId()); l.setCantidad(1);
        pedidoForm.getLineas().add(l);
        var pedido = pedidos.crear(mesero, pedidoForm);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.LISTO);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO);
        var c = facturas.emitir(mesero, pedido.getId());
        assertThat(c.getCiudad()).isEqualTo("Bogotá");
        assertThat(c.getPais()).isEqualTo("Colombia");
        assertThat(c.getZonaHoraria()).isEqualTo("America/Bogota");
        // Cambiar configuración después no altera el comprobante emitido.
        configuracion.actualizar(ubicacion("Colombia", "Medellín", "America/Bogota"));
        var re = facturas.buscarPara(mesero, c.getId());
        assertThat(re.getCiudad()).isEqualTo("Bogotá");
        assertThat(re.getZonaHoraria()).isEqualTo("America/Bogota");
        // Vista muestra ciudad y país, no el técnico (America/Bogota) como ubicación.
        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-ub2").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;
        mockMvc.perform(get("/facturas/{id}", c.getId()).session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bogotá")))
                .andExpect(content().string(containsString("Colombia")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("(America/Bogota)"))));
    }

    @Test
    void bloqueaCambioDeZonaConPendientes() {
        configuracion.actualizar(ubicacion("Colombia", "Bogotá", "America/Bogota"));
        var mf = new MesaForm();
        mf.setNombre("Mesa bloqueo"); mf.setCapacidad(4); mf.setActiva(true);
        var mesa = reservas.crearMesa(mf);
        crearUsuario("cliente-bloq", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-bloq");
        var rf = new ReservaForm();
        rf.setMesaId(mesa.getId()); rf.setFecha(LocalDate.now(BOGOTA).plusDays(1));
        rf.setHoraInicio(LocalTime.parse("12:00")); rf.setHoraFin(LocalTime.parse("13:00"));
        rf.setCantidadPersonas(2);
        reservas.crear(actor, rf);
        assertThatThrownBy(() -> configuracion.actualizar(ubicacion("México", "Ciudad de México", "America/Mexico_City")))
                .isInstanceOf(ConfiguracionService.ConfiguracionInvalidaException.class)
                .hasMessageContaining("pendiente");
        // Cambiar ciudad dentro de la misma zona sí permite.
        configuracion.actualizar(ubicacion("Colombia", "Medellín", "America/Bogota"));
        assertThat(configuracion.actual().getCiudad()).isEqualTo("Medellín");
    }
}
