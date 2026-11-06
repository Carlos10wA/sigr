# Plan de pruebas y resultados — SIGR

## Etapa 1 (2026-11-05)
| Prueba | Comando | Resultado real |
|---|---|---|
| `SigrApplicationTests.contextLoads` | `.\mvnw.cmd test` | OK |
| `HomeControllerTest.homePermitida...` | `.\mvnw.cmd test` | OK (200, vista index) |
| `HomeControllerTest.loginAccesible` | `.\mvnw.cmd test` | OK (200, vista login) |
| `HomeControllerTest.loginConCredenciales...` | `.\mvnw.cmd test` | OK (redirección 3xx) |
| Arranque real | `.\mvnw.cmd spring-boot:run` | Tomcat en 8080, `Started SigrApplication` |
| Acceso HTTP | `Invoke-WebRequest http://localhost:8080/` y `/login` | 200 en ambos |

Resumen: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`.

## Pendiente (próximas etapas)
Autenticación/roles, platos, pedidos, reservas, facturación duplicada, caja/reportes, recorrido completo. Ver matriz de trazabilidad.
Estado: **pendiente de ejecución** (no inventado).
