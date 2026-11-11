package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import co.edu.tecnologicaoriente.sigr.service.PedidoService;
import co.edu.tecnologicaoriente.sigr.service.FacturacionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private static final Logger log = LoggerFactory.getLogger(PedidoController.class);

    private final PedidoService pedidos;
    private final MenuService menu;
    private final FacturacionService facturas;

    public PedidoController(PedidoService pedidos, MenuService menu, FacturacionService facturas) {
        this.pedidos = pedidos;
        this.menu = menu;
        this.facturas = facturas;
    }

    private Usuario actual(Principal principal) {
        if (principal == null) {
            return null;
        }
        try {
            return pedidos.resolverActual(principal.getName());
        } catch (RuntimeException e) {
            log.warn("No se pudo resolver el usuario actual: {}", e.getMessage());
            return null;
        }
    }

    @GetMapping
    public String listar(Principal principal, Model model) {
        Usuario actor = actual(principal);
        if (actor == null) {
            return "redirect:/login";
        }
        model.addAttribute("pedidos", pedidos.listarPara(actor));
        model.addAttribute("actor", actor);
        return "pedidos/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Principal principal, Model model) {
        if (actual(principal) == null) {
            return "redirect:/login";
        }
        PedidoForm form = new PedidoForm();
        for (int i = 0; i < 3; i++) {
            form.getLineas().add(new PedidoLineaForm());
        }
        model.addAttribute("form", form);
        model.addAttribute("platos", menu.listarDisponibles());
        model.addAttribute("titulo", "Nuevo pedido");
        return "pedidos/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(Principal principal, @Valid @ModelAttribute("form") PedidoForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        Usuario actor = actual(principal);
        if (actor == null) {
            return "redirect:/login";
        }
        if (result.hasErrors()) {
            model.addAttribute("platos", menu.listarDisponibles());
            model.addAttribute("titulo", "Nuevo pedido");
            model.addAttribute("error", "Revise las cantidades: deben ser positivas (1 a 100).");
            return "pedidos/formulario";
        }
        try {
            var creado = pedidos.crear(actor, form);
            flash.addFlashAttribute("mensaje", "Pedido creado correctamente.");
            return "redirect:/pedidos/" + creado.getId();
        } catch (PedidoService.PedidoInvalidoException e) {
            model.addAttribute("platos", menu.listarDisponibles());
            model.addAttribute("titulo", "Nuevo pedido");
            model.addAttribute("error", e.getMessage());
            return "pedidos/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalle(Principal principal, @PathVariable Long id, Model model) {
        Usuario actor = actual(principal);
        if (actor == null) {
            return "redirect:/login";
        }
        var pedido = pedidos.buscarPara(actor, id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("comprobante", facturas.porPedido(id).orElse(null));
        model.addAttribute("actor", actor);
        return "pedidos/detalle";
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(Principal principal, @PathVariable Long id,
                                @RequestParam EstadoPedido estado, RedirectAttributes flash) {
        Usuario actor = actual(principal);
        if (actor == null) {
            return "redirect:/login";
        }
        try {
            pedidos.cambiarEstado(actor, id, estado);
            flash.addFlashAttribute("mensaje", "Estado actualizado a " + estado.nombreES() + ".");
        } catch (PedidoService.TransicionInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error al cambiar estado del pedido {}", id, e);
            flash.addFlashAttribute("error", "No se pudo actualizar el estado del pedido. Intente de nuevo.");
        }
        return "redirect:/pedidos/" + id;
    }

    /** Solo el estado en texto para polleo automático del listado/detalle (sin recargar la página). */
    @GetMapping("/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> estado(Principal principal, @PathVariable Long id) {
        Usuario actor = actual(principal);
        if (actor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var pedido = pedidos.buscarPara(actor, id);
        return ResponseEntity.ok(Map.of("id", pedido.getId(), "estado", pedido.getEstado().name(),
                "nombre", pedido.getEstado().nombreES()));
    }
}
