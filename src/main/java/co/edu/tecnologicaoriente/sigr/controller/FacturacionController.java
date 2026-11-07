package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.service.FacturacionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/facturas")
public class FacturacionController {

    private final FacturacionService facturas;

    public FacturacionController(FacturacionService facturas) { this.facturas = facturas; }

    private Usuario actual(Principal principal) {
        return facturas.resolverActual(principal.getName());
    }

    @GetMapping
    public String listar(Principal principal, Model model) {
        model.addAttribute("comprobantes", facturas.listarPara(actual(principal)));
        model.addAttribute("actor", actual(principal));
        return "facturas/listado";
    }

    @GetMapping("/{id}")
    public String detalle(Principal principal, @PathVariable Long id, Model model) {
        model.addAttribute("comprobante", facturas.buscarPara(actual(principal), id));
        model.addAttribute("actor", actual(principal));
        return "facturas/detalle";
    }

    @PostMapping("/emitir/{pedidoId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMINISTRADOR','ROLE_MESERO')")
    public String emitir(Principal principal, @PathVariable Long pedidoId, RedirectAttributes flash) {
        try {
            var creado = facturas.emitir(actual(principal), pedidoId);
            flash.addFlashAttribute("mensaje", "Comprobante " + creado.getNumero() + " emitido (pendiente de pago).");
            return "redirect:/facturas/" + creado.getId();
        } catch (FacturacionService.YaFacturadoException e) {
            flash.addFlashAttribute("error", "Este pedido ya tiene un comprobante. Se abre el existente.");
            return "redirect:/facturas/" + e.getComprobanteId();
        } catch (FacturacionService.FacturacionInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidos/" + pedidoId;
        }
    }
}
