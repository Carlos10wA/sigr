package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;

public class MesaForm {

    @NotBlank(message = "El nombre de la mesa es obligatorio.")
    @Size(max = 60, message = "El nombre no puede superar 60 caracteres.")
    private String nombre;

    @NotNull(message = "La capacidad es obligatoria.")
    @Min(value = 1, message = "La capacidad debe ser al menos 1.")
    @Max(value = 50, message = "La capacidad no puede superar 50.")
    private Integer capacidad;

    private boolean activa = true;

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer v) { this.capacidad = v; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean v) { this.activa = v; }
}
