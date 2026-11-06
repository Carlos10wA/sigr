package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class PedidoLineaForm {

    private Long platoId;

    // Sin @NotNull: las filas vacías del formulario se ignoran en el servicio.
    // El servicio valida que cada línea con plato tenga cantidad 1..100.
    @Min(value = 1, message = "La cantidad debe ser positiva (mínimo 1).")
    @Max(value = 100, message = "La cantidad no puede superar 100.")
    private Integer cantidad = 1;

    public Long getPlatoId() { return platoId; }
    public void setPlatoId(Long platoId) { this.platoId = platoId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}
