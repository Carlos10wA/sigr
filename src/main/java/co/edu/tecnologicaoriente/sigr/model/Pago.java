package co.edu.tecnologicaoriente.sigr.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id", nullable = false, unique = true)
    private Comprobante comprobante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sesion_id", nullable = false)
    private SesionCaja sesion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorRecibido;

    @Column(precision = 12, scale = 2)
    private BigDecimal cambio;

    @Column(length = 100)
    private String referencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    public Pago() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Comprobante getComprobante() { return comprobante; }
    public void setComprobante(Comprobante c) { this.comprobante = c; }
    public SesionCaja getSesion() { return sesion; }
    public void setSesion(SesionCaja s) { this.sesion = s; }
    public MetodoPago getMetodo() { return metodo; }
    public void setMetodo(MetodoPago m) { this.metodo = m; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal b) { this.importe = b; }
    public BigDecimal getValorRecibido() { return valorRecibido; }
    public void setValorRecibido(BigDecimal b) { this.valorRecibido = b; }
    public BigDecimal getCambio() { return cambio; }
    public void setCambio(BigDecimal b) { this.cambio = b; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String r) { this.referencia = r; }
    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario u) { this.responsable = u; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime f) { this.fechaHora = f; }
}
