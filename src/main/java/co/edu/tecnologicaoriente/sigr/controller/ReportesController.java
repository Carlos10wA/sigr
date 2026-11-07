package co.edu.tecnologicaoriente.sigr.controller;

import co.edu.tecnologicaoriente.sigr.service.ReporteService;
import co.edu.tecnologicaoriente.sigr.service.ReservaService;
import co.edu.tecnologicaoriente.sigr.util.CsvUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/reportes")
@PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
public class ReportesController {

    private final ReporteService reportes;
    private final ReservaService reservas;
    private final co.edu.tecnologicaoriente.sigr.service.CobrosExcelService excel;
    private final co.edu.tecnologicaoriente.sigr.service.ConfiguracionService configuracion;

    public ReportesController(ReporteService reportes, ReservaService reservas,
                              co.edu.tecnologicaoriente.sigr.service.CobrosExcelService excel,
                              co.edu.tecnologicaoriente.sigr.service.ConfiguracionService configuracion) {
        this.reportes = reportes; this.reservas = reservas; this.excel = excel; this.configuracion = configuracion;
    }

    @GetMapping
    public String inicio(Model model) {
        model.addAttribute("zona", reportes.zonaActual().getId());
        return "reportes/index";
    }

    @GetMapping("/cobros")
    public String cobros(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {
        model.addAttribute("zona", reportes.zonaActual().getId());
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        if (desde == null && hasta == null) return "reportes/cobros";
        try {
            model.addAttribute("resumen", reportes.cobros(desde, hasta));
        } catch (ReporteService.RangoInvalidoException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reportes/cobros";
    }

    @GetMapping("/cobros/csv")
    public ResponseEntity<String> cobrosCsv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        var resumen = reportes.cobros(desde, hasta);
        StringBuilder sb = new StringBuilder();
        sb.append("comprobante,pedido,fecha_pago,metodo,importe\n");
        for (var p : resumen.detalle()) {
            sb.append(CsvUtil.escapar(p.getComprobante().getNumero())).append(",")
              .append(p.getComprobante().getPedido().getId()).append(",")
              .append(CsvUtil.escapar(p.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))).append(",")
              .append(CsvUtil.escapar(p.getMetodo().nombreES())).append(",")
              .append(p.getImporte().toPlainString()).append("\n");
        }
        return csv(sb.toString(), "cobros-" + desde + "-" + hasta + ".csv");
    }

    @GetMapping("/cobros/xlsx")
    public ResponseEntity<byte[]> cobrosXlsx(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        var resumen = reportes.cobros(desde, hasta);
        var cfg = configuracion.actual();
        byte[] datos = excel.generar(resumen, desde, hasta, cfg.getCiudad(), cfg.getPais(), reportes.zonaActual().getId());
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        h.setContentDisposition(ContentDisposition.attachment().filename("cobros-" + desde + "-" + hasta + ".xlsx").build());
        return ResponseEntity.ok().headers(h).body(datos);
    }

    @GetMapping("/platos")
    public String platos(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {
        model.addAttribute("zona", reportes.zonaActual().getId());
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        if (desde == null && hasta == null) return "reportes/platos";
        try {
            model.addAttribute("platos", reportes.platosVendidos(desde, hasta));
        } catch (ReporteService.RangoInvalidoException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reportes/platos";
    }

    @GetMapping("/platos/csv")
    public ResponseEntity<String> platosCsv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        var lista = reportes.platosVendidos(desde, hasta);
        StringBuilder sb = new StringBuilder("plato,cantidad,importe\n");
        for (var p : lista) {
            sb.append(CsvUtil.escapar(p.nombre())).append(",").append(p.cantidad()).append(",").append(p.importe().toPlainString()).append("\n");
        }
        return csv(sb.toString(), "platos-" + desde + "-" + hasta + ".csv");
    }

    @GetMapping("/reservas")
    public String reservas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                           @RequestParam(required = false) Long mesaId,
                           Model model) {
        model.addAttribute("zona", reportes.zonaActual().getId());
        model.addAttribute("mesas", reservas.listarMesas());
        model.addAttribute("desde", desde); model.addAttribute("hasta", hasta); model.addAttribute("mesaId", mesaId);
        if (desde == null && hasta == null) return "reportes/reservas";
        try {
            model.addAttribute("resumen", reportes.reservas(desde, hasta, mesaId));
        } catch (ReporteService.RangoInvalidoException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reportes/reservas";
    }

    @GetMapping("/reservas/csv")
    public ResponseEntity<String> reservasCsv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                             @RequestParam(required = false) Long mesaId) {
        var resumen = reportes.reservas(desde, hasta, mesaId);
        StringBuilder sb = new StringBuilder("reserva,titular,mesa,fecha,horario,estado,motivo\n");
        for (var r : resumen.detalle()) {
            String motivo = r.getEstado() == co.edu.tecnologicaoriente.sigr.model.EstadoReserva.CONFIRMADA ? "—"
                    : r.getMotivoCancelacion() == null ? "Sin motivo registrado" : r.getMotivoCancelacion().etiquetaES();
            sb.append(r.getId()).append(",")
              .append(CsvUtil.escapar(r.getClienteNombre())).append(",")
              .append(CsvUtil.escapar(r.getMesa().getNombre())).append(",")
              .append(CsvUtil.escapar(r.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")))).append(",")
              .append(CsvUtil.escapar(r.getHoraInicio().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + "-" + r.getHoraFin().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))).append(",")
              .append(CsvUtil.escapar(r.getEstado().nombreES())).append(",")
              .append(CsvUtil.escapar(motivo)).append("\n");
        }
        return csv(sb.toString(), "reservas-" + desde + "-" + hasta + ".csv");
    }

    @GetMapping("/cierres")
    public String cierres(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                          Model model) {
        model.addAttribute("zona", reportes.zonaActual().getId());
        model.addAttribute("desde", desde); model.addAttribute("hasta", hasta);
        if (desde == null && hasta == null) return "reportes/cierres";
        try {
            model.addAttribute("cierres", reportes.cierres(desde, hasta));
        } catch (ReporteService.RangoInvalidoException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "reportes/cierres";
    }

    @GetMapping("/cierres/csv")
    public ResponseEntity<String> cierresCsv(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        var lista = reportes.cierres(desde, hasta);
        StringBuilder sb = new StringBuilder("sesion,apertura,cierre,base,efectivo,tarjeta,transferencia,total,esperado,contado,diferencia\n");
        for (var s : lista) {
            sb.append(s.getId()).append(",")
              .append(CsvUtil.escapar(s.getFechaApertura().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))).append(",")
              .append(CsvUtil.escapar(s.getFechaCierre().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))).append(",")
              .append(s.getBaseInicial().toPlainString()).append(",")
              .append(s.getCobrosEfectivo().toPlainString()).append(",")
              .append(s.getCobrosTarjeta().toPlainString()).append(",")
              .append(s.getCobrosTransferencia().toPlainString()).append(",")
              .append(s.getTotalCobrado().toPlainString()).append(",")
              .append(s.getEfectivoEsperado().toPlainString()).append(",")
              .append(s.getEfectivoContado().toPlainString()).append(",")
              .append(s.getDiferencia().toPlainString()).append("\n");
        }
        return csv(sb.toString(), "cierres-" + desde + "-" + hasta + ".csv");
    }

    private ResponseEntity<String> csv(String cuerpo, String nombre) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        h.setContentDisposition(ContentDisposition.attachment().filename(nombre).build());
        return ResponseEntity.ok().headers(h).body("﻿" + cuerpo);
    }
}
