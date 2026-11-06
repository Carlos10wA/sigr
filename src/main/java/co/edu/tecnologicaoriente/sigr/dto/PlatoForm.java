package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class PlatoForm {

    @NotBlank(message = "El nombre del plato es obligatorio.")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
    private String nombre;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 255, message = "La descripción debe ser breve (máx. 255 caracteres).")
    private String descripcion;

    @NotNull(message = "La categoría es obligatoria.")
    private Long categoriaId;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "El precio debe ser un valor monetario válido en COP.")
    private BigDecimal precio;

    @Size(max = 200, message = "La ruta de la fotografía es demasiado larga.")
    private String foto;

    private boolean disponible = true;
    private boolean activo = true;

    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long v) { this.categoriaId = v; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal v) { this.precio = v; }
    public String getFoto() { return foto; }
    public void setFoto(String v) { this.foto = v; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean v) { this.disponible = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
}
