package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.PedidoForm;
import co.edu.tecnologicaoriente.sigr.dto.PedidoLineaForm;
import co.edu.tecnologicaoriente.sigr.model.EstadoPedido;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import co.edu.tecnologicaoriente.sigr.service.PedidoService;
import co.edu.tecnologicaoriente.sigr.service.FacturacionService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidos;
    private final MenuService menu;
    private final FacturacionService facturas;

    public PedidoController(PedidoService pedidos, MenuService menu, FacturacionService facturas) {
        this.pedidos = pedidos;
        this.menu = menu;
        this.facturas = facturas;
    }

    private Usuario actual(Principal principal) {
        return pedidos.resolverActual(principal.getName());
    }

    @GetMapping
    public String listar(Principal principal, Model model) {
        Usuario actor = actual(principal);
        model.addAttribute("pedidos", pedidos.listarPara(actor));
        model.addAttribute("actor", actor);
        return "pedidos/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
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
        if (result.hasErrors()) {
            model.addAttribute("platos", menu.listarDisponibles());
            model.addAttribute("titulo", "Nuevo pedido");
            model.addAttribute("error", "Revise las cantidades: deben ser positivas (1 a 100).");
            return "pedidos/formulario";
        }
        try {
            var creado = pedidos.crear(actual(principal), form);
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
        var pedido = pedidos.buscarPara(actual(principal), id);
        model.addAttribute("pedido", pedido);
        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("comprobante", facturas.porPedido(id).orElse(null));
        model.addAttribute("actor", actual(principal));
        return "pedidos/detalle";
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(Principal principal, @PathVariable Long id,
                                @RequestParam EstadoPedido estado, RedirectAttributes flash) {
        try {
            pedidos.cambiarEstado(actual(principal), id, estado);
            flash.addFlashAttribute("mensaje", "Estado actualizado a " + estado.nombreES() + ".");
        } catch (PedidoService.TransicionInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/pedidos/" + id;
    }
}
