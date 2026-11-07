package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.*;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class ReportesIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ReporteService reportes;
    @Autowired MenuService menu;
    @Autowired PedidoService pedidos;
    @Autowired FacturacionService facturas;
    @Autowired CajaService caja;
    @Autowired UsuarioService usuarios;
    @Autowired ReservaService reservas;

    private static final String ADMIN_PW = "Admin1234";
    private static final String PW = "Mesero1234";
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

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
    void escenario66000CobrosPlatosYCierres() throws Exception {
        var cat = categoria("Rep-cat");
        var bandeja = menu.crearPlato(platoForm("Bandejas paisas", cat.getId(), "28000"));
        var cafe = menu.crearPlato(platoForm("Cafés", cat.getId(), "5000"));
        crearUsuario("mesero-rep", Rol.MESERO);
        crearUsuario("cliente-rep", Rol.CLIENTE);
        var cliente = facturas.resolverActual("cliente-rep");
        var mesero = facturas.resolverActual("mesero-rep");
        var pedido = pedidos.crear(cliente, formDoble(bandeja.getId(), 2, cafe.getId(), 2));
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.LISTO);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO);
        var compPagado = facturas.emitir(mesero, pedido.getId());
        // Otro comprobante pendiente (sin pago) para excluir.
        PedidoForm form2 = new PedidoForm();
        var l = new PedidoLineaForm(); l.setPlatoId(bandeja.getId()); l.setCantidad(1);
        form2.getLineas().add(l);
        var pedidoPendiente = pedidos.crear(cliente, form2);
        pedidos.cambiarEstado(mesero, pedidoPendiente.getId(), EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedidoPendiente.getId(), EstadoPedido.LISTO);
        pedidos.cambiarEstado(mesero, pedidoPendiente.getId(), EstadoPedido.ENTREGADO);
        facturas.emitir(mesero, pedidoPendiente.getId());

        var admin = caja.resolverActual("admin");
        var ap = new AperturaCajaForm();
        ap.setBaseInicial(new BigDecimal("100000"));
        caja.abrir(admin, ap);
        var pago = new PagoForm();
        pago.setComprobanteId(compPagado.getId()); pago.setMetodo("EFECTIVO");
        pago.setValorRecibido(new BigDecimal("70000"));
        caja.registrarPago(admin, pago);
        var cierre = new CierreCajaForm();
        cierre.setEfectivoContado(new BigDecimal("166000"));
        caja.cerrar(admin, cierre);

        LocalDate hoy = LocalDate.now(BOGOTA);
        var cobros = reportes.cobros(hoy.minusDays(1), hoy.plusDays(1));
        assertThat(cobros.cantidad()).isEqualTo(1);
        assertThat(cobros.total()).isEqualByComparingTo(new BigDecimal("66000.00"));
        assertThat(cobros.efectivo()).isEqualByComparingTo(new BigDecimal("66000.00"));
        assertThat(cobros.tarjeta()).isEqualByComparingTo(new BigDecimal("0.00"));
        var platos = reportes.platosVendidos(hoy.minusDays(1), hoy.plusDays(1));
        assertThat(platos).extracting(p -> p.nombre()).containsExactly("Bandejas paisas", "Cafés");
        assertThat(platos.get(0).cantidad()).isEqualTo(2);
        var cierres = reportes.cierres(hoy.minusDays(1), hoy.plusDays(1));
        assertThat(cierres).hasSize(1);
        assertThat(cierres.get(0).getBaseInicial()).isEqualByComparingTo(new BigDecimal("100000.00"));
        assertThat(cierres.get(0).getCobrosEfectivo()).isEqualByComparingTo(new BigDecimal("66000.00"));
        assertThat(cierres.get(0).getEfectivoEsperado()).isEqualByComparingTo(new BigDecimal("166000.00"));

        var sAdmin = mockMvc.perform(post("/login").with(csrf()).param("username", "admin").param("password", ADMIN_PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var ses = (org.springframework.mock.web.MockHttpSession) sAdmin;
        mockMvc.perform(get("/reportes/cobros").param("desde", hoy.minusDays(1).toString()).param("hasta", hoy.plusDays(1).toString()).session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("$66.000,00 COP")))
                .andExpect(content().string(containsString("FAC-")));
        mockMvc.perform(get("/reportes/cobros/csv").param("desde", hoy.minusDays(1).toString()).param("hasta", hoy.plusDays(1).toString()).session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FAC-")))
                .andExpect(content().string(containsString("66000")));
        // Permisos
        crearUsuario("mesero-rep2", Rol.MESERO);
        var sMesero = mockMvc.perform(post("/login").with(csrf()).param("username", "mesero-rep2").param("password", PW))
                .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession();
        var sesMesero = (org.springframework.mock.web.MockHttpSession) sMesero;
        mockMvc.perform(get("/reportes").session(sesMesero)).andExpect(status().isForbidden());
        mockMvc.perform(get("/reportes/cobros").session(sesMesero)).andExpect(status().isForbidden());
        // Rangos inválidos
        mockMvc.perform(get("/reportes/cobros").param("desde", hoy.toString()).param("hasta", hoy.minusDays(1).toString()).session(ses))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no puede ser anterior")));
        mockMvc.perform(get("/reportes/reservas").param("desde", hoy.minusDays(5).toString()).param("hasta", hoy.minusDays(1).toString()).session(ses))
                .andExpect(status().isOk());
    }

    @Test
    void reservasTotalesYMotivos() {
        var mf = new MesaForm();
        mf.setNombre("Mesa rep"); mf.setCapacidad(4); mf.setActiva(true);
        var mesa = reservas.crearMesa(mf);
        crearUsuario("cliente-repres", Rol.CLIENTE);
        var actor = reservas.resolverActual("cliente-repres");
        LocalDate dia = LocalDate.now(BOGOTA).plusDays(1);
        var rf = new ReservaForm();
        rf.setMesaId(mesa.getId()); rf.setFecha(dia);
        rf.setHoraInicio(java.time.LocalTime.parse("12:00")); rf.setHoraFin(java.time.LocalTime.parse("13:00"));
        rf.setCantidadPersonas(2);
        var r1 = reservas.crear(actor, rf);
        var rf2 = new ReservaForm();
        rf2.setMesaId(mesa.getId()); rf2.setFecha(dia);
        rf2.setHoraInicio(java.time.LocalTime.parse("14:00")); rf2.setHoraFin(java.time.LocalTime.parse("15:00"));
        rf2.setCantidadPersonas(2);
        var r2 = reservas.crear(actor, rf2);
        reservas.cancelar(actor, r2.getId());
        var resumen = reportes.reservas(dia, dia, mesa.getId());
        assertThat(resumen.confirmadas()).isEqualTo(1);
        assertThat(resumen.manuales()).isEqualTo(1);
    }
}
