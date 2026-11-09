package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import co.edu.tecnologicaoriente.sigr.service.PedidoService;
import co.edu.tecnologicaoriente.sigr.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoAutoEstadoTest {

    @Autowired MockMvc mockMvc;
    @Autowired MenuService menu;
    @Autowired PedidoService pedidos;
    @Autowired UsuarioService usuarios;

    private static final String ADMIN_PW = "Admin1234";
    private static final String PW = "Mesero1234";

    private co.edu.tecnologicaoriente.sigr.model.Categoria categoria(String nombre) {
        CategoriaForm f = new CategoriaForm();
        f.setNombre(nombre);
        f.setActiva(true);
        return menu.crearCategoria(f);
    }

    private void usuario(String nombre, Rol rol) {
        co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm f = new co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm();
        f.setNombreCompleto(nombre);
        f.setUsuario(nombre);
        f.setContrasena(PW);
        f.setRol(rol);
        f.setActivo(true);
        usuarios.crear(f);
    }

    private org.springframework.mock.web.MockHttpSession login(String usuario, String clave) throws Exception {
        return (org.springframework.mock.web.MockHttpSession)
                mockMvc.perform(post("/login").with(csrf()).param("username", usuario).param("password", clave))
                        .andExpect(status().is3xxRedirection())
                        .andReturn().getRequest().getSession();
    }

    @Test
    void estadoAutomaticoRespetaPropiedadYPermisos() throws Exception {
        var cat = categoria("Auto-cat");
        PlatoForm pf = new PlatoForm();
        pf.setNombre("Plato auto");
        pf.setDescripcion("Desc");
        pf.setCategoriaId(cat.getId());
        pf.setPrecio(new BigDecimal("12000"));
        pf.setFoto("empanadas.jpg");
        pf.setDisponible(true);
        pf.setActivo(true);
        var plato = menu.crearPlato(pf);
        usuario("cli-auto-a", Rol.CLIENTE);
        usuario("cli-auto-b", Rol.CLIENTE);
        usuario("mes-auto", Rol.MESERO);
        var clienteA = pedidos.resolverActual("cli-auto-a");
        PedidoForm form = new PedidoForm();
        var l = new PedidoLineaForm();
        l.setPlatoId(plato.getId());
        l.setCantidad(1);
        form.getLineas().add(l);
        var pedido = pedidos.crear(clienteA, form);

        var sesA = login("cli-auto-a", PW);
        mockMvc.perform(get("/pedidos/" + pedido.getId() + "/estado").session(sesA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.nombre").value("Pendiente"));

        // Cliente ajeno: 404 sin exponer el pedido.
        var sesB = login("cli-auto-b", PW);
        mockMvc.perform(get("/pedidos/" + pedido.getId() + "/estado").session(sesB))
                .andExpect(status().isNotFound());

        // Mesero y administrador conservan sus permisos existentes.
        var sesMesero = login("mes-auto", PW);
        mockMvc.perform(get("/pedidos/" + pedido.getId() + "/estado").session(sesMesero))
                .andExpect(status().isOk());
        var sesAdmin = login("admin", ADMIN_PW);
        mockMvc.perform(get("/pedidos/" + pedido.getId() + "/estado").session(sesAdmin))
                .andExpect(status().isOk());

        // El listado incluye el script de sondeo y la marca de estado.
        mockMvc.perform(get("/pedidos").session(sesA))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-estado-pedido")))
                .andExpect(content().string(containsString("actualizar-estado.js")));

        assertThat(pedido.getId()).isNotNull();
    }
}
