# Verificación de línea base — SIGR (LB-SIGR-1.0 candidata)

Matriz requisito → implementación → método de comprobación → resultado → pendientes. Distingue pruebas automatizadas de verificaciones manuales. **No se declara validado lo que no se comprobó.**

## 1. Requisitos verificados

| Requisito | Implementación | Método de comprobación | Resultado | Pendientes |
|---|---|---|---|---|
| Autenticación y permisos por rol | Spring Security, BCrypt, CSRF; 403 propio en español (`/error/403`) | `UsuarioIntegrationTest` + suite completa | ✅ Automatizado | — |
| Catálogo: CRUD de platos y categorías | `MenuService`, `Plato/CategoriaController`, UI ES | `MenuIntegrationTest` (fotos, render, categorías) | ✅ Automatizado | — |
| Pedidos: registro, precio histórico, total servidor, transiciones, IDOR 404 | `PedidoService` + `PedidoController` | `PedidoIntegrationTest` | ✅ Automatizado | — |
| Seguimiento automático de pedidos | `GET /pedidos/{id}/estado` JSON + `static/js/actualizar-estado.js` (sondeo 10 s, sin recargar, pausa si la pestaña está oculta, anti-superposición) | `PedidoAutoEstadoTest` (propietario 200, ajeno 404, mesero/admin 200, listado con script) | ✅ Automatizado | Recorrido manual con dos sesiones simultáneas |
| Reservas sin cruces por mesa | Validación de intervalo + bloqueo pesimista por mesa; vencimiento automático America/Bogota | `ReservaIntegrationTest` (flujo, cruces, vencimiento, permisos) | ✅ Automatizado | Concurrencia real con doble envío simultáneo |
| Comprobantes de venta | `FacturacionService` (FAC-, histórico, estado de pago, anti-duplicado) | `FacturacionIntegrationTest` (comprobante, cliente, duplicado) | ✅ Automatizado | - |
| Caja: apertura, pagos completos únicos, cierre con diferencia | `CajaService` (transacción, bloqueo, esperado = base + efectivo, observación si difiere) | `CajaIntegrationTest` (66.000, cierre 166.000, permisos) | ✅ Automatizado | — |
| Reportes: cobros/platos/reservas/cierres | `ReporteService` + `ReportesController`; fechas por tipo (pago/pago/reserva/cierre), zona configurada | `ReportesIntegrationTest` (escenario 66.000, rangos, vacíos, 403, Excel) | ✅ Automatizado | — |
| Exportaciones CSV + Excel de Cobros + impresión | `CsvUtil`, `CobrosExcelService` (Apache POI 5.2.5) | Suite automatizada (tipos de celda, totales, filtros, rango vacío) | ✅ Automatizado | Inspección visual del `.xlsx` en Excel/LibreOffice |
| Git: rama `main`, historial real, trazabilidad, docs reproducibles | Commits descriptivos reales, matriz y docs versionados | `git log` / `git status` / revisión de archivos | ✅ Verificado | Publicación en GitHub y etiqueta `v1.0.0` (pendientes) |
| `.gitignore` excluye BD/logs/secretos/target | `.gitignore` raíz | Revisión de `git ls-files` (152 archivos versionados) | ✅ Verificado | — |
| Licencia MIT y créditos | `LICENSE.txt` (MIT 2026, titular del autor), `docs/creditos-imagenes.md` | Revisión de archivos | ✅ Verificado | — |

## 2. Alcance no verificado/pendiente (manual)

- Recorrido completo manual con navegador y datos de demo (ver `docs/guia-capturas.md`).
- Concurrencia real bajo doble envío (reservas/caja) y eficiencia con carga.
- Publicación en GitHub y creación de `v1.0.0` (pendientes, no afirmadas).
- Decisión de aprobación de LB-SIGR-1.0 (la línea base sigue **candidata**).
