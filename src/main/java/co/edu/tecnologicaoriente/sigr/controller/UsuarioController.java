package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm;
import co.edu.tecnologicaoriente.sigr.dto.UsuarioEditarForm;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) { this.service = service; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", service.listarTodos());
        return "usuarios/listado";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new UsuarioCrearForm());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("titulo", "Crear usuario");
        return "usuarios/formulario";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("form") UsuarioCrearForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Crear usuario");
            return "usuarios/formulario";
        }
        try {
            service.crear(form);
            flash.addFlashAttribute("mensaje", "Usuario creado correctamente.");
            return "redirect:/usuarios";
        } catch (UsuarioService.UsuarioDuplicadoException e) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Crear usuario");
            model.addAttribute("error", e.getMessage());
            return "usuarios/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Usuario u = service.buscarPorId(id);
        UsuarioEditarForm form = new UsuarioEditarForm();
        form.setNombreCompleto(u.getNombreCompleto());
        form.setUsuario(u.getUsuario());
        form.setRol(u.getRol());
        form.setActivo(u.isActivo());
        model.addAttribute("form", form);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("titulo", "Editar usuario");
        model.addAttribute("usuarioId", id);
        return "usuarios/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id,
                         @Valid @ModelAttribute("form") UsuarioEditarForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Editar usuario");
            model.addAttribute("usuarioId", id);
            return "usuarios/formulario";
        }
        try {
            service.editar(id, form);
            flash.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
            return "redirect:/usuarios";
        } catch (UsuarioService.UsuarioDuplicadoException | UsuarioService.UltimoAdministradorException e) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Editar usuario");
            model.addAttribute("usuarioId", id);
            model.addAttribute("error", e.getMessage());
            return "usuarios/formulario";
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                RedirectAttributes flash) {
        try {
            service.cambiarEstado(id, activo);
            flash.addFlashAttribute("mensaje", activo ? "Usuario activado." : "Usuario desactivado.");
        } catch (UsuarioService.UltimoAdministradorException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
