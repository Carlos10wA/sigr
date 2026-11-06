package co.edu.tecnologicaoriente.sigr.model;

public enum EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    /** Texto en español para pantallas. */
    public String nombreES() {
        return switch (this) {
            case PENDIENTE -> "Pendiente";
            case EN_PREPARACION -> "En preparación";
            case LISTO -> "Listo";
            case ENTREGADO -> "Entregado";
            case CANCELADO -> "Cancelado";
        };
    }
}
