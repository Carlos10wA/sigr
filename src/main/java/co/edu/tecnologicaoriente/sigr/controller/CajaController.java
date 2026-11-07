package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.AperturaCajaForm;
import co.edu.tecnologicaoriente.sigr.dto.CierreCajaForm;
import co.edu.tecnologicaoriente.sigr.dto.PagoForm;
import co.edu.tecnologicaoriente.sigr.model.MetodoPago;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.service.CajaService;
import co.edu.tecnologicaoriente.sigr.service.FacturacionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/caja")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
public class CajaController {

    private final CajaService caja;
    private final FacturacionService facturas;

    public CajaController(CajaService caja, FacturacionService facturas) {
        this.caja = caja; this.facturas = facturas;
    }

    private Usuario actual(Principal principal) { return caja.resolverActual(principal.getName()); }

    @GetMapping
    public String panel(Principal principal, Model model) {
        var abierta = caja.abierta().orElse(null);
        model.addAttribute("sesion", abierta);
        model.addAttribute("cerradas", caja.listarSesiones().stream()
                .filter(s -> s.getEstado() == co.edu.tecnologicaoriente.sigr.model.EstadoSesionCaja.CERRADA).toList());
        model.addAttribute("aperturaForm", new AperturaCajaForm());
        CierreCajaForm cierre = new CierreCajaForm();
        // Sin precarga: el campo empieza vacío para reflejar el conteo real.
        cierre.setEfectivoContado(null);
        model.addAttribute("cierreForm", cierre);
        if (abierta != null) {
            java.math.BigDecimal ef = java.math.BigDecimal.ZERO, tar = java.math.BigDecimal.ZERO, tra = java.math.BigDecimal.ZERO;
            for (var p : abierta.getPagos()) {
                switch (p.getMetodo()) {
                    case EFECTIVO -> ef = ef.add(p.getImporte());
                    case TARJETA -> tar = tar.add(p.getImporte());
                    case TRANSFERENCIA -> tra = tra.add(p.getImporte());
                }
            }
            model.addAttribute("cobrosEfectivo", ef);
            model.addAttribute("cobrosTarjeta", tar);
            model.addAttribute("cobrosTransferencia", tra);
            model.addAttribute("efectivoEsperado", abierta.getBaseInicial().add(ef));
        }
        model.addAttribute("metodos", MetodoPago.values());
        model.addAttribute("actor", actual(principal));
        return "caja/panel";
    }

    @PostMapping("/abrir")
    public String abrir(Principal principal, @Valid @ModelAttribute("aperturaForm") AperturaCajaForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Revise la base inicial.");
            return "redirect:/caja";
        }
        try {
            caja.abrir(actual(principal), form);
            flash.addFlashAttribute("mensaje", "Caja abierta correctamente.");
            return "redirect:/caja";
        } catch (CajaService.CajaInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/caja";
        }
    }

    @GetMapping("/pagos/nuevo")
    public String pagoForm(@RequestParam(required = false) Long comprobanteId, Model model) {
        PagoForm form = new PagoForm();
        form.setComprobanteId(comprobanteId);
        model.addAttribute("form", form);
        model.addAttribute("metodos", MetodoPago.values());
        return "caja/pago";
    }

    @PostMapping("/pagos")
    public String pagar(Principal principal, @Valid @ModelAttribute("form") PagoForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors() || form.getMetodo() == null) {
            model.addAttribute("metodos", MetodoPago.values());
            model.addAttribute("error", "Revise método, valor recibido o referencia.");
            return "caja/pago";
        }
        try {
            caja.registrarPago(actual(principal), form);
            flash.addFlashAttribute("mensaje", "Pago registrado correctamente.");
            return "redirect:/caja";
        } catch (CajaService.PagoDuplicadoException e) {
            flash.addFlashAttribute("error", "Ya está pagado; se abre el comprobante existente.");
            return "redirect:/facturas/" + form.getComprobanteId();
        } catch (CajaService.CajaInvalidaException e) {
            model.addAttribute("metodos", MetodoPago.values());
            model.addAttribute("error", e.getMessage());
            return "caja/pago";
        }
    }

    @PostMapping("/cerrar")
    public String cerrar(Principal principal, @Valid @ModelAttribute("cierreForm") CierreCajaForm form,
                         BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            flash.addFlashAttribute("error", "Revise el efectivo contado.");
            return "redirect:/caja";
        }
        try {
            caja.cerrar(actual(principal), form);
            flash.addFlashAttribute("mensaje", "Caja cerrada correctamente.");
            return "redirect:/caja";
        } catch (CajaService.CajaInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/caja";
        }
    }
}
