# Estado de avance â€” SIGR (2026-11-08)

Proyecto SIGR (Sistema Integral de Gesti\u00f3n de Restaurante): demostrativo, con l\u00ednea base **LB-SIGR-1.0 candidata** \u2014 pendiente de validaci\u00f3n/aprobaci\u00f3n. Sin etiqueta 1.0.0. Repositorio: https://github.com/Carlos10wA/sigr (publicaci\u00f3n pendiente de revisi\u00f3n).

## Implementado en esta sesiÃ³n

1. **VerificaciÃ³n catÃ¡logo (bÃºsqueda, filtros, detalle)** â€” Verificada: tests previos 23/23 pasaban, incluidas renderizadas con datos demo (nombres, categorÃ­as, precios COP, fotos reales CC). Sin cambios de catÃ¡logo salvo navegaciÃ³n.
2. **Pedidos** â€” Platos solo activos+disponibles, cantidades positivas 1â€“100, precio histÃ³rico por lÃ­nea (nombre/precio al pedir), total BigDecimal calculado en el servidor (nunca confiado al cliente), transiciones `PENDIENTE â†’ EN_PREPARACION â†’ LISTO â†’ ENTREGADO` y `CANCELADO` desde pendiente/en preparaciÃ³n. Pantallas ES en `templates/pedidos/`.
3. **Reservas** â€” Mesas (nombre Ãºnico, capacidad 1â€“50, activa, solo ADMINISTRADOR), reservas con fecha (no pasada, America/Bogota), horario (fin > inicio), capacidad (personas â‰¤ mesa), titulares, anti-superposiciÃ³n por mesa+fecha (cancelada libera el hueco) y cancelaciÃ³n. Pantallas ES en `templates/mesas/` y `templates/reservas/`.
4. **Claridad + selecciÃ³n de mesa** â€” Horarios 24 h explÃ­citos (hora/minutos, Â«12:00 = mediodÃ­a; 00:00 = medianocheÂ», sin precarga), fechas `dd/MM/yyyy`, Â«N.Âº de reservaÂ», selector Â«Mesa 1 â€” 4 personasÂ»; el formulario pide fecha/horario/personas antes de la mesa y solo lista activas con capacidad y libres en todo el intervalo (Â«Actualizar mesas disponiblesÂ»); al cambiar datos limpia la selecciÃ³n invÃ¡lida; sin disponibilidad avisa y permite cambiar; el servidor revalida con bloqueo pesimista por mesa frente a cruces simultÃ¡neos y conserva los datos ante error. Sin renombres ni reasignaciones.
5. **ConfiguraciÃ³n de mesas** â€” Solo ADMINISTRADOR ajusta la cantidad total de activas (sin cantidad arbitraria ni auto-creaciÃ³n al iniciar): al subir crea faltantes Ãºnicos con la capacidad indicada; al bajar elige cuÃ¡les desactivar sin borrar/renumerar; no desactiva mesas con confirmadas pendientes (explica IDs/fechas); reactivaciÃ³n permitida; resumen calculado desde mesas reales. Reservas ofrecen Mesa 2/3 cuando Mesa 1 estÃ¡ ocupada y diferencian sin mesas/capacidad/ocupadas con enlace a Gestionar mesas para admin.
6. **FacturaciÃ³n** â€” Ãšnico comprobante por pedido ENTREGADO (Â«Comprobante de venta â€” Sin validez fiscalÂ», pendiente de pago, sin cobro): nÃºmero FAC- estable, fecha Bogota, copia histÃ³rica y total BigDecimal servidor (2Ã—28.000+2Ã—5.000=66.000 COP en prueba). ADMINISTRADOR/MESERO emiten y ven todos; CLIENTE solo consulta los suyos y no emite. Anti-duplicado con bloqueo + Ãºnica; sin editar/eliminar; pedido facturado no se cancela. Listado, detalle imprimible y enlace desde entregado.
7. **UbicaciÃ³n del restaurante** â€” Solo ADMINISTRADOR (`/configuracion`): paÃ­s, ciudad del catÃ¡logo con su zona explÃ­cita (no deducida del paÃ­s), direcciÃ³n; inicial Colombia/BogotÃ¡/America-Bogota, sin geolocalizaciÃ³n. El comprobante muestra Â«BogotÃ¡, ColombiaÂ» (sin Â«(America/Bogota)Â») y guarda copia de ubicaciÃ³n/zona al emitir; cambios responsables no alteran emitidos. Cambio de zona con confirmadas pendientes se bloquea explicando el motivo, sin reinterpretar horarios; las fechas futuras usan la zona configurada.
8. **Caja y pagos** â€” Solo ADMINISTRADOR (`/caja`): sesiÃ³n Ãºnica abierta (responsable, fecha/zona histÃ³rica, base â‰¥ 0), pagos completos Ãºnicos por comprobante pendiente (efectivo con recibido/cambio, tarjeta/transferencia con referencia, sin abonos/mixtos/devoluciones, registro manual sin banco), importe servidor BigDecimal y PAGADO en una transacciÃ³n con bloqueo + Ãºnica; cierre con cobros por mÃ©todo, esperado = base + efectivo, diferencia con observaciÃ³n, resumen definitivo sin reabrir. MESERO sin Caja (403); CLIENTE solo consulta sus Pagados. Escenario: base 100.000 + 66.000 con 70.000 â†’ cambio 4.000, esperado 166.000; tarjeta/transferencia no suben efectivo.
9. **Reportes** â€” Solo ADMINISTRADOR (`/reportes`, CSV e impresiÃ³n): Cobros por fecha de pago (66.000/1, excluye base/cambio/pendiente), Platos histÃ³ricos por fecha de pago, Reservas por fecha/mesa con motivos (sin motivo para legacy) y Cierres por fecha de cierre; rangos completos por zona configurada con COP/dd/MM/HH:mm. Cobros ademÃ¡s exportable a Excel (.xlsx Apache POI) con el botÃ³n Â«Descargar ExcelÂ». Escenario verificado en BD aislada.

