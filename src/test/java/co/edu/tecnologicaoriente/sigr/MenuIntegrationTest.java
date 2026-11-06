package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.repository.PlatoRepository;
import co.edu.tecnologicaoriente.sigr.service.DemoDataService;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import co.edu.tecnologicaoriente.sigr.service.UsuarioService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MenuIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired MenuService menu;
    @Autowired DemoDataService demo;
    @Autowired UsuarioService usuarios;
    @Autowired PlatoRepository platoRepo;
    @Autowired EntityManager em;

    private static final String ADMIN_PW = "Admin1234";
    private static final String PW = "Mesero1234";

    private co.edu.tecnologicaoriente.sigr.model.Categoria categoria(String nombre, boolean activa) {
        CategoriaForm f = new CategoriaForm();
        f.setNombre(nombre); f.setActiva(activa);
        return menu.crearCategoria(f);
    }

    private PlatoForm platoForm(String nombre, Long catId, String precio) {
        PlatoForm f = new PlatoForm();
        f.setNombre(nombre); f.setDescripcion("Desc " + nombre);
        f.setCategoriaId(catId); f.setPrecio(new BigDecimal(precio));
        f.setFoto("bandeja-paisa.jpg"); f.setDisponible(true); f.setActivo(true);
        return f;
    }

    private void crearUsuario(String user, Rol rol) {
        co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm f = new co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm();
        f.setNombreCompleto(user); f.setUsuario(user); f.setContrasena(PW); f.setRol(rol); f.setActivo(true);
        usuarios.crear(f);
    }

    @Test
    void crearEditarDesactivarPlato() {
        var cat = categoria("Entradas-test", true);
        var p = menu.crearPlato(platoForm("Empanadas-test", cat.getId(), "12000"));
        assertThat(p.getId()).isNotNull();
        PlatoForm edit = platoForm("Empanadas deluxe", cat.getId(), "13500");
        menu.editarPlato(p.getId(), edit);
        assertThat(platoRepo.findById(p.getId()).orElseThrow().getNombre()).isEqualTo("Empanadas deluxe");
        menu.cambiarEstadoPlato(p.getId(), false);
        assertThat(platoRepo.findById(p.getId()).orElseThrow().isActivo()).isFalse();
        assertThat(menu.catalogo(null, null)).noneMatch(x -> x.getId().equals(p.getId()));
    }

    @Test
    void validacionPrecioYCategoria() {
        var catInactiva = categoria("Inactiva-test", false);
        PlatoForm conInactiva = platoForm("Plato fantasma", catInactiva.getId(), "5000");
        assertThatThrownBy(() -> menu.crearPlato(conInactiva)).isInstanceOf(MenuService.CategoriaInvalidaException.class);
    }

    @Test
    void filtroCatalogoPorNombreYCategoria() {
        var cat = categoria("Postres-test", true);
        menu.crearPlato(platoForm("Torta buscable", cat.getId(), "11000"));
        assertThat(menu.catalogo("buscable", null)).hasSize(1);
        assertThat(menu.catalogo(null, cat.getId())).hasSize(1);
        assertThat(menu.catalogo("nadie", null)).isEmpty();
    }

    @Test
    void demoIdempotenteNoDuplica() {
        demo.cargarDemo();
        long total1 = platoRepo.count();
        var r2 = demo.cargarDemo();
        assertThat(r2.categoriasNuevas()).isEqualTo(0);
        assertThat(r2.platosNuevos()).isEqualTo(0);
        assertThat(platoRepo.count()).isEqualTo(total1);
        assertThat(platoRepo.count()).isGreaterThanOrEqualTo(12);
    }

    @Test
    @org.springframework.transaction.annotation.Transactional void persistenciaTrasLimpiarContexto() {
        var cat = categoria("Persist-test", true);
        var p = menu.crearPlato(platoForm("Persistente", cat.getId(), "9999"));
        em.flush(); em.clear();
        var hallado = platoRepo.findById(p.getId()).orElseThrow();
        assertThat(hallado.getNombre()).isEqualTo("Persistente");
        assertThat(hallado.getPrecio()).isEqualByComparingTo(new BigDecimal("9999"));
    }

    @Test
    void catalogoRenderizaDatosDemoConNombresCategoriasPreciosYFotos() throws Exception {
        demo.cargarDemo();

        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var session = (org.springframework.mock.web.MockHttpSession) sAdmin;

        mockMvc.perform(get("/platos").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Empanadas")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Entradas")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$12.000,00 COP")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/platos/empanadas.jpg")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Bandeja paisa")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Platos principales")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("$28.000,00 COP")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/platos/bandeja-paisa.jpg")));

        // El detalle también debe renderizar la categoría cargada con @EntityGraph.
        Long idEmpanada = platoRepo.findAll().stream()
                .filter(p -> "Empanadas".equals(p.getNombre()))
                .map(co.edu.tecnologicaoriente.sigr.model.Plato::getId)
                .findFirst().orElseThrow();
        mockMvc.perform(get("/platos/{id}", idEmpanada).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Entradas")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/platos/empanadas.jpg")));
    }

    @Test
    void errorGenericoEnEspanolSinWhitelabel() throws Exception {
        crearUsuario("cliente-error", Rol.CLIENTE);
        var sCliente = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-error").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var session = (org.springframework.mock.web.MockHttpSession) sCliente;

        // 404 conserva el estado HTTP (sin Whitelabel).
        mockMvc.perform(get("/ruta-que-no-existe-sigr").session(session))
                .andExpect(status().isNotFound());

        // La plantilla genérica /error responde en español, sin detalles técnicos,
        // conservando el 404 original. MockMvc necesita Accept: text/html y los
        // atributos de error para seleccionar la vista HTML (no JSON).
        mockMvc.perform(get("/error")
                        .accept("text/html")
                        .requestAttr("jakarta.servlet.error.status_code", 404)
                        .requestAttr("jakarta.servlet.error.request_uri", "/ruta-que-no-existe-sigr")
                        .requestAttr("jakarta.servlet.error.message", "Not Found")
                        .session(session))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ocurri")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Whitelabel"))));
    }

    @Test
    void permisosTresRolesEnCatalogoYGestion() throws Exception {
        crearUsuario("mesero-test", Rol.MESERO);
        crearUsuario("cliente-test", Rol.CLIENTE);
        var cat = categoria("Perm-test", true);

        var sMesero = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-test").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        mockMvc.perform(get("/platos").session((org.springframework.mock.web.MockHttpSession) sMesero)).andExpect(status().isOk());
        mockMvc.perform(get("/platos/nuevo").session((org.springframework.mock.web.MockHttpSession) sMesero)).andExpect(status().isForbidden());
        mockMvc.perform(get("/categorias").session((org.springframework.mock.web.MockHttpSession) sMesero)).andExpect(status().isForbidden());

        var sCliente = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-test").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        mockMvc.perform(get("/platos").session((org.springframework.mock.web.MockHttpSession) sCliente)).andExpect(status().isOk());
        mockMvc.perform(get("/categorias").session((org.springframework.mock.web.MockHttpSession) sCliente)).andExpect(status().isForbidden());

        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        mockMvc.perform(get("/platos/nuevo").session((org.springframework.mock.web.MockHttpSession) sAdmin)).andExpect(status().isOk());
        mockMvc.perform(get("/categorias").session((org.springframework.mock.web.MockHttpSession) sAdmin)).andExpect(status().isOk());
        mockMvc.perform(post("/platos/nuevo").with(csrf()).session((org.springframework.mock.web.MockHttpSession) sAdmin)
                .param("nombre", "Malo").param("descripcion", "x").param("categoriaId", cat.getId().toString()).param("precio", "0"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form"));
    }
}
