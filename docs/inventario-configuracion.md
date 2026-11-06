# Inventario de elementos de configuración

| ID | Componente | Ruta real | Responsable | Versión / ref | Estado |
|---|---|---|---|---|---|
| CFG-01 | pom.xml | `pom.xml` | Carlos Alberto Ayala Larrahondo | Spring Boot 3.5.16, Java 21 | Revisado Etapa 1 |
| CFG-02 | Maven Wrapper | `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` | ídem | Maven 3.9.11 | Revisado |
| CFG-03 | Config aplicación | `src/main/resources/application.properties` | ídem | H2 archivo, zona America/Bogota | Revisado |
| CFG-04 | Código fuente | `src/main/java/...` | ídem | 0.0.1-SNAPSHOT | Revisado |
| CFG-05 | Plantillas | `src/main/resources/templates/` | ídem | Etapa 1 | Revisado |
| CFG-06 | Pruebas | `src/test/java/...` | ídem | JUnit 5 | Revisado |
| CFG-07 | Licencia | `LICENSE.txt` | ídem | Pendiente decisión autor | Pendiente |
| CFG-08 | Repositorio GitHub | — | ídem | — | Pendiente URL real |
| CFG-09 | Módulo usuarios (Etapa 2) | `model/`, `repository/`, `service/`, `controller/UsuarioController`, `security/DbUserDetailsService`, `config/DataInitializer` | ídem | 0.0.1-SNAPSHOT | Revisado Etapa 2 |
| CFG-10 | Vistas usuarios | `templates/usuarios/`, `templates/fragments/` | ídem | Etapa 2 | Revisado |
| CFG-11 | Clave admin inicial | variable de entorno `SIGR_ADMIN_PASSWORD` (no versionada) | ídem | — | Configurado vía PowerShell, no en Git |
