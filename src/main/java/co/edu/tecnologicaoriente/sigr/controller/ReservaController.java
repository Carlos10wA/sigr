package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.dto.ReservaForm;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservas;

    public ReservaController(ReservaService reservas) { this.reservas = reservas; }

    private Usuario actual(Principal principal) {
        return reservas.resolverActual(principal.getName());
    }

    @GetMapping
    public String listar(Principal principal, Model model) {
        var lista = reservas.listarPara(actual(principal));
        model.addAttribute("reservas", lista);
        model.addAttribute("situaciones", reservas.situaciones(lista));
        model.addAttribute("actor", actual(principal));
        return "reservas/listado";
    }

    private void agregarDisponibilidad(ReservaForm form, Model model) {
        var inicio = reservas.resolverHora(form.getHoraInicioHora(), form.getHoraInicioMinuto());
        var fin = reservas.resolverHora(form.getHoraFinHora(), form.getHoraFinMinuto());
        boolean completo = form.getFecha() != null && inicio != null && fin != null && fin.isAfter(inicio)
                && form.getCantidadPersonas() != null && form.getCantidadPersonas() >= 1;
        var activas = reservas.listarMesasActivas();
        var conCapacidad = completo
                ? activas.stream().filter(m -> m.getCapacidad() >= form.getCantidadPersonas()).toList()
                : activas;
        List<co.edu.tecnologicaoriente.sigr.model.Mesa> disponibles = completo
                ? reservas.listarDisponibles(form.getFecha(), inicio, fin, form.getCantidadPersonas())
                : List.of();
        if (form.getMesaId() != null && disponibles.stream().noneMatch(m -> m.getId().equals(form.getMesaId()))) {
            form.setMesaId(null);
        }
        model.addAttribute("mesas", disponibles);
        model.addAttribute("hayBusqueda", completo);
        model.addAttribute("sinMesasConfiguradas", completo && activas.isEmpty());
        model.addAttribute("sinCapacidad", completo && !activas.isEmpty() && conCapacidad.isEmpty());
        model.addAttribute("sinDisponibles", completo && !conCapacidad.isEmpty() && disponibles.isEmpty());
    }

    @GetMapping("/nueva")
    public String nueva(@ModelAttribute("form") ReservaForm form, Model model) {
        // La mesa se elige después de fecha, horarios y personas: filtrar la disponibilidad.
        agregarDisponibilidad(form, model);
        model.addAttribute("titulo", "Nueva reserva");
        return "reservas/formulario";
    }

    @PostMapping("/nueva")
    public String crear(Principal principal, @Valid @ModelAttribute("form") ReservaForm form,
                        BindingResult result, Model model, RedirectAttributes flash) {
        // Convertir la selección explícita (hora + minutos 24 h) a LocalTime.
        if (form.getHoraInicioHora() != null && form.getHoraInicioMinuto() != null
                && form.getHoraInicioHora() >= 0 && form.getHoraInicioHora() <= 23
                && form.getHoraInicioMinuto() >= 0 && form.getHoraInicioMinuto() <= 59) {
            form.setHoraInicio(LocalTime.of(form.getHoraInicioHora(), form.getHoraInicioMinuto()));
        }
        if (form.getHoraFinHora() != null && form.getHoraFinMinuto() != null
                && form.getHoraFinHora() >= 0 && form.getHoraFinHora() <= 23
                && form.getHoraFinMinuto() >= 0 && form.getHoraFinMinuto() <= 59) {
            form.setHoraFin(LocalTime.of(form.getHoraFinHora(), form.getHoraFinMinuto()));
        }
        if (form.getHoraInicio() == null || form.getHoraFin() == null) {
            result.rejectValue("horaInicio", "horaInicio",
                    "Seleccione la hora de inicio (hora y minutos) en formato 24 horas.");
            result.rejectValue("horaFin", "horaFin",
                    "Seleccione la hora de fin (hora y minutos) en formato 24 horas.");
        }
        if (result.hasErrors()) {
            agregarDisponibilidad(form, model);
            model.addAttribute("titulo", "Nueva reserva");
            model.addAttribute("error", "Revise fecha, horarios (24 horas) y cantidad de personas.");
            return "reservas/formulario";
        }
        try {
            var creada = reservas.crear(actual(principal), form);
            flash.addFlashAttribute("mensaje", "Reserva creada correctamente.");
            return "redirect:/reservas/" + creada.getId();
        } catch (ReservaService.ReservaInvalidaException | ReservaService.ReservaNoDisponibleException e) {
            agregarDisponibilidad(form, model);
            model.addAttribute("titulo", "Nueva reserva");
            model.addAttribute("error", e.getMessage());
            return "reservas/formulario";
        }
    }

    @GetMapping("/{id}")
    public String detalle(Principal principal, @PathVariable Long id, Model model) {
        var reserva = reservas.buscarPara(actual(principal), id);
        model.addAttribute("reserva", reserva);
        model.addAttribute("situacion", reservas.situacion(reserva));
        model.addAttribute("actor", actual(principal));
        return "reservas/detalle";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(Principal principal, @PathVariable Long id, RedirectAttributes flash) {
        try {
            reservas.cancelar(actual(principal), id);
            flash.addFlashAttribute("mensaje", "Reserva cancelada correctamente.");
        } catch (ReservaService.ReservaInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservas/" + id;
    }
}
