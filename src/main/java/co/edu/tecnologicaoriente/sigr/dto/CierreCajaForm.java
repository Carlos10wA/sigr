package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CierreCajaForm {

    @NotNull(message = "El efectivo contado es obligatorio.")
    @DecimalMin(value = "0.00", message = "El efectivo contado no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "El efectivo contado debe ser un valor monetario válido en COP.")
    private BigDecimal efectivoContado;

    @Size(max = 500, message = "La observación no puede superar 500 caracteres.")
    private String observacion;

    public BigDecimal getEfectivoContado() { return efectivoContado; }
    public void setEfectivoContado(BigDecimal v) { this.efectivoContado = v; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String v) { this.observacion = v; }
}
