package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class AperturaCajaForm {

    @NotNull(message = "La base inicial es obligatoria.")
    @DecimalMin(value = "0.00", message = "La base inicial no puede ser negativa.")
    @Digits(integer = 10, fraction = 2, message = "La base debe ser un valor monetario válido en COP.")
    private BigDecimal baseInicial;

    public BigDecimal getBaseInicial() { return baseInicial; }
    public void setBaseInicial(BigDecimal v) { this.baseInicial = v; }
}
