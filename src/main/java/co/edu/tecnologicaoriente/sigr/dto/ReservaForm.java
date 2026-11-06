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

    // Selección explícita 24 h (hora + minutos) sin depender del formato del navegador.
    // El controlador convierte estos campos a horaInicio/horaFin (LocalTime).
    @Min(value = 0, message = "La hora debe estar entre 00 y 23.")
    @Max(value = 23, message = "La hora debe estar entre 00 y 23.")
    private Integer horaInicioHora;

    @Min(value = 0, message = "Los minutos deben estar entre 00 y 59.")
    @Max(value = 59, message = "Los minutos deben estar entre 00 y 59.")
    private Integer horaInicioMinuto;

    @Min(value = 0, message = "La hora debe estar entre 00 y 23.")
    @Max(value = 23, message = "La hora debe estar entre 00 y 23.")
    private Integer horaFinHora;

    @Min(value = 0, message = "Los minutos deben estar entre 00 y 59.")
    @Max(value = 59, message = "Los minutos deben estar entre 00 y 59.")
    private Integer horaFinMinuto;

    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaInicio;

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
    public Integer getHoraInicioHora() { return horaInicioHora; }
    public void setHoraInicioHora(Integer v) { this.horaInicioHora = v; }
    public Integer getHoraInicioMinuto() { return horaInicioMinuto; }
    public void setHoraInicioMinuto(Integer v) { this.horaInicioMinuto = v; }
    public Integer getHoraFinHora() { return horaFinHora; }
    public void setHoraFinHora(Integer v) { this.horaFinHora = v; }
    public Integer getHoraFinMinuto() { return horaFinMinuto; }
    public void setHoraFinMinuto(Integer v) { this.horaFinMinuto = v; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime v) { this.horaInicio = v; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime v) { this.horaFin = v; }
    public Integer getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(Integer v) { this.cantidadPersonas = v; }
}
