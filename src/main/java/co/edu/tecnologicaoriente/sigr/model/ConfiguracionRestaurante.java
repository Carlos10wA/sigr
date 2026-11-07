package co.edu.tecnologicaoriente.sigr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "configuracion_restaurante")
public class ConfiguracionRestaurante {

    public static final String ZONA_POR_DEFECTO = "America/Bogota";

    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 60)
    private String pais = "Colombia";

    @Column(nullable = false, length = 60)
    private String ciudad = "Bogotá";

    @Column(length = 200)
    private String direccion = "";

    @Column(nullable = false, length = 60)
    private String zonaHoraria = ZONA_POR_DEFECTO;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public ConfiguracionRestaurante() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String zonaHoraria) { this.zonaHoraria = zonaHoraria; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime f) { this.fechaActualizacion = f; }
}
