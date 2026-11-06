package co.edu.tecnologicaoriente.sigr.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservaForm {

    @NotNull(message = "La mesa es obligatoria.")
    private Long mesaId;

    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String clienteNombre;

    @NotNull(message = "La fecha es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "La hora de inicio es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaFin;

    @NotNull(message = "La cantidad de personas es obligatoria.")
    @Min(value = 1, message = "Debe ser al menos 1 persona.")
    @Max(value = 50, message = "No puede superar 50 personas.")
    private Integer cantidadPersonas;

    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long v) { this.mesaId = v; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String v) { this.clienteNombre = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime v) { this.horaInicio = v; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime v) { this.horaFin = v; }
    public Integer getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(Integer v) { this.cantidadPersonas = v; }
}
