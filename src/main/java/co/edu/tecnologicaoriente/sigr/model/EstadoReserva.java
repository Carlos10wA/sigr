package co.edu.tecnologicaoriente.sigr.model;

public enum EstadoReserva {
    CONFIRMADA,
    CANCELADA;

    public String nombreES() {
        return switch (this) {
            case CONFIRMADA -> "Confirmada";
            case CANCELADA -> "Cancelada";
        };
    }
}
