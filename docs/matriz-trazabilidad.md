# Matriz de requisitos y trazabilidad (Etapa 1)

| Req ID | Requisito | Implementación | Prueba | Evidencia | Estado |
|---|---|---|---|---|---|
| RQ-01 | Java 21 + Spring Boot 3.5.x | pom.xml 3.5.16, toolchain 21 | `.\mvnw.cmd test` | BUILD SUCCESS | ✅ Etapa 1 |
| RQ-02 | MVC/Thymeleaf/Security/JPA/Validation/H2 | starters en pom.xml | contexto carga | log Spring Boot 3.5.16 | ✅ |
| RQ-03 | Maven Wrapper Windows | mvnw.cmd 3.9.11 | `.\mvnw.cmd -v` Java 21 | salida wrapper | ✅ |
| RQ-04 | Página inicial y login | HomeController + index/login | HomeControllerTest 4 tests | GET / 200, /login 200 | ✅ |
| RQ-05 | Usuarios/Menús/Pedidos/Reservas/Facturación/Caja | — | — | — | ⏳ Pendiente próximas etapas |
| RQ-06 | Línea base LB-SIGR-1.0 aprobada | Registro candidato | — | commit pendiente | ⏳ Pendiente aprobación |
