package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.repository.ComprobanteRepository;
import co.edu.tecnologicaoriente.sigr.service.FacturacionService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FacturacionIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired MenuService menu;
    @Autowired PedidoService pedidos;
    @Autowired FacturacionService facturas;
    @Autowired UsuarioService usuarios;
    @Autowired ComprobanteRepository repo;

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

    private PedidoForm formDoble(Long p1, int c1, Long p2, int c2) {
        PedidoForm form = new PedidoForm();
        var l1 = new PedidoLineaForm(); l1.setPlatoId(p1); l1.setCantidad(c1);
        var l2 = new PedidoLineaForm(); l2.setPlatoId(p2); l2.setCantidad(c2);
        form.getLineas().add(l1); form.getLineas().add(l2);
        var vacia = new PedidoLineaForm(); vacia.setPlatoId(null); vacia.setCantidad(1);
        form.getLineas().add(vacia);
        return form;
    }

    @Test
    void emitirRequiereEntregadoYPendientePago() {
        var cat = categoria("Fact-cat-estado");
        var plato = menu.crearPlato(platoForm("Plato fact", cat.getId(), "10000"));
        crearUsuario("mesero-fact", Rol.MESERO);
        var mesero = facturas.resolverActual("mesero-fact");
        var pedido = pedidos.crear(mesero, formDe(plato.getId(), 2));
        assertThatThrownBy(() -> facturas.emitir(mesero, pedido.getId()))
                .isInstanceOf(FacturacionService.FacturacionInvalidaException.class);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.EN_PREPARACION);
        assertThatThrownBy(() -> facturas.emitir(mesero, pedido.getId()))
                .isInstanceOf(FacturacionService.FacturacionInvalidaException.class);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.LISTO);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO);
        var c = facturas.emitir(mesero, pedido.getId());
        assertThat(c.getNumero()).startsWith("FAC-");
        assertThat(c.getTotal()).isEqualByComparingTo(new BigDecimal("20000.00"));
        assertThat(c.getEstado().nombreES()).isEqualTo("Pendiente de pago");
        assertThat(c.getClienteNombre()).isEqualTo(pedido.getUsuario().getNombreCompleto());
        assertThat(c.getLineas()).hasSize(1);
        assertThat(c.getLineas().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("20000"));
    }

    @Test
    void duplicadoSeImpideYReabreExistente() throws Exception {
        var cat = categoria("Fact-cat-dup");
        var plato = menu.crearPlato(platoForm("Plato dup", cat.getId(), "10000"));
        crearUsuario("mesero-dup", Rol.MESERO);
        var mesero = facturas.resolverActual("mesero-dup");
        var pedido = pedidos.crear(mesero, formDe(plato.getId(), 1));
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.LISTO);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO);
        var primero = facturas.emitir(mesero, pedido.getId());
        assertThatThrownBy(() -> facturas.emitir(mesero, pedido.getId()))
                .isInstanceOf(FacturacionService.YaFacturadoException.class)
                .hasMessageContaining("ya tiene un comprobante");
        assertThat(repo.findByPedido_Id(pedido.getId()).orElseThrow().getId()).isEqualTo(primero.getId());
        assertThat(repo.count()).isGreaterThan(0);
        // Doble clic vía HTTP: informa y redirige al existente sin crear otro.
        var s = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-dup").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) s;
        long antes = repo.count();
        mockMvc.perform(post("/facturas/emitir/{pid}", pedido.getId()).with(csrf()).session(ses))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/facturas/" + primero.getId()));
        assertThat(repo.count()).isEqualTo(antes);
    }

    @Test
    void precioHistoricoYTotal66Mil() throws Exception {
        var cat = categoria("Fact-cat-hist");
        var bandeja = menu.crearPlato(platoForm("Bandejas paisas", cat.getId(), "28000"));
        var cafe = menu.crearPlato(platoForm("Cafés", cat.getId(), "5000"));
        crearUsuario("cliente-hist", Rol.CLIENTE);
        crearUsuario("mesero-hist", Rol.MESERO);
        var cliente = facturas.resolverActual("cliente-hist");
        var mesero = facturas.resolverActual("mesero-hist");
        var pedido = pedidos.crear(cliente, formDoble(bandeja.getId(), 2, cafe.getId(), 2));
        assertThat(pedido.getTotal()).isEqualByComparingTo(new BigDecimal("66000.00"));
        // Cambiar precio actual no altera el comprobante que se emita (snapshot del pedido).
        menu.editarPlato(bandeja.getId(), platoForm("Bandejas paisas", cat.getId(), "99999"));
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.LISTO);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO);
        var c = facturas.emitir(mesero, pedido.getId());
        assertThat(c.getTotal()).isEqualByComparingTo(new BigDecimal("66000.00"));
        assertThat(c.getLineas()).extracting(l -> l.getNombrePlato())
                .containsExactlyInAnyOrder("Bandejas paisas", "Cafés");
        // Cliente solo puede consultar el suyo; ajeno 404; cliente no puede emitir.
        var sCliente = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-hist").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesCliente = (org.springframework.mock.web.MockHttpSession) sCliente;
        mockMvc.perform(get("/facturas/{id}", c.getId()).session(sesCliente))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comprobante de venta — Sin validez fiscal")))
                .andExpect(content().string(containsString("$66.000,00 COP")))
                .andExpect(content().string(containsString("Bandejas paisas")))
                .andExpect(content().string(containsString("Pendiente de pago")));
        crearUsuario("cliente-ajeno", Rol.CLIENTE);
        var sAjeno = mockMvc.perform(post("/login").with(csrf()).param("username", "cliente-ajeno").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesAjeno = (org.springframework.mock.web.MockHttpSession) sAjeno;
        mockMvc.perform(get("/facturas/{id}", c.getId()).session(sesAjeno))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/facturas/emitir/{pid}", pedido.getId()).with(csrf()).session(sesAjeno))
                .andExpect(status().isForbidden());
        // Listado solo propio
        mockMvc.perform(get("/facturas").session(sesCliente))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FAC-")));
        mockMvc.perform(get("/facturas").session(sesAjeno))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("FAC-"))));
        // Pedido facturado no se puede cancelar ni editar estado.
        assertThatThrownBy(() -> pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.CANCELADO))
                .isInstanceOf(PedidoService.TransicionInvalidaException.class);
    }

    @Test
    void rechazaCanceladosYPendientes() {
        var cat = categoria("Fact-cat-rech");
        var plato = menu.crearPlato(platoForm("Plato rech", cat.getId(), "10000"));
        crearUsuario("mesero-rech", Rol.MESERO);
        var mesero = facturas.resolverActual("mesero-rech");
        var p1 = pedidos.crear(mesero, formDe(plato.getId(), 1));
        pedidos.cambiarEstado(mesero, p1.getId(), EstadoPedido.CANCELADO);
        assertThatThrownBy(() -> facturas.emitir(mesero, p1.getId()))
                .isInstanceOf(FacturacionService.FacturacionInvalidaException.class);
    }
}
