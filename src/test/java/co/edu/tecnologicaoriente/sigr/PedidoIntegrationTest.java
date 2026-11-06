package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.repository.PedidoRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PedidoIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired MenuService menu;
    @Autowired PedidoService pedidos;
    @Autowired UsuarioService usuarios;
    @Autowired PedidoRepository pedidoRepo;

    private static final String ADMIN_PW = "Admin1234";
    private static final String PW = "Mesero1234";

    private co.edu.tecnologicaoriente.sigr.model.Categoria categoria(String nombre) {
        CategoriaForm f = new CategoriaForm();
        f.setNombre(nombre); f.setActiva(true);
        return menu.crearCategoria(f);
    }

    private PlatoForm platoForm(String nombre, Long catId, String precio) {
        PlatoForm f = new PlatoForm();
        f.setNombre(nombre); f.setDescripcion("Desc " + nombre);
        f.setCategoriaId(catId); f.setPrecio(new BigDecimal(precio));
        f.setFoto("empanadas.jpg"); f.setDisponible(true); f.setActivo(true);
        return f;
    }

    private void crearUsuario(String user, Rol rol) {
        co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm f = new co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm();
        f.setNombreCompleto(user); f.setUsuario(user); f.setContrasena(PW); f.setRol(rol); f.setActivo(true);
        usuarios.crear(f);
    }

    private co.edu.tecnologicaoriente.sigr.model.Usuario buscarUsuario(String user) {
        // resolver vía servicio interno sin exponer contraseña
        return pedidos.resolverActual(user);
    }

    private PedidoForm formDe(Long platoId, int cantidad) {
        PedidoForm form = new PedidoForm();
        PedidoLineaForm l = new PedidoLineaForm();
        l.setPlatoId(platoId); l.setCantidad(cantidad);
        form.getLineas().add(l);
        PedidoLineaForm vacia = new PedidoLineaForm();
        vacia.setPlatoId(null); vacia.setCantidad(1);
        form.getLineas().add(vacia);
        return form;
    }

    @Test
    void crearCalculaTotalEnServidorConSnapshot() {
        var cat = categoria("Ped-cat-total");
        var plato = menu.crearPlato(platoForm("Empanadas pedido", cat.getId(), "12000"));
        crearUsuario("cliente-total", Rol.CLIENTE);
        var actor = buscarUsuario("cliente-total");

        var creado = pedidos.crear(actor, formDe(plato.getId(), 2));
        assertThat(creado.getTotal()).isEqualByComparingTo(new BigDecimal("24000.00"));
        assertThat(creado.getDetalles()).hasSize(1);
        assertThat(creado.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("12000"));
        assertThat(creado.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);

        // Precio histórico: cambiar precio del plato no altera el pedido.
        PlatoForm cambio = platoForm("Empanadas pedido", cat.getId(), "99999");
        menu.editarPlato(plato.getId(), cambio);
        var recargado = pedidoRepo.findById(creado.getId()).orElseThrow();
        assertThat(recargado.getTotal()).isEqualByComparingTo(new BigDecimal("24000.00"));
        assertThat(recargado.getDetalles().get(0).getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("12000"));
        assertThat(recargado.getDetalles().get(0).getNombrePlato()).isEqualTo("Empanadas pedido");
    }

    @Test
    void rechazaNoDisponibleDesactivadoYCantidadInvalida() {
        var cat = categoria("Ped-cat-val");
        var noDisp = menu.crearPlato(platoForm("No disponible", cat.getId(), "10000"));
        menu.cambiarDisponibilidad(noDisp.getId(), false);
        var desactivado = menu.crearPlato(platoForm("Desactivado", cat.getId(), "10000"));
        menu.cambiarEstadoPlato(desactivado.getId(), false);
        var bueno = menu.crearPlato(platoForm("Bueno", cat.getId(), "10000"));
        crearUsuario("cliente-val", Rol.CLIENTE);
        var actor = buscarUsuario("cliente-val");

        assertThatThrownBy(() -> pedidos.crear(actor, formDe(noDisp.getId(), 1)))
                .isInstanceOf(PedidoService.PedidoInvalidoException.class);
        assertThatThrownBy(() -> pedidos.crear(actor, formDe(desactivado.getId(), 1)))
                .isInstanceOf(PedidoService.PedidoInvalidoException.class);
        assertThatThrownBy(() -> pedidos.crear(actor, formDe(bueno.getId(), 0)))
                .isInstanceOf(PedidoService.PedidoInvalidoException.class);
        assertThatThrownBy(() -> pedidos.crear(actor, formDe(bueno.getId(), -2)))
                .isInstanceOf(PedidoService.PedidoInvalidoException.class);
        assertThatThrownBy(() -> pedidos.crear(actor, formDe(999999L, 1)))
                .isInstanceOf(PedidoService.PedidoInvalidoException.class);
    }

    @Test
    void transicionesValidasEInvalidas() {
        var cat = categoria("Ped-cat-est");
        var plato = menu.crearPlato(platoForm("Plato estados", cat.getId(), "15000"));
        crearUsuario("mesero-est", Rol.MESERO);
        var mesero = buscarUsuario("mesero-est");
        var pedido = pedidos.crear(mesero, formDe(plato.getId(), 1));

        assertThatThrownBy(() -> pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO))
                .isInstanceOf(PedidoService.TransicionInvalidaException.class);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.EN_PREPARACION);
        assertThat(pedidoRepo.findById(pedido.getId()).orElseThrow().getEstado()).isEqualTo(EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.LISTO);
        assertThatThrownBy(() -> pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.CANCELADO))
                .isInstanceOf(PedidoService.TransicionInvalidaException.class);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO);
        assertThatThrownBy(() -> pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.CANCELADO))
                .isInstanceOf(PedidoService.TransicionInvalidaException.class);
    }

    @Test
    void clienteSoloVeYCancelarLosSuyosEIdorDevuelve404() throws Exception {
        var cat = categoria("Ped-cat-idor");
        var plato = menu.crearPlato(platoForm("Plato idor", cat.getId(), "10000"));
        crearUsuario("cliente-uno", Rol.CLIENTE);
        crearUsuario("cliente-dos", Rol.CLIENTE);
        var uno = buscarUsuario("cliente-uno");
        var dos = buscarUsuario("cliente-dos");
        var propio = pedidos.crear(uno, formDe(plato.getId(), 1));
        var ajeno = pedidos.crear(dos, formDe(plato.getId(), 1));

        assertThat(pedidos.listarPara(uno)).extracting(p -> p.getId()).containsExactly(propio.getId());
        assertThatThrownBy(() -> pedidos.buscarPara(uno, ajeno.getId()))
                .isInstanceOf(PedidoService.PedidoNoEncontradoException.class);

        var sUno = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-uno").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sessionUno = (org.springframework.mock.web.MockHttpSession) sUno;
        // Detalle propio renderiza con datos (nombre, precio COP, total)
        mockMvc.perform(get("/pedidos/{id}", propio.getId()).session(sessionUno))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Plato idor")))
                .andExpect(content().string(containsString("COP")))
                .andExpect(content().string(containsString("Pendiente")));
        // Detalle ajeno manipulando ID: 404 sin mostrar datos ajenos
        mockMvc.perform(get("/pedidos/{id}", ajeno.getId()).session(sessionUno))
                .andExpect(status().isNotFound());
        // Lista solo muestra propio
        mockMvc.perform(get("/pedidos").session(sessionUno))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pedidos")));
        // Cliente no puede avanzar estado
        mockMvc.perform(post("/pedidos/{id}/cambiar-estado", propio.getId())
                        .with(csrf()).session(sessionUno).param("estado", "EN_PREPARACION"))
                .andExpect(status().is3xxRedirection());
        assertThat(pedidoRepo.findById(propio.getId()).orElseThrow().getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
    }

    @Test
    void meseroVeTodosYAdminGestionaTodos() throws Exception {
        var cat = categoria("Ped-cat-roles");
        var plato = menu.crearPlato(platoForm("Plato roles", cat.getId(), "10000"));
        crearUsuario("mesero-roles", Rol.MESERO);
        crearUsuario("cliente-roles", Rol.CLIENTE);
        var mesero = buscarUsuario("mesero-roles");
        pedidos.crear(buscarUsuario("cliente-roles"), formDe(plato.getId(), 1));
        assertThat(pedidos.listarPara(mesero)).isNotEmpty();

        var sMesero = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-roles").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sessionMesero = (org.springframework.mock.web.MockHttpSession) sMesero;
        mockMvc.perform(get("/pedidos/nuevo").session(sessionMesero))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nuevo pedido")))
                .andExpect(content().string(containsString("Plato roles")));
        mockMvc.perform(get("/pedidos").session(sessionMesero))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Pedidos")))
                .andExpect(content().string(containsString("cliente-roles")));

        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sessionAdmin = (org.springframework.mock.web.MockHttpSession) sAdmin;
        mockMvc.perform(get("/pedidos").session(sessionAdmin))
                .andExpect(status().isOk());
        // Anónimo no puede entrar
        mockMvc.perform(get("/pedidos")).andExpect(status().is3xxRedirection());
        // Formulario sin datos visibles para cliente ajeno no expone otros pedidos
        var sCliente = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-roles").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sessionCliente = (org.springframework.mock.web.MockHttpSession) sCliente;
        mockMvc.perform(post("/pedidos/nuevo").with(csrf()).session(sessionCliente)
                        .param("lineas[0].platoId", plato.getId().toString()).param("lineas[0].cantidad", "1")
                        .param("lineas[1].cantidad", "1").param("lineas[2].cantidad", "1"))
                .andExpect(status().is3xxRedirection());
    }
}
