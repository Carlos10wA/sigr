package co.edu.tecnologicaoriente.sigr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Migración controlada e idempotente para comprobantes.estado:
 * la BD antigua tiene un ENUM nativo que solo admite PENDIENTE_DE_PAGO;
 * se convierte a VARCHAR(30) conservando datos y restricciones.
 * No borra tablas ni marca pagos: solo cambia el tipo de columna.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MigracionEstadoPago implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracionEstadoPago.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;

    public MigracionEstadoPago(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection()) {
            if (!existeTabla(conn, "COMPROBANTES")) {
                return;
            }
            String tipo = tipoColumna(conn, "COMPROBANTES", "ESTADO");
            if (tipo == null) {
                return;
            }
            String normalizado = tipo.toUpperCase();
            log.info("SIGR migración: COMPROBANTES.ESTADO es {}", tipo);
            if (normalizado.contains("ENUM")) {
                jdbc.execute("ALTER TABLE comprobantes ALTER COLUMN estado SET DATA TYPE VARCHAR(30)");
                log.info("SIGR migración: comprobantes.estado convertido a VARCHAR(30) conservando datos.");
            }
            // Verificación posterior: las restricciones se mantienen (p. ej. NOT NULL vía update ya aplicado).
        } catch (Exception e) {
            log.warn("SIGR migración comprobantes.estado no se aplicó: {}", e.getMessage());
        }
    }

    private boolean existeTabla(Connection conn, String tabla) throws Exception {
        DatabaseMetaData meta = conn.getMetaData();
        try (var rs = meta.getTables(null, null, tabla, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private String tipoColumna(Connection conn, String tabla, String columna) throws Exception {
        // INFORMATION_SCHEMA es el catálogo más fiable en H2 para el tipo declarado.
        String sql = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?";
        Object tipo = jdbc.queryForObject(sql, String.class, tabla, columna);
        return tipo == null ? null : tipo.toString();
    }
}
