package co.edu.tecnologicaoriente.sigr.model;

public enum EstadoSesionCaja {
    ABIERTA,
    CERRADA;

    public String nombreES() {
        return switch (this) {
            case ABIERTA -> "Abierta";
            case CERRADA -> "Cerrada";
        };
    }
}
