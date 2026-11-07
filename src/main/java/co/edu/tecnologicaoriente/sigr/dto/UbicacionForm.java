package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;

public class UbicacionForm {

    @NotBlank(message = "El país es obligatorio.")
    @Size(max = 60, message = "El país no puede superar 60 caracteres.")
    private String pais;

    @NotBlank(message = "La ciudad es obligatoria.")
    @Size(max = 60, message = "La ciudad no puede superar 60 caracteres.")
    private String ciudad;

    @Size(max = 200, message = "La dirección no puede superar 200 caracteres.")
    private String direccion;

    @NotBlank(message = "La zona horaria es obligatoria.")
    private String zonaHoraria;

    public String getPais() { return pais; }
    public void setPais(String v) { this.pais = v; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String v) { this.ciudad = v; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }
    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String v) { this.zonaHoraria = v; }
}
