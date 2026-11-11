package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoCambioEstadoRegresionTest {

    @Autowired MockMvc mockMvc;
    @Autowired MenuService menu;
    @Autowired PedidoService pedidos;
    @Autowired UsuarioService usuarios;

    private static final String PW = "Mesero1234";

    private co.edu.tecnologicaoriente.sigr.model.Categoria categoria(String nombre) {
        CategoriaForm f = new CategoriaForm(); f.setNombre(nombre); f.setActiva(true);
        return menu.crearCategoria(f);
    }

    private void usuario(String nombre, Rol rol) {
        co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm f = new co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm();
        f.setNombreCompleto(nombre); f.setUsuario(nombre); f.setContrasena(PW); f.setRol(rol); f.setActivo(true);
        usuarios.crear(f);
    }

    @Test
    void sinSesionCambioEstadoNoProduceError500() throws Exception {
        // Sin sesión, el cambio de estado redirige a login en lugar de mostrar "Ocurrió un error".
        mockMvc.perform(post("/pedidos/1/cambiar-estado").with(csrf()).param("estado", "ENTREGADO"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void transicionListoAEntregadoRenderizaSin500() throws Exception {
        var cat = categoria("Reg-cat");
        PlatoForm pf = new PlatoForm();
        pf.setNombre("Plato reg"); pf.setDescripcion("Desc"); pf.setCategoriaId(cat.getId());
        pf.setPrecio(new BigDecimal("12000")); pf.setFoto("empanadas.jpg"); pf.setDisponible(true); pf.setActivo(true);
        var plato = menu.crearPlato(pf);
        usuario("mes-reg", Rol.MESERO);
        var mesero = pedidos.resolverActual("mes-reg");
        PedidoForm form = new PedidoForm();
        var l = new PedidoLineaForm(); l.setPlatoId(plato.getId()); l.setCantidad(1);
        form.getLineas().add(l);
        var pedido = pedidos.crear(mesero, form);
        pedidos.cambiarEstado(mesero, pedido.getId(), co.edu.tecnologicaoriente.sigr.model.EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedido.getId(), co.edu.tecnologicaoriente.sigr.model.EstadoPedido.LISTO);

        org.springframework.mock.web.MockHttpSession ses = (org.springframework.mock.web.MockHttpSession)
                mockMvc.perform(post("/login").with(csrf()).param("username", "mes-reg").param("password", PW))
                        .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();

        mockMvc.perform(post("/pedidos/{id}/cambiar-estado", pedido.getId()).with(csrf()).session(ses).param("estado", "ENTREGADO"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/pedidos/{id}", pedido.getId()).session(ses))
                .andExpect(status().isOk());
        // sondeo de estado no devuelve 500
        mockMvc.perform(get("/pedidos/{id}/estado", pedido.getId()).session(ses))
                .andExpect(status().isOk());
    }
}
