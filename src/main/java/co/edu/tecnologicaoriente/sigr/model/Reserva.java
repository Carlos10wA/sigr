package co.edu.tecnologicaoriente.sigr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @Column(nullable = false, length = 100)
    private String clienteNombre;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false)
    private int cantidadPersonas;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
    }

    public Reserva() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Mesa getMesa() { return mesa; }
    public void setMesa(Mesa mesa) { this.mesa = mesa; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    public int getCantidadPersonas() { return cantidadPersonas; }
    public void setCantidadPersonas(int cantidadPersonas) { this.cantidadPersonas = cantidadPersonas; }
    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f) { this.fechaCreacion = f; }

    /**
     * Situación temporal (no cambia el estado persistido).
     * Cancelada siempre en rojo; si no, compara fecha+hora con America/Bogota:
     * antes del inicio = Próxima, inicio &lt;= ahora &lt; fin = En curso, ahora &gt;= fin = finalizado.
     */
    public SituacionReserva situacionEn(ZonedDateTime ahora) {
        if (estado == EstadoReserva.CANCELADA) {
            return SituacionReserva.CANCELADA;
        }
        ZonedDateTime bogota = ahora.withZoneSameInstant(ZoneId.of("America/Bogota"));
        ZonedDateTime inicio = ZonedDateTime.of(fecha, horaInicio, ZoneId.of("America/Bogota"));
        ZonedDateTime fin = ZonedDateTime.of(fecha, horaFin, ZoneId.of("America/Bogota"));
        if (bogota.isBefore(inicio)) {
            return SituacionReserva.PROXIMA;
        }
        if (bogota.isBefore(fin)) {
            return SituacionReserva.EN_CURSO;
        }
        return SituacionReserva.FINALIZADO;
    }
}
