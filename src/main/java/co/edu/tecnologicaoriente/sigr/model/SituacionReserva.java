package co.edu.tecnologicaoriente.sigr.model;

public enum SituacionReserva {
    PROXIMA,
    EN_CURSO,
    FINALIZADO,
    CANCELADA,
    CANCELADA_VENCIMIENTO;

    public String etiquetaES() {
        return switch (this) {
            case PROXIMA -> "Próxima";
            case EN_CURSO -> "En curso";
            case FINALIZADO -> "Horario finalizado";
            case CANCELADA -> "Cancelada";
            case CANCELADA_VENCIMIENTO -> "Cancelada por vencimiento";
        };
    }

    public String claseCss() {
        return switch (this) {
            case PROXIMA -> "situacion-proxima";
            case EN_CURSO -> "situacion-curso";
            case FINALIZADO -> "situacion-finalizado";
            case CANCELADA, CANCELADA_VENCIMIENTO -> "situacion-cancelada";
        };
    }
}
