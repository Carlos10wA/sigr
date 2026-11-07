package co.edu.tecnologicaoriente.sigr.model;

public enum MetodoPago {
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA;

    public String nombreES() {
        return switch (this) {
            case EFECTIVO -> "Efectivo";
            case TARJETA -> "Tarjeta";
            case TRANSFERENCIA -> "Transferencia";
        };
    }
}
