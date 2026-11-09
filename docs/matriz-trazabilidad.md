# Matriz de requisitos y trazabilidad (Etapa 1)

| Req ID | Requisito | Implementación | Prueba | Evidencia | Estado |
|---|---|---|---|---|---|
| RQ-01 | Java 21 + Spring Boot 3.5.x | pom.xml 3.5.16, toolchain 21 | `.\mvnw.cmd test` | BUILD SUCCESS | ✅ Etapa 1 |
| RQ-02 | MVC/Thymeleaf/Security/JPA/Validation/H2 | starters en pom.xml | contexto carga | log Spring Boot 3.5.16 | ✅ |
| RQ-03 | Maven Wrapper Windows | mvnw.cmd 3.9.11 | `.\mvnw.cmd -v` Java 21 | salida wrapper | ✅ |
| RQ-04 | Página inicial y login | HomeController + index/login | HomeControllerTest 4 tests | GET / 200, /login 200 | ✅ |
| RQ-05 | Usuarios JPA + roles + permisos + último admin + 403 personalizada | Usuario/DbUserDetailsService/SecurityConfig/error/403 | UsuarioIntegrationTest 11 casos | `Tests run: 14` | ✅ Etapa 2 + ajuste 403 |
| RQ-06 | Menús + categorías + catálogo + demo idempotente + fotos locales | MenuService/DemoDataService/PlatoController/CategoriaController + @EntityGraph + error.html + fotos CC (`docs/creditos-imagenes.md`) | MenuIntegrationTest 9 casos | `Tests run: 23` | ✅ Etapa 3 + ajuste + fotos reales |
| RQ-07 | Pedidos: disponibles, cantidades positivas, precio histórico, total servidor BigDecimal, transiciones | Pedido/DetallePedido/PedidoService + @EntityGraph + IDOR 404 | PedidoIntegrationTest 5 casos | `Tests run: 28` | ✅ Pedidos |
| RQ-08 | Reservas + configuración + indicadores + colores + vencimiento | Mesa/Reserva/ReservaService + anti-superposición + IDOR 404 + listas horarias + `listarDisponibles` + bloqueo pesimista + `ajustarCantidadActivas` + `SituacionReserva` + scheduler Bogota | ReservaIntegrationTest 16 + SituacionReservaTest 7 | `Tests run: 51` | ✅ Reservas + claridad + disponibilidad + configuración + indicadores + vencimiento |
| RQ-09 | Facturación académica (único por entregado, histórico, pendiente de pago, imprimible) | Comprobante/ComprobanteLinea/FacturacionService + bloqueo + única | FacturacionIntegrationTest 4 casos | `Tests run: 55` | ✅ Facturación |
| RQ-10 | Ubicación del restaurante (ciudad única + solo lectura, copia en comprobante, bloqueo de zona) | ConfiguracionRestaurante/ConfiguracionService + snapshot en Comprobante | UbicacionIntegrationTest 4 casos | `Tests run: 59` | ✅ Ubicación + corrección Cali |
| RQ-11 | Caja y pagos (sesión única, pago completo único, cierre con diferencia) | SesionCaja/Pago/CajaService + bloqueo + única | CajaIntegrationTest 4 casos | `Tests run: 63` | ✅ Caja y pagos |
| RQ-12 | Migración ENUM EstadoPago → VARCHAR (idempotente, sin borrado) | MigracionEstadoPago + columnDefinition VARCHAR(30) | MigracionEstadoPagoTest 1 caso | `Tests run: 64` | ✅ Migración |
| RQ-13 | Claridad de cierre de Caja (resumen automático, diferencia estimada, sin precarga) | CajaController resumen + JS diferencia | CajaIntegrationTest 5 casos | `Tests run: 65` | ✅ Cierre claro |
| RQ-14 | Reportes (cobros, platos, reservas, cierres, CSV, imprimible, Excel Cobros .xlsx) | ReporteService/ReportesController/CobrosExcelService + CsvUtil | ReportesIntegrationTest 2 casos | `Tests run: 67` | ✅ Reportes + Excel |
| RQ-15 | Línea base LB-SIGR-1.0 aprobada | Registro candidato (Etapa 1 verificada) | — | commits `c9e286d`, `0ec61b3` + 5º por Etapa 3 | ⏳ Pendiente revisión final |
| RQ-16 | Seguimiento automático de pedidos (sondeo 10 s, sin recarga) | PedidoController `/estado` + `actualizar-estado.js` | PedidoAutoEstadoTest | Suite (69 pruebas) | ✅ Automático |
| RQ-17 | Documentación línea base: verificación, manual, acta, incidencias, licencia | docs/* + LICENSE.txt MIT | Revisión de archivos | — | ✅ Documentado |
