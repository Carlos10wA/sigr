package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PagoForm {

    @NotNull(message = "El comprobante es obligatorio.")
    private Long comprobanteId;

    @NotBlank(message = "El método de pago es obligatorio.")
    private String metodo;

    @DecimalMin(value = "0.00", message = "El valor recibido no puede ser negativo.")
    @Digits(integer = 10, fraction = 2, message = "El valor debe ser un valor monetario válido en COP.")
    private BigDecimal valorRecibido;

    @Size(max = 100, message = "La referencia no puede superar 100 caracteres.")
    private String referencia;

    public Long getComprobanteId() { return comprobanteId; }
    public void setComprobanteId(Long v) { this.comprobanteId = v; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String v) { this.metodo = v; }
    public BigDecimal getValorRecibido() { return valorRecibido; }
    public void setValorRecibido(BigDecimal v) { this.valorRecibido = v; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String v) { this.referencia = v; }
}
