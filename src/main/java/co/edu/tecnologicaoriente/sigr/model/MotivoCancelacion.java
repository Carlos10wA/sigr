package co.edu.tecnologicaoriente.sigr.model;

public enum MotivoCancelacion {
    MANUAL,
    VENCIMIENTO;

    public String etiquetaES() {
        return switch (this) {
            case MANUAL -> "Cancelada";
            case VENCIMIENTO -> "Cancelada por vencimiento";
        };
    }
}