### Reglas de permisos y propiedad (fuera de la vista, en el servidor)

| Rol | Pedidos | Reservas / Mesas |
|---|---|---|
| ADMINISTRADOR | Ve/gestiona todos (avanzar estado, cancelar) | Gestiona mesas; ve/cancela todas las reservas; crea mesas demo |
| MESERO | Crea (como operador) y gestiona la operaciÃ³n: ve todos, avanza/cancela | Ve/crea/cancela todas (titular libre); no ve `/mesas` (403) |
| CLIENTE | Solo crea/consulta/cancela (pendiente/en preparaciÃ³n) los suyos | Solo crea/consulta/cancela las suyas (titular forzado a su nombre) |

ID ajeno manipulado (pedido/reserva de otro CLIENTE) devuelve **404** sin revelar existencia (no 500 ni datos ajenos). El registro queda asociado al creador autenticado siempre. AnÃ³nimo redirige a `/login`.

## Resultados reales de pruebas

- CatÃ¡logo (previo a esta sesiÃ³n): `Tests run: 23, Failures: 0, Errors: 0 â€” BUILD SUCCESS`.
- Tras Pedidos: `Tests run: 28, Failures: 0, Errors: 0 â€” BUILD SUCCESS` (`PedidoIntegrationTest` 5/5: total servidor + snapshot, rechazo no disponible/desactivado/cantidad, transiciones, IDOR 404 + render detalle/lista, roles).
- Tras Reservas: `Tests run: 33, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
  - `HomeControllerTest` 2, `MenuIntegrationTest` 9, `PedidoIntegrationTest` 5, `ReservaIntegrationTest` 5, `SigrApplicationTests` 1, `UsuarioIntegrationTest` 11.
- Tras claridad + disponibilidad: `Tests run: 39, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
  - `ReservaIntegrationTest` 11 (incluye cruces, horarios separados/iguales, fechas/mesas distintas, cancelada que libera, sin disponibilidad, selecciÃ³n limpia y conservaciÃ³n de datos).
- Tras configuraciÃ³n de mesas: `Tests run: 41, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
  - `ReservaIntegrationTest` 13 (aÃ±ade Tres 1 ocupada 12:00-16:42 â†’ 13:00-14:00 para 4 ofrece Tres 2 y 3; ampliaciÃ³n, reducciÃ³n protegida, reactivaciÃ³n, numeraciÃ³n Ãºnica, permisos).
- Tras indicadores + vencimiento: `Tests run: 51, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
- Tras FacturaciÃ³n: `Tests run: 55, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
  - `FacturacionIntegrationTest` 4 (emisiÃ³n/rechazados, duplicado, histÃ³rico 66.000 COP + permisos/propiedad, cancelados).
- Tras ubicaciÃ³n: `Tests run: 58, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
  - `UbicacionIntegrationTest` 3 (solo admin, ciudad exige su zona, copia inmutable en comprobante sin Â«(America/Bogota)Â», bloqueo de zona con pendientes).
- Tras Cali + Caja: `Tests run: 63, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
  - `UbicacionIntegrationTest` 4 (Cali solo lectura) y `CajaIntegrationTest` 4 (escenario 66.000, permisos, tarjeta/transferencia, validaciones/cierre).
- Tras migraciÃ³n ENUM: `Tests run: 64, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
- Tras cierre claro: `Tests run: 65, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS`
- Tras Reportes: `Tests run: 67, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS` (`ReportesIntegrationTest` 2, con Excel .xlsx).
- Tras seguimiento automÃ¡tico + lÃ­nea base (2026-11-08): `Tests run: 69, Failures: 0, Errors: 0, Skipped: 0 â€” BUILD SUCCESS` (`PedidoAutoEstadoTest` 1 + `reportesSinResultadosYLimitesExactos` 1, en BD aislada).
  - Comando: `.\mvnw.cmd test` con `JAVA_HOME=C:\Program Files\Java\jdk-21` (Java 21.0.2, Spring Boot 3.5.16). Probado 2026-11-06 con H2 en memoria (tests) â€” la BD archivo `data/sigrdb.mv.db` no se borrÃ³ ni reiniciÃ³.

