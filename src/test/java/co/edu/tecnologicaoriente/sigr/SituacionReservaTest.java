package co.edu.tecnologicaoriente.sigr;

import co.edu.tecnologicaoriente.sigr.model.EstadoReserva;
import co.edu.tecnologicaoriente.sigr.model.Reserva;
import co.edu.tecnologicaoriente.sigr.model.SituacionReserva;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SituacionReservaTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private Reserva reserva(String fecha, String ini, String fin, EstadoReserva estado) {
        var r = new Reserva();
        r.setFecha(LocalDate.parse(fecha));
        r.setHoraInicio(LocalTime.parse(ini));
        r.setHoraFin(LocalTime.parse(fin));
        r.setEstado(estado);
        return r;
    }

    private ZonedDateTime zon(String iso) {
        return ZonedDateTime.parse(iso);
    }

    @Test
    void antesDelInicioEsProxima() {
        var r = reserva("2026-11-20", "12:00", "13:00", EstadoReserva.CONFIRMADA);
        assertThat(r.situacionEn(zon("2026-11-20T11:59:59-05:00"))).isEqualTo(SituacionReserva.PROXIMA);
    }

    @Test
    void inicioExactoEsEnCurso() {
        var r = reserva("2026-11-20", "12:00", "13:00", EstadoReserva.CONFIRMADA);
        assertThat(r.situacionEn(zon("2026-11-20T12:00:00-05:00"))).isEqualTo(SituacionReserva.EN_CURSO);
    }

    @Test
    void duranteEsEnCurso() {
        var r = reserva("2026-11-20", "12:00", "13:00", EstadoReserva.CONFIRMADA);
        assertThat(r.situacionEn(zon("2026-11-20T12:30:00-05:00"))).isEqualTo(SituacionReserva.EN_CURSO);
    }

    @Test
    void finExactoEsFinalizado() {
        var r = reserva("2026-11-20", "12:00", "13:00", EstadoReserva.CONFIRMADA);
        assertThat(r.situacionEn(zon("2026-11-20T13:00:00-05:00"))).isEqualTo(SituacionReserva.FINALIZADO);
        assertThat(r.situacionEn(zon("2026-11-20T13:00:01-05:00"))).isEqualTo(SituacionReserva.FINALIZADO);
    }

    @Test
    void canceladaSiempreRoja() {
        var r = reserva("2026-11-20", "12:00", "13:00", EstadoReserva.CANCELADA);
        assertThat(r.situacionEn(zon("2026-11-20T11:00:00-05:00"))).isEqualTo(SituacionReserva.CANCELADA);
        assertThat(r.situacionEn(zon("2026-11-20T12:30:00-05:00"))).isEqualTo(SituacionReserva.CANCELADA);
        assertThat(r.situacionEn(zon("2026-11-20T14:00:00-05:00"))).isEqualTo(SituacionReserva.CANCELADA);
    }

    @Test
    void canceladaPorVencimientoSeDistingue() {
        var r = reserva("2026-11-20", "12:00", "13:00", EstadoReserva.CANCELADA);
        r.setMotivoCancelacion(co.edu.tecnologicaoriente.sigr.model.MotivoCancelacion.VENCIMIENTO);
        assertThat(r.situacionEn(zon("2026-11-20T11:00:00-05:00"))).isEqualTo(SituacionReserva.CANCELADA_VENCIMIENTO);
        assertThat(r.getEstadoConMotivo()).isEqualTo("Cancelada por vencimiento");
        assertThat(SituacionReserva.CANCELADA_VENCIMIENTO.claseCss()).isEqualTo("situacion-cancelada");
        assertThat(SituacionReserva.CANCELADA_VENCIMIENTO.etiquetaES()).isEqualTo("Cancelada por vencimiento");
        // Colores nuevos: En curso verde, Próxima naranja.
        assertThat(SituacionReserva.EN_CURSO.claseCss()).isEqualTo("situacion-curso");
        assertThat(SituacionReserva.PROXIMA.claseCss()).isEqualTo("situacion-proxima");
    }

    @Test
    void etiquetasYClases() {
        assertThat(SituacionReserva.PROXIMA.etiquetaES()).isEqualTo("Próxima");
        assertThat(SituacionReserva.EN_CURSO.etiquetaES()).isEqualTo("En curso");
        assertThat(SituacionReserva.FINALIZADO.etiquetaES()).isEqualTo("Horario finalizado");
        assertThat(SituacionReserva.CANCELADA.etiquetaES()).isEqualTo("Cancelada");
    }
}
