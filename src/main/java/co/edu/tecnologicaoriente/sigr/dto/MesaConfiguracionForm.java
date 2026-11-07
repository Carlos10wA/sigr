package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

public class MesaConfiguracionForm {

    @NotNull(message = "La cantidad total de mesas activas es obligatoria.")
    @Min(value = 0, message = "La cantidad no puede ser negativa.")
    @Max(value = 100, message = "La cantidad no puede superar 100.")
    private Integer cantidadActivas;

    @NotNull(message = "La capacidad de las mesas nuevas es obligatoria.")
    @Min(value = 1, message = "La capacidad debe ser al menos 1.")
    @Max(value = 50, message = "La capacidad no puede superar 50.")
    private Integer capacidadNuevas;

    private List<Long> mesasADesactivar = new ArrayList<>();

    public Integer getCantidadActivas() { return cantidadActivas; }
    public void setCantidadActivas(Integer v) { this.cantidadActivas = v; }
    public Integer getCapacidadNuevas() { return capacidadNuevas; }
    public void setCapacidadNuevas(Integer v) { this.capacidadNuevas = v; }
    public List<Long> getMesasADesactivar() { return mesasADesactivar; }
    public void setMesasADesactivar(List<Long> v) { this.mesasADesactivar = v; }
}
