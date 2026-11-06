package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm;
import co.edu.tecnologicaoriente.sigr.dto.UsuarioEditarForm;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.repository.UsuarioRepository;
import co.edu.tecnologicaoriente.sigr.service.UsuarioService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository repo;
    @Autowired UsuarioService service;
    @Autowired EntityManager em;

    private static final String ADMIN_PW = "Admin1234";
    private static final String PW = "Mesero1234";

    @Test
    void loginCorrectoRedirige() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void loginIncorrectoFalla() throws Exception {
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin").param("password", "incorrecta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error"));
    }

    @Test
    void usuarioDesactivadoNoPuedeIniciarSesion() throws Exception {
        UsuarioCrearForm f = new UsuarioCrearForm();
        f.setNombreCompleto("Sin Acceso"); f.setUsuario("sinacceso");
        f.setContrasena(PW); f.setRol(Rol.CLIENTE); f.setActivo(false);
        service.crear(f);
        mockMvc.perform(post("/login").with(csrf())
                        .param("username", "sinacceso").param("password", PW))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error"));
    }

    @Test
    void usuarioDuplicadoRechazado() {
        UsuarioCrearForm f = new UsuarioCrearForm();
        f.setNombreCompleto("Duplicado"); f.setUsuario("admin");
        f.setContrasena(PW); f.setRol(Rol.CLIENTE); f.setActivo(true);
        assertThatThrownBy(() -> service.crear(f))
                .isInstanceOf(UsuarioService.UsuarioDuplicadoException.class);
    }

    @Test
    void noAdminNoPuedeAccederUsuarios() throws Exception {
        UsuarioCrearForm f = new UsuarioCrearForm();
        f.setNombreCompleto("Cliente Demo"); f.setUsuario("cliente1");
        f.setContrasena(PW); f.setRol(Rol.CLIENTE); f.setActivo(true);
        service.crear(f);
        var result = mockMvc.perform(post("/login").with(csrf())
                        .param("username", "cliente1").param("password", PW))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        mockMvc.perform(get("/usuarios").session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession()))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void meseroRecibe403ConPaginaPersonalizada() throws Exception {
        UsuarioCrearForm f = new UsuarioCrearForm();
        f.setNombreCompleto("Mesero Demo"); f.setUsuario("mesero1-test");
        f.setContrasena(PW); f.setRol(Rol.MESERO); f.setActivo(true);
        service.crear(f);
        var result = mockMvc.perform(post("/login").with(csrf())
                        .param("username", "mesero1-test").param("password", PW))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        mockMvc.perform(get("/usuarios").session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession()))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void pagina403PersonalizadaEnEspanolSinDetallesTecnicos() throws Exception {
        mockMvc.perform(get("/error/403"))
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Acceso denegado")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tu usuario no tiene permisos para acceder a esta sección")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Volver al panel")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("Whitelabel"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("stacktrace"))));
    }

    @Test
    void adminPuedeAccederUsuarios() throws Exception {
        var result = mockMvc.perform(post("/login").with(csrf())
                        .param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        mockMvc.perform(get("/usuarios")
                        .session((org.springframework.mock.web.MockHttpSession) result.getRequest().getSession()))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/listado"));
    }

    @Test
    void anonimoNoPuedeAccederUsuarios() throws Exception {
        mockMvc.perform(get("/usuarios")).andExpect(status().is3xxRedirection());
    }

    @Test
    void ultimoAdministradorNoSeDesactiva() {
        // En la BD de test solo existe el admin inicial como ADMINISTRADOR activo adicional
        // salvo que otros tests crearan mÃ¡s. Buscamos el id del admin y verificamos la protecciÃ³n
        // forzando el caso con un Ãºnico admin: si hay >1, no se puede generic afirmar.
        // Por eso probamos editar el admin a CLIENTE con conteo==1 en un escenario controlado:
        var admins = repo.findAll().stream()
                .filter(u -> u.getRol() == Rol.ADMINISTRADOR && u.isActivo())
                .toList();
        if (admins.size() == 1) {
            assertThatThrownBy(() -> service.cambiarEstado(admins.get(0).getId(), false))
                    .isInstanceOf(UsuarioService.UltimoAdministradorException.class);
            UsuarioEditarForm ef = new UsuarioEditarForm();
            ef.setNombreCompleto("Admin"); ef.setUsuario("admin");
            ef.setRol(Rol.CLIENTE); ef.setActivo(true);
            assertThatThrownBy(() -> service.editar(admins.get(0).getId(), ef))
                    .isInstanceOf(UsuarioService.UltimoAdministradorException.class);
        } else {
            // Con mÃ¡s de un admin, desactivar uno sÃ­ estÃ¡ permitido.
            Usuario admin0 = admins.get(0);
            service.cambiarEstado(admin0.getId(), false);
            assertThat(repo.findById(admin0.getId()).orElseThrow().isActivo()).isFalse();
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional void persistenciaTrasLimpiarContexto() {
        UsuarioCrearForm f = new UsuarioCrearForm();
        f.setNombreCompleto("Persistente"); f.setUsuario("persistente1");
        f.setContrasena(PW); f.setRol(Rol.MESERO); f.setActivo(true);
        service.crear(f);
        em.flush(); em.clear();
        var hallado = repo.findByUsuario("persistente1").orElseThrow();
        assertThat(hallado.getNombreCompleto()).isEqualTo("Persistente");
        assertThat(hallado.getRol()).isEqualTo(Rol.MESERO);
        assertThat(hallado.getContrasena()).isNotEqualTo(PW); // BCrypt
        assertThat(hallado.getContrasena().startsWith("$2")).isTrue();
    }
}
