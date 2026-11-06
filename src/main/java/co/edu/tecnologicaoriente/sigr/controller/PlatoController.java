package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.service.DemoDataService;
import co.edu.tecnologicaoriente.sigr.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/platos")
public class PlatoController {

    private final MenuService menu;
    private final DemoDataService demo;

    public PlatoController(MenuService menu, DemoDataService demo) {
        this.menu = menu;
        this.demo = demo;
    }

    /** Catálogo para cualquier rol autenticado. */
    @GetMapping
    public String catalogo(@RequestParam(required = false) String q,
                           @RequestParam(required = false) Long categoriaId,
                           Model model) {
        model.addAttribute("platos", menu.catalogo(q, categoriaId));
        model.addAttribute("categorias", menu.listarCategoriasActivas());
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("categoriaId", categoriaId);
        // Para administradores, mostrar también accesos de gestión.
        model.addAttribute("vacio", menu.listarPlatosAdmin().isEmpty());
        return "platos/catalogo";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("plato", menu.buscarPlato(id));
        return "platos/detalle";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new PlatoForm());
        model.addAttribute("categorias", menu.listarCategoriasActivas());
        model.addAttribute("titulo", "Nuevo plato");
        return "platos/formulario";
    }

    @PostMapping("/nuevo")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public String crear(@Valid @ModelAttribute("form") PlatoForm form, BindingResult result,
                        Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", menu.listarCategoriasActivas());
            model.addAttribute("titulo", "Nuevo plato");
            return "platos/formulario";
        }
        try {
            menu.crearPlato(form);
            flash.addFlashAttribute("mensaje", "Plato creado correctamente.");
            return "redirect:/platos";
        } catch (MenuService.CategoriaInvalidaException e) {
            model.addAttribute("categorias", menu.listarCategoriasActivas());
            model.addAttribute("titulo", "Nuevo plato");
            model.addAttribute("error", e.getMessage());
            return "platos/formulario";
        }
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public String editarForm(@PathVariable Long id, Model model) {
        var p = menu.buscarPlato(id);
        PlatoForm form = new PlatoForm();
        form.setNombre(p.getNombre());
        form.setDescripcion(p.getDescripcion());
        form.setCategoriaId(p.getCategoria().getId());
        form.setPrecio(p.getPrecio());
        form.setFoto(p.getFoto());
        form.setDisponible(p.isDisponible());
        form.setActivo(p.isActivo());
        model.addAttribute("form", form);
        model.addAttribute("categorias", menu.listarCategoriasActivas());
        model.addAttribute("titulo", "Editar plato");
        model.addAttribute("platoId", id);
        return "platos/formulario";
    }

    @PostMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public String editar(@PathVariable Long id, @Valid @ModelAttribute("form") PlatoForm form,
                         BindingResult result, Model model, RedirectAttributes flash) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", menu.listarCategoriasActivas());
            model.addAttribute("titulo", "Editar plato");
            model.addAttribute("platoId", id);
            return "platos/formulario";
        }
        try {
            menu.editarPlato(id, form);
            flash.addFlashAttribute("mensaje", "Plato actualizado correctamente.");
            return "redirect:/platos";
        } catch (MenuService.CategoriaInvalidaException e) {
            model.addAttribute("categorias", menu.listarCategoriasActivas());
            model.addAttribute("titulo", "Editar plato");
            model.addAttribute("platoId", id);
            model.addAttribute("error", e.getMessage());
            return "platos/formulario";
        }
    }

    @PostMapping("/{id}/cambiar-estado")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
                                RedirectAttributes flash) {
        menu.cambiarEstadoPlato(id, activo);
        flash.addFlashAttribute("mensaje", activo ? "Plato activado." : "Plato desactivado.");
        return "redirect:/platos";
    }

    @PostMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public String disponibilidad(@PathVariable Long id, @RequestParam boolean disponible,
                                 RedirectAttributes flash) {
        try {
            menu.cambiarDisponibilidad(id, disponible);
            flash.addFlashAttribute("mensaje", disponible ? "Plato marcado disponible." : "Plato marcado no disponible.");
        } catch (MenuService.CategoriaInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/platos";
    }

    @PostMapping("/cargar-demo")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public String cargarDemo(RedirectAttributes flash) {
        var r = demo.cargarDemo();
        flash.addFlashAttribute("mensaje",
                "Datos de demostración listos: " + r.categoriasNuevas() + " categorías y "
                        + r.platosNuevos() + " platos nuevos (sin duplicar existentes).");
        return "redirect:/platos";
    }
}
