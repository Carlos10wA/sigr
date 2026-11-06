package co.edu.tecnologicaoriente.sigr.dto;

import co.edu.tecnologicaoriente.sigr.model.Rol;
import jakarta.validation.constraints.*;

public class UsuarioCrearForm {

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String nombreCompleto;

    @NotBlank(message = "El usuario es obligatorio.")
    @Size(min = 3, max = 60, message = "El usuario debe tener entre 3 y 60 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Solo letras, números, punto, guion y guion bajo.")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 8, max = 72, message = "La contraseña debe tener mínimo 8 caracteres.")
    private String contrasena;

    @NotNull(message = "El rol es obligatorio.")
    private Rol rol;

    private boolean activo = true;

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String v) { this.nombreCompleto = v; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String v) { this.usuario = v; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String v) { this.contrasena = v; }
    public Rol getRol() { return rol; }
    public void setRol(Rol v) { this.rol = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
}
