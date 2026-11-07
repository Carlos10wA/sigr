package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.UbicacionForm;
import co.edu.tecnologicaoriente.sigr.service.ConfiguracionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configuracion")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
public class ConfiguracionController {

    private final ConfiguracionService configuracion;

    public ConfiguracionController(ConfiguracionService configuracion) { this.configuracion = configuracion; }

    @GetMapping
    public String ver(Model model) {
        var c = configuracion.actual();
        UbicacionForm form = new UbicacionForm();
        form.setPais(c.getPais());
        form.setCiudad(c.getCiudad());
        form.setDireccion(c.getDireccion());
        form.setZonaHoraria(c.getZonaHoraria());
        model.addAttribute("form", form);
        model.addAttribute("ciudades", configuracion.ciudades());
        model.addAttribute("titulo", "Configuración del restaurante");
        return "configuracion/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("form") UbicacionForm form, BindingResult result,
                          Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("ciudades", configuracion.ciudades());
            model.addAttribute("titulo", "Configuración del restaurante");
            model.addAttribute("error", "Revise país, ciudad, dirección y zona horaria.");
            return "configuracion/formulario";
        }
        try {
            configuracion.actualizar(form);
            flash.addFlashAttribute("mensaje", "Ubicación actualizada correctamente.");
            return "redirect:/configuracion";
        } catch (ConfiguracionService.ConfiguracionInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/configuracion";
        }
    }
}
