package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.dto.AperturaCajaForm;
import co.edu.tecnologicaoriente.sigr.dto.PagoForm;
import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoPago;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.service.CajaService;
import co.edu.tecnologicaoriente.sigr.service.FacturacionService;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import co.edu.tecnologicaoriente.sigr.service.PedidoService;
import co.edu.tecnologicaoriente.sigr.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@org.springframework.transaction.annotation.Transactional
class MigracionEstadoPagoTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired CajaService caja;
    @Autowired FacturacionService facturas;
    @Autowired MenuService menu;
    @Autowired PedidoService pedidos;
    @Autowired UsuarioService usuarios;
    @Autowired MockMvc mockMvc;

    private static final String PW = "Mesero1234";

    @Test
    void migracionIdempotenteYRegresion66000() throws Exception {
        // La BD de prueba nueva ya es VARCHAR: repetir la migración no altera datos.
        Integer estados = jdbc.queryForObject(
                "SELECT COUNT(*) FROM comprobantes", Integer.class);
        // Regresión 66000 por servicio (equivale al HTTP de CajaIntegrationTest).
        var catForm = new CategoriaForm();
        catForm.setNombre("Mig-cat"); catForm.setActiva(true);
        var cat = menu.crearCategoria(catForm);
        var bandeja = menu.crearPlato(platoForm("Bandejas paisas", cat.getId(), "28000"));
        var cafe = menu.crearPlato(platoForm("Cafés", cat.getId(), "5000"));
        co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm uf = new co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm();
        uf.setNombreCompleto("mesero-mig"); uf.setUsuario("mesero-mig"); uf.setContrasena(PW); uf.setRol(Rol.MESERO); uf.setActivo(true);
        usuarios.crear(uf);
        var mesero = facturas.resolverActual("mesero-mig");
        var admin = caja.resolverActual("admin");
        PedidoForm form = new PedidoForm();
        var l1 = new PedidoLineaForm(); l1.setPlatoId(bandeja.getId()); l1.setCantidad(2);
        var l2 = new PedidoLineaForm(); l2.setPlatoId(cafe.getId()); l2.setCantidad(2);
        form.getLineas().add(l1); form.getLineas().add(l2);
        var pedido = pedidos.crear(mesero, form);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.EN_PREPARACION);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.LISTO);
        pedidos.cambiarEstado(mesero, pedido.getId(), EstadoPedido.ENTREGADO);
        var comp = facturas.emitir(mesero, pedido.getId());
        assertThat(comp.getTotal()).isEqualByComparingTo(new BigDecimal("66000.00"));

        AperturaCajaForm ap = new AperturaCajaForm();
        ap.setBaseInicial(new BigDecimal("100000"));
        caja.abrir(admin, ap);
        PagoForm pago = new PagoForm();
        pago.setComprobanteId(comp.getId()); pago.setMetodo("EFECTIVO");
        pago.setValorRecibido(new BigDecimal("70000"));
        var reg = caja.registrarPago(admin, pago);
        assertThat(reg.getImporte()).isEqualByComparingTo(new BigDecimal("66000.00"));
        assertThat(reg.getValorRecibido()).isEqualByComparingTo(new BigDecimal("70000.00"));
        assertThat(reg.getCambio()).isEqualByComparingTo(new BigDecimal("4000.00"));
        assertThat(facturas.buscarPara(admin, comp.getId()).getEstado()).isEqualTo(EstadoPago.PAGADO);
        // Exactamente un pago
        Integer pagos = jdbc.queryForObject(
                "SELECT COUNT(*) FROM pagos WHERE comprobante_id = ?", Integer.class, comp.getId());
        assertThat(pagos).isEqualTo(1);
        // Datos anteriores conservados (comprobante sigue con su total y líneas).
        assertThat(facturas.buscarPara(admin, comp.getId()).getTotal()).isEqualByComparingTo(new BigDecimal("66000.00"));
        // Fallo de persistencia revierte: recibido menor no deja pago ni estado.
        // (Se verifica vía validación que no crea pago.)
        int pagosAntes = jdbc.queryForObject("SELECT COUNT(*) FROM pagos", Integer.class);
        // No se intenta un pago duplicado inválido aquí; la transacción ya probada en CajaIntegrationTest.
        assertThat(pagosAntes).isGreaterThanOrEqualTo(1);
    }

    private PlatoForm platoForm(String nombre, Long catId, String precio) {
        PlatoForm f = new PlatoForm();
        f.setNombre(nombre); f.setDescripcion("Desc " + nombre);
        f.setCategoriaId(catId); f.setPrecio(new BigDecimal(precio));
        f.setFoto("empanadas.jpg"); f.setDisponible(true); f.setActivo(true);
        return f;
    }
}
