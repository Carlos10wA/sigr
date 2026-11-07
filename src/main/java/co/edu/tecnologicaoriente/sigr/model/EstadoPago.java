package co.edu.tecnologicaoriente.sigr.model;

public enum EstadoPago {
    PENDIENTE_DE_PAGO,
    PAGADO;

    public String nombreES() {
        return switch (this) {
            case PENDIENTE_DE_PAGO -> "Pendiente de pago";
            case PAGADO -> "Pagado";
        };
    }
}
