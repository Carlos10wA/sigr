package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaForm {

    @NotBlank(message = "El nombre de la categoría es obligatorio.")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres.")
    private String nombre;

    private boolean activa = true;

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean v) { this.activa = v; }
}
