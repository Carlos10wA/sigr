# Inventario de elementos de configuración

| ID | Componente | Ruta real | Responsable | Versión / ref | Estado |
|---|---|---|---|---|---|
| CFG-01 | pom.xml | `pom.xml` | Autor | Spring Boot 3.5.16, Java 21, H2 2.3.232, POI 5.2.5 | Revisado |
| CFG-02 | Maven Wrapper | `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` | ídem | Maven 3.9.11 | Revisado |
| CFG-03 | Config aplicación | `src/main/resources/application.properties` | ídem | H2 archivo, zona America/Bogota | Revisado |
| CFG-04 | Código fuente | `src/main/java/...` | ídem | 0.0.1-SNAPSHOT | Revisado |
| CFG-05 | Plantillas | `src/main/resources/templates/` | ídem | Usuarios, platos, pedidos, reservas, facturas, caja, reportes, configuracion | Revisado |
| CFG-06 | Pruebas | `src/test/java/...` | ídem | JUnit 5 (BD aislada, 69 pruebas) | Revisado |
| CFG-07 | Licencia | `LICENSE.txt` (MIT 2026) | ídem | Código propio; fotos/dependencias con sus licencias | Revisado |
| CFG-08 | Repositorio GitHub | https://github.com/Carlos10wA/sigr | ídem | — | Publicación pendiente de revisión |
| CFG-09 | Módulos negocio | pedidos, reservas, facturación, caja, reportes, Excel Cobros, seguimiento automático | ídem | 0.0.1-SNAPSHOT | Revisado (LB-SIGR-1.0 candidata) |
| CFG-10 | Clave admin inicial | variable de entorno `SIGR_ADMIN_PASSWORD` (no versionada) | ídem | — | Configurado vía PowerShell, no en Git |
| CFG-11 | Documentación LB-SIGR-1.0 | `docs/` (ver acta-linea-base) | ídem | — | Revisado |
| CFG-12 | Fotografías catálogo | `static/images/platos/*.jpg` (12), creditos en `docs/creditos-imagenes.md` | ídem | — | Revisado |
