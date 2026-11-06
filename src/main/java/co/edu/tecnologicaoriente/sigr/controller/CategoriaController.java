package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.model.Categoria;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
public class CategoriaController {

    private final MenuService service;

    public CategoriaController(MenuService service) { this.service = service; }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listarCategorias());
        return "categorias/listado";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("form", new CategoriaForm());
        model.addAttribute("titulo", "Nueva categoría");
        return "categorias/formulario";
    }

    @PostMapping("/nueva")
    public String crear(@Valid @ModelAttribute("form") CategoriaForm form, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nueva categoría");
            return "categorias/formulario";
        }
        try {
            service.crearCategoria(form);
            flash.addFlashAttribute("mensaje", "Categoría creada correctamente.");
            return "redirect:/categorias";
        } catch (MenuService.CategoriaDuplicadaException e) {
            model.addAttribute("titulo", "Nueva categoría");
            model.addAttribute("error", e.getMessage());
            return "categorias/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Categoria c = service.buscarCategoria(id);
        CategoriaForm form = new CategoriaForm();
        form.setNombre(c.getNombre());
        form.setActiva(c.isActiva());
        model.addAttribute("form", form);
        model.addAttribute("titulo", "Editar categoría");
        model.addAttribute("categoriaId", id);
        return "categorias/formulario";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("form") CategoriaForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Editar categoría");
            model.addAttribute("categoriaId", id);
            return "categorias/formulario";
        }
        try {
            service.editarCategoria(id, form);
            flash.addFlashAttribute("mensaje", "Categoría actualizada correctamente.");
            return "redirect:/categorias";
        } catch (MenuService.CategoriaDuplicadaException e) {
            model.addAttribute("titulo", "Editar categoría");
            model.addAttribute("categoriaId", id);
            model.addAttribute("error", e.getMessage());
            return "categorias/formulario";
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activa,
                                RedirectAttributes flash) {
        service.cambiarEstadoCategoria(id, activa);
        flash.addFlashAttribute("mensaje", activa ? "Categoría activada." : "Categoría desactivada.");
        return "redirect:/categorias";
    }
}
