package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.MesaConfiguracionForm;
import co.edu.tecnologicaoriente.sigr.dto.MesaForm;
import co.edu.tecnologicaoriente.sigr.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mesas")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
public class MesaController {

    private final ReservaService reservas;

    public MesaController(ReservaService reservas) { this.reservas = reservas; }

    @GetMapping
    public String listar(Model model) {
        var mesas = reservas.listarMesas();
        model.addAttribute("mesas", mesas);
        model.addAttribute("resumen", reservas.resumenMesas());
        MesaConfiguracionForm config = new MesaConfiguracionForm();
        config.setCantidadActivas((int) reservas.resumenMesas().activas());
        config.setCapacidadNuevas(4);
        model.addAttribute("config", config);
        model.addAttribute("activas", reservas.listarMesasActivas());
        return "mesas/listado";
    }

    @PostMapping("/configuracion")
    public String configuracion(@Valid @ModelAttribute("config") MesaConfiguracionForm form,
                                BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("mesas", reservas.listarMesas());
            model.addAttribute("resumen", reservas.resumenMesas());
            model.addAttribute("activas", reservas.listarMesasActivas());
            model.addAttribute("error", "Revise la cantidad y la capacidad.");
            return "mesas/listado";
        }
        try {
            String mensaje = reservas.ajustarCantidadActivas(form);
            flash.addFlashAttribute("mensaje", mensaje);
            return "redirect:/mesas";
        } catch (ReservaService.ReservaInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/mesas";
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activa, RedirectAttributes flash) {
        try {
            reservas.cambiarEstadoMesa(id, activa);
            flash.addFlashAttribute("mensaje", activa ? "Mesa reactivada." : "Mesa desactivada.");
        } catch (ReservaService.ReservaInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mesas";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("form", new MesaForm());
        model.addAttribute("titulo", "Nueva mesa");
        return "mesas/formulario";
    }

    @PostMapping("/nueva")
    public String crear(@Valid @ModelAttribute("form") MesaForm form, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nueva mesa");
            return "mesas/formulario";
        }
        try {
            reservas.crearMesa(form);
            flash.addFlashAttribute("mensaje", "Mesa creada correctamente.");
            return "redirect:/mesas";
        } catch (ReservaService.MesaDuplicadaException e) {
            model.addAttribute("titulo", "Nueva mesa");
            model.addAttribute("error", e.getMessage());
            return "mesas/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        var m = reservas.buscarMesa(id);
        MesaForm form = new MesaForm();
        form.setNombre(m.getNombre());
        form.setCapacidad(m.getCapacidad());
        form.setActiva(m.isActiva());
        model.addAttribute("form", form);
        model.addAttribute("titulo", "Editar mesa");
        model.addAttribute("mesaId", id);
        return "mesas/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("form") MesaForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Editar mesa");
            model.addAttribute("mesaId", id);
            return "mesas/formulario";
        }
        try {
            reservas.editarMesa(id, form);
            flash.addFlashAttribute("mensaje", "Mesa actualizada correctamente.");
            return "redirect:/mesas";
        } catch (ReservaService.MesaDuplicadaException | ReservaService.ReservaInvalidaException e) {
            model.addAttribute("titulo", "Editar mesa");
            model.addAttribute("mesaId", id);
            model.addAttribute("error", e.getMessage());
            return "mesas/formulario";
        }
    }

    @PostMapping("/cargar-demo")
    public String cargarDemo(RedirectAttributes flash) {
        int nuevas = reservas.cargarMesasDemo();
        flash.addFlashAttribute("mensaje", nuevas + " mesa(s) de demostración creada(s).");
        return "redirect:/reservas";
    }
}
