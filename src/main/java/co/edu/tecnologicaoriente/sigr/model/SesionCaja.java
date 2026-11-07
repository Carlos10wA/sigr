package co.edu.tecnologicaoriente.sigr.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sesiones_caja")
public class SesionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    @Column(nullable = false, length = 60)
    private String zonaHoraria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal baseInicial = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSesionCaja estado = EstadoSesionCaja.ABIERTA;

    private LocalDateTime fechaCierre;
    private String responsableCierre;
    private String zonaCierre;
    @Column(precision = 12, scale = 2)
    private BigDecimal efectivoEsperado;
    @Column(precision = 12, scale = 2)
    private BigDecimal efectivoContado;
    @Column(precision = 12, scale = 2)
    private BigDecimal diferencia;
    @Column(precision = 12, scale = 2)
    private BigDecimal cobrosEfectivo;
    @Column(precision = 12, scale = 2)
    private BigDecimal cobrosTarjeta;
    @Column(precision = 12, scale = 2)
    private BigDecimal cobrosTransferencia;
    @Column(precision = 12, scale = 2)
    private BigDecimal totalCobrado;
    @Column(length = 500)
    private String observacion;

    @OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pago> pagos = new ArrayList<>();

    public SesionCaja() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getResponsable() { return responsable; }
    public void setResponsable(Usuario responsable) { this.responsable = responsable; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime f) { this.fechaApertura = f; }
    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String z) { this.zonaHoraria = z; }
    public BigDecimal getBaseInicial() { return baseInicial; }
    public void setBaseInicial(BigDecimal b) { this.baseInicial = b; }
    public EstadoSesionCaja getEstado() { return estado; }
    public void setEstado(EstadoSesionCaja estado) { this.estado = estado; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime f) { this.fechaCierre = f; }
    public String getResponsableCierre() { return responsableCierre; }
    public void setResponsableCierre(String r) { this.responsableCierre = r; }
    public String getZonaCierre() { return zonaCierre; }
    public void setZonaCierre(String z) { this.zonaCierre = z; }
    public BigDecimal getEfectivoEsperado() { return efectivoEsperado; }
    public void setEfectivoEsperado(BigDecimal b) { this.efectivoEsperado = b; }
    public BigDecimal getEfectivoContado() { return efectivoContado; }
    public void setEfectivoContado(BigDecimal b) { this.efectivoContado = b; }
    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal b) { this.diferencia = b; }
    public BigDecimal getCobrosEfectivo() { return cobrosEfectivo; }
    public void setCobrosEfectivo(BigDecimal b) { this.cobrosEfectivo = b; }
    public BigDecimal getCobrosTarjeta() { return cobrosTarjeta; }
    public void setCobrosTarjeta(BigDecimal b) { this.cobrosTarjeta = b; }
    public BigDecimal getCobrosTransferencia() { return cobrosTransferencia; }
    public void setCobrosTransferencia(BigDecimal b) { this.cobrosTransferencia = b; }
    public BigDecimal getTotalCobrado() { return totalCobrado; }
    public void setTotalCobrado(BigDecimal b) { this.totalCobrado = b; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String o) { this.observacion = o; }
    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }
}
