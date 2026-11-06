package co.edu.tecnologicaoriente.sigr.model;

public enum Rol {
    ADMINISTRADOR,
    MESERO,
    CLIENTE;

    /** Autoridad Spring Security correspondiente (ROLE_ + nombre). */
    public String authority() {
        return "ROLE_" + name();
    }
}
