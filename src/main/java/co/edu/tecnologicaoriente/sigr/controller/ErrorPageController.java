package co.edu.tecnologicaoriente.sigr.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Página 403 personalizada en español.
 * Mantiene el estado HTTP 403 y no expone detalles técnicos.
 * Se reenvía aquí desde el accessDeniedHandler de Spring Security.
 */
@Controller
public class ErrorPageController {

    @GetMapping("/error/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accesoDenegado() {
        return "error/403";
    }
}