## Commits (rama `main`, Ã¡rbol limpio salvo este documento)

- `d29fea44eb79192b05cbb0a99ad2004fbb52e49d` â€” `feat(pedidos): ... 28/28 BUILD SUCCESS (LB-SIGR-1.0 candidata)`
- `9dc9bad90b0600768bf93f2b2bb9d7c1c34fd0b2` â€” `feat(reservas): ... 33/33 BUILD SUCCESS (LB-SIGR-1.0 candidata)`

Historial previo conservado: `237f060`, `5285f29`, `b7d01b0`, `13430e9`, `3fc326c`, `0ec61b3`, `c9e286d`.

## Pendientes (no implementados)

- **Reportes**: implementados (`/reportes`, `/reportes/cobros`, `/platos`, `/reservas`, `/cierres` + CSV y Excel de Cobros). Pendiente solo revisiÃ³n final de lÃ­nea base y verificaciÃ³n manual (carga, concurrencia real, recorrido completo).
- AprobaciÃ³n de lÃ­nea base LB-SIGR-1.0 y creaciÃ³n de `v1.0.0`: no aprobados ni creados.
- Publicaci\u00f3n en GitHub: repositorio en https://github.com/Carlos10wA/sigr; la publicaci\u00f3n y la aprobaci\u00f3n de la l\u00ednea base son acontecimientos distintos (aprobaci\u00f3n pendiente).
- Servicios de pago: ninguno instalado. Secretos: ninguno registrado (solo `SIGR_ADMIN_PASSWORD` local vÃ­a entorno).
- Licencia MIT aplicada (`LICENSE.txt`); sin datos de instituciÃ³n/docente en presentaciÃ³n pÃºblica (informe separado).

## Pasos para iniciar y revisar

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:SIGR_ADMIN_PASSWORD="SuClaveLocal"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

1. Abrir `http://localhost:8080`, iniciar sesiÃ³n como `admin` (clave definida localmente, no en Git).
2. **CatÃ¡logo**: `/platos` â†’ Â«Cargar datos de demostraciÃ³nÂ» (idempotente, 12 platos con fotos CC locales, `docs/creditos-imagenes.md`); probar bÃºsqueda y filtro por categorÃ­a y detalle.
3. **Pedidos**: `/pedidos/nuevo` (solo platos disponibles) â†’ crear â†’ detalle `/pedidos/{id}` (lÃ­neas, subtotales, total COP) â†’ avanzar estado como ADMINISTRADOR/MESERO.
4. **Mesas/Reservas**: como ADMINISTRADOR, `/mesas` â†’ resumen + Â«Cantidad total de mesas activasÂ» y capacidad â†’ guardar (ampliar/reducir con elecciÃ³n) â†’ `/reservas/nueva`: primero fecha/horarios/personas â†’ Â«Actualizar mesas disponiblesÂ» â†’ elegir mesa â†’ crear â†’ detalle `/reservas/{id}` â†’ cancelar. Como CLIENTE, verificar que solo ve lo suyo y que un ID ajeno da 404.
5. **FacturaciÃ³n**: pedido ENTREGADO â†’ detalle `/pedidos/{id}` â†’ Â«Emitir comprobanteÂ» (ADMIN/MESERO) â†’ `/facturas/{id}` (Â«Comprobante de venta â€” Sin validez fiscalÂ», pendiente de pago) â†’ Imprimir. CLIENTE solo consulta los suyos.
6. **UbicaciÃ³n**: como ADMINISTRADOR, `/configuracion` â†’ paÃ­s/ciudad/direcciÃ³n/zona â†’ guardar; comprobar que el detalle de comprobante muestra Â«BogotÃ¡, ColombiaÂ» (o la ciudad configurada) sin texto tÃ©cnico. Cambio de zona con pendientes debe bloquearse.
7. **Caja**: como ADMINISTRADOR, `/caja` â†’ abrir con base â†’ registrar pago (efectivo con recibido/cambio o tarjeta/transferencia con referencia) desde `/caja/pagos/nuevo` o desde el comprobante â†’ cerrar con efectivo contado (y observaciÃ³n si difiere). MESERO debe recibir 403 en `/caja`; CLIENTE solo ve sus comprobantes Pagados.
8. **Reportes**: como ADMINISTRADOR, `/reportes` â†’ Cobros/Platos/Reservas/Cierres con rangos â†’ Imprimir o Exportar CSV.
