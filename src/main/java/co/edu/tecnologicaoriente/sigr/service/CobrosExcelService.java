package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.service.ReporteService.ResumenCobros;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class CobrosExcelService {

    public byte[] generar(ResumenCobros resumen, LocalDate desde, LocalDate hasta,
                          String ciudad, String pais, String zona) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet hoja = wb.createSheet("Cobros");
            // Presentación sin colores, encabezados en negrita, bordes discretos.
            CellStyle tituloStyle = wb.createCellStyle();
            tituloStyle.setFont(bold(wb, 14));
            CellStyle cabeceraStyle = wb.createCellStyle();
            cabeceraStyle.setFont(bold(wb, 11));
            cabeceraStyle.setBorderTop(BorderStyle.THIN);
            cabeceraStyle.setBorderBottom(BorderStyle.THIN);
            cabeceraStyle.setBorderLeft(BorderStyle.THIN);
            cabeceraStyle.setBorderRight(BorderStyle.THIN);
            CellStyle textoStyle = wb.createCellStyle();
            textoStyle.setBorderTop(BorderStyle.THIN);
            textoStyle.setBorderBottom(BorderStyle.THIN);
            textoStyle.setBorderLeft(BorderStyle.THIN);
            textoStyle.setBorderRight(BorderStyle.THIN);
            CellStyle dineroStyle = wb.createCellStyle();
            dineroStyle.cloneStyleFrom(textoStyle);
            dineroStyle.setDataFormat(wb.createDataFormat().getFormat("\"$\"#,##0.00"));
            CellStyle fechaStyle = wb.createCellStyle();
            fechaStyle.cloneStyleFrom(textoStyle);
            fechaStyle.setDataFormat(wb.createDataFormat().getFormat("dd/MM/yyyy hh:mm"));
            CellStyle totalStyle = wb.createCellStyle();
            totalStyle.cloneStyleFrom(dineroStyle);
            totalStyle.setFont(bold(wb, 11));

            DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int r = 0;
            var titulo = hoja.createRow(r++).createCell(0);
            titulo.setCellValue("SIGR — Reporte de cobros");
            titulo.setCellType(CellType.STRING);
            titulo.setCellStyle(tituloStyle);
            fila(hoja, r++, "Ubicación", ciudad + ", " + pais);
            fila(hoja, r++, "Periodo consultado", desde.format(fechaFmt) + " a " + hasta.format(fechaFmt));
            fila(hoja, r++, "Moneda", "COP");
            fila(hoja, r++, "Zona horaria", zona);
            r++; // blanco
            fila(hoja, r++, "Total cobrado", resumen.total());
            fila(hoja, r++, "Comprobantes pagados", resumen.cantidad());
            fila(hoja, r++, "Cobros en efectivo", resumen.efectivo());
            fila(hoja, r++, "Cobros por tarjeta", resumen.tarjeta());
            fila(hoja, r++, "Cobros por transferencia", resumen.transferencia());
            r++;
            int cabeceraFila = r;
            String[] cabeceras = {"Comprobante", "Pedido", "Fecha de pago", "Método", "Importe"};
            var filaCab = hoja.createRow(r++);
            for (int i = 0; i < cabeceras.length; i++) {
                var c = filaCab.createCell(i);
                c.setCellValue(cabeceras[i]);
                c.setCellType(CellType.STRING);
                c.setCellStyle(cabeceraStyle);
            }
            hoja.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(cabeceraFila, cabeceraFila, 0, 4));
            for (var p : resumen.detalle()) {
                var f = hoja.createRow(r++);
                var c0 = f.createCell(0); c0.setCellValue(p.getComprobante().getNumero()); c0.setCellType(CellType.STRING); c0.setCellStyle(textoStyle);
                var c1 = f.createCell(1); c1.setCellValue(p.getComprobante().getPedido().getId()); c1.setCellType(CellType.STRING); c1.setCellStyle(textoStyle);
                var c2 = f.createCell(2);
                c2.setCellValue(java.sql.Timestamp.valueOf(p.getFechaHora()));
                c2.setCellType(CellType.NUMERIC);
                c2.setCellStyle(fechaStyle);
                var c3 = f.createCell(3); c3.setCellValue(p.getMetodo().nombreES()); c3.setCellType(CellType.STRING); c3.setCellStyle(textoStyle);
                var c4 = f.createCell(4); c4.setCellValue(p.getImporte().doubleValue()); c4.setCellType(CellType.NUMERIC); c4.setCellStyle(dineroStyle);
            }
            var filaTotal = hoja.createRow(r++);
            var et = filaTotal.createCell(3);
            et.setCellValue("Total");
            et.setCellType(CellType.STRING);
            et.setCellStyle(totalStyle);
            var val = filaTotal.createCell(4);
            val.setCellValue(resumen.total().doubleValue());
            val.setCellType(CellType.NUMERIC);
            val.setCellStyle(totalStyle);
            r++;
            var nota = hoja.createRow(r++).createCell(0);
            nota.setCellValue("Incluye pagos registrados durante el periodo consultado. La base inicial de caja no forma parte de los cobros");
            nota.setCellType(CellType.STRING);

            for (int i = 0; i < 5; i++) {
                hoja.autoSizeColumn(i);
            }
            // Columnas ajustadas para que la impresión no corte columnas.

            try (var out = new ByteArrayOutputStream()) {
                wb.write(out);
                return out.toByteArray();
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel", e);
        }
    }

    private void fila(Sheet hoja, int n, String etiqueta, Object valor) {
        var f = hoja.createRow(n);
        var a = f.createCell(0);
        a.setCellValue(etiqueta);
        a.setCellType(CellType.STRING);
        var b = f.createCell(1);
        if (valor instanceof BigDecimal bd) {
            b.setCellValue(bd.doubleValue());
            b.setCellType(CellType.NUMERIC);
        } else if (valor instanceof Number num) {
            b.setCellValue(num.longValue());
            b.setCellType(CellType.NUMERIC);
        } else {
            b.setCellValue(String.valueOf(valor));
            b.setCellType(CellType.STRING);
        }
    }

    private Font bold(Workbook wb, int size) {
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) size);
        return f;
    }
}
