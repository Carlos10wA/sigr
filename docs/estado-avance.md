# Estado de avance — SIGR (2026-11-08)

Proyecto SIGR (Sistema Integral de Gestión de Restaurante): demostrativo, con línea base **LB-SIGR-1.0 candidata** — pendiente de validación/aprobación. Sin etiqueta `v1.0.0`. GitHub: sin remoto (no se publicó).

## Implementado en esta sesión

1. **Verificación catálogo (búsqueda, filtros, detalle)** — Verificada: tests previos 23/23 pasaban, incluidas renderizadas con datos demo (nombres, categorías, precios COP, fotos reales CC). Sin cambios de catálogo salvo navegación.
2. **Pedidos** — Platos solo activos+disponibles, cantidades positivas 1–100, precio histórico por línea (nombre/precio al pedir), total BigDecimal calculado en el servidor (nunca confiado al cliente), transiciones `PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO` y `CANCELADO` desde pendiente/en preparación. Pantallas ES en `templates/pedidos/`.
3. **Reservas** — Mesas (nombre único, capacidad 1–50, activa, solo ADMINISTRADOR), reservas con fecha (no pasada, America/Bogota), horario (fin > inicio), capacidad (personas ≤ mesa), titulares, anti-superposición por mesa+fecha (cancelada libera el hueco) y cancelación. Pantallas ES en `templates/mesas/` y `templates/reservas/`.
4. **Claridad + selección de mesa** — Horarios 24 h explícitos (hora/minutos, «12:00 = mediodía; 00:00 = medianoche», sin precarga), fechas `dd/MM/yyyy`, «N.º de reserva», selector «Mesa 1 — 4 personas»; el formulario pide fecha/horario/personas antes de la mesa y solo lista activas con capacidad y libres en todo el intervalo («Actualizar mesas disponibles»); al cambiar datos limpia la selección inválida; sin disponibilidad avisa y permite cambiar; el servidor revalida con bloqueo pesimista por mesa frente a cruces simultáneos y conserva los datos ante error. Sin renombres ni reasignaciones.
5. **Configuración de mesas** — Solo ADMINISTRADOR ajusta la cantidad total de activas (sin cantidad arbitraria ni auto-creación al iniciar): al subir crea faltantes únicos con la capacidad indicada; al bajar elige cuáles desactivar sin borrar/renumerar; no desactiva mesas con confirmadas pendientes (explica IDs/fechas); reactivación permitida; resumen calculado desde mesas reales. Reservas ofrecen Mesa 2/3 cuando Mesa 1 está ocupada y diferencian sin mesas/capacidad/ocupadas con enlace a Gestionar mesas para admin.
6. **Facturación académica** — Único comprobante por pedido ENTREGADO («Comprobante académico — sin validez fiscal», pendiente de pago, sin cobro): número FAC- estable, fecha Bogota, copia histórica y total BigDecimal servidor (2×28.000+2×5.000=66.000 COP en prueba). ADMINISTRADOR/MESERO emiten y ven todos; CLIENTE solo consulta los suyos y no emite. Anti-duplicado con bloqueo + única; sin editar/eliminar; pedido facturado no se cancela. Listado, detalle imprimible y enlace desde entregado.
7. **Ubicación del restaurante** — Solo ADMINISTRADOR (`/configuracion`): país, ciudad del catálogo con su zona explícita (no deducida del país), dirección; inicial Colombia/Bogotá/America-Bogota, sin geolocalización. El comprobante muestra «Bogotá, Colombia» (sin «(America/Bogota)») y guarda copia de ubicación/zona al emitir; cambios responsables no alteran emitidos. Cambio de zona con confirmadas pendientes se bloquea explicando el motivo, sin reinterpretar horarios; las fechas futuras usan la zona configurada.
8. **Caja y pagos** — Solo ADMINISTRADOR (`/caja`): sesión única abierta (responsable, fecha/zona histórica, base ≥ 0), pagos completos únicos por comprobante pendiente (efectivo con recibido/cambio, tarjeta/transferencia con referencia, sin abonos/mixtos/devoluciones, registro manual sin banco), importe servidor BigDecimal y PAGADO en una transacción con bloqueo + única; cierre con cobros por método, esperado = base + efectivo, diferencia con observación, resumen definitivo sin reabrir. MESERO sin Caja (403); CLIENTE solo consulta sus Pagados. Escenario: base 100.000 + 66.000 con 70.000 → cambio 4.000, esperado 166.000; tarjeta/transferencia no suben efectivo.
9. **Reportes** — Solo ADMINISTRADOR (`/reportes`, CSV e impresión): Cobros por fecha de pago (66.000/1, excluye base/cambio/pendiente), Platos históricos por fecha de pago, Reservas por fecha/mesa con motivos (sin motivo para legacy) y Cierres por fecha de cierre; rangos completos por zona configurada con COP/dd/MM/HH:mm. Cobros además exportable a Excel (.xlsx Apache POI) con el botón «Descargar Excel». Escenario verificado en BD aislada.

### Reglas de permisos y propiedad (fuera de la vista, en el servidor)

| Rol | Pedidos | Reservas / Mesas |
|---|---|---|
| ADMINISTRADOR | Ve/gestiona todos (avanzar estado, cancelar) | Gestiona mesas; ve/cancela todas las reservas; crea mesas demo |
| MESERO | Crea (como operador) y gestiona la operación: ve todos, avanza/cancela | Ve/crea/cancela todas (titular libre); no ve `/mesas` (403) |
| CLIENTE | Solo crea/consulta/cancela (pendiente/en preparación) los suyos | Solo crea/consulta/cancela las suyas (titular forzado a su nombre) |

ID ajeno manipulado (pedido/reserva de otro CLIENTE) devuelve **404** sin revelar existencia (no 500 ni datos ajenos). El registro queda asociado al creador autenticado siempre. Anónimo redirige a `/login`.

## Resultados reales de pruebas

- Catálogo (previo a esta sesión): `Tests run: 23, Failures: 0, Errors: 0 — BUILD SUCCESS`.
- Tras Pedidos: `Tests run: 28, Failures: 0, Errors: 0 — BUILD SUCCESS` (`PedidoIntegrationTest` 5/5: total servidor + snapshot, rechazo no disponible/desactivado/cantidad, transiciones, IDOR 404 + render detalle/lista, roles).
- Tras Reservas: `Tests run: 33, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
  - `HomeControllerTest` 2, `MenuIntegrationTest` 9, `PedidoIntegrationTest` 5, `ReservaIntegrationTest` 5, `SigrApplicationTests` 1, `UsuarioIntegrationTest` 11.
- Tras claridad + disponibilidad: `Tests run: 39, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
  - `ReservaIntegrationTest` 11 (incluye cruces, horarios separados/iguales, fechas/mesas distintas, cancelada que libera, sin disponibilidad, selección limpia y conservación de datos).
- Tras configuración de mesas: `Tests run: 41, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
  - `ReservaIntegrationTest` 13 (añade Tres 1 ocupada 12:00-16:42 → 13:00-14:00 para 4 ofrece Tres 2 y 3; ampliación, reducción protegida, reactivación, numeración única, permisos).
- Tras indicadores + vencimiento: `Tests run: 51, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
- Tras Facturación: `Tests run: 55, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
  - `FacturacionIntegrationTest` 4 (emisión/rechazados, duplicado, histórico 66.000 COP + permisos/propiedad, cancelados).
- Tras ubicación: `Tests run: 58, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
  - `UbicacionIntegrationTest` 3 (solo admin, ciudad exige su zona, copia inmutable en comprobante sin «(America/Bogota)», bloqueo de zona con pendientes).
- Tras Cali + Caja: `Tests run: 63, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
  - `UbicacionIntegrationTest` 4 (Cali solo lectura) y `CajaIntegrationTest` 4 (escenario 66.000, permisos, tarjeta/transferencia, validaciones/cierre).
- Tras migración ENUM: `Tests run: 64, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
- Tras cierre claro: `Tests run: 65, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
- Tras Reportes: `Tests run: 67, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS` (`ReportesIntegrationTest` 2, con Excel .xlsx).
- Tras seguimiento automático + línea base (2026-11-08): `Tests run: 69, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS` (`PedidoAutoEstadoTest` 1 + `reportesSinResultadosYLimitesExactos` 1, en BD aislada).
  - Comando: `.\mvnw.cmd test` con `JAVA_HOME=C:\Program Files\Java\jdk-21` (Java 21.0.2, Spring Boot 3.5.16). Probado 2026-11-06 con H2 en memoria (tests) — la BD archivo `data/sigrdb.mv.db` no se borró ni reinició.

## Commits (rama `main`, árbol limpio salvo este documento)

- `d29fea44eb79192b05cbb0a99ad2004fbb52e49d` — `feat(pedidos): ... 28/28 BUILD SUCCESS (LB-SIGR-1.0 candidata)`
- `9dc9bad90b0600768bf93f2b2bb9d7c1c34fd0b2` — `feat(reservas): ... 33/33 BUILD SUCCESS (LB-SIGR-1.0 candidata)`

Historial previo conservado: `237f060`, `5285f29`, `b7d01b0`, `13430e9`, `3fc326c`, `0ec61b3`, `c9e286d`.

## Pendientes (no implementados)

- **Reportes**: implementados (`/reportes`, `/reportes/cobros`, `/platos`, `/reservas`, `/cierres` + CSV y Excel de Cobros). Pendiente solo revisión final de línea base y verificación manual (carga, concurrencia real, recorrido completo).
- Aprobación de línea base LB-SIGR-1.0 y creación de `v1.0.0`: no aprobados ni creados.
- Publicación en GitHub: no realizada (sin remoto; URL oficial pendiente).
- Servicios de pago: ninguno instalado. Secretos: ninguno registrado (solo `SIGR_ADMIN_PASSWORD` local vía entorno).
- Licencia MIT aplicada (`LICENSE.txt`); sin datos de institución/docente en presentación pública (informe separado).

## Pasos para iniciar y revisar

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:SIGR_ADMIN_PASSWORD="SuClaveLocal"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

1. Abrir `http://localhost:8080`, iniciar sesión como `admin` (clave definida localmente, no en Git).
2. **Catálogo**: `/platos` → «Cargar datos de demostración» (idempotente, 12 platos con fotos CC locales, `docs/creditos-imagenes.md`); probar búsqueda y filtro por categoría y detalle.
3. **Pedidos**: `/pedidos/nuevo` (solo platos disponibles) → crear → detalle `/pedidos/{id}` (líneas, subtotales, total COP) → avanzar estado como ADMINISTRADOR/MESERO.
4. **Mesas/Reservas**: como ADMINISTRADOR, `/mesas` → resumen + «Cantidad total de mesas activas» y capacidad → guardar (ampliar/reducir con elección) → `/reservas/nueva`: primero fecha/horarios/personas → «Actualizar mesas disponibles» → elegir mesa → crear → detalle `/reservas/{id}` → cancelar. Como CLIENTE, verificar que solo ve lo suyo y que un ID ajeno da 404.
5. **Facturación**: pedido ENTREGADO → detalle `/pedidos/{id}` → «Emitir comprobante» (ADMIN/MESERO) → `/facturas/{id}` («Comprobante académico — sin validez fiscal», pendiente de pago) → Imprimir. CLIENTE solo consulta los suyos.
6. **Ubicación**: como ADMINISTRADOR, `/configuracion` → país/ciudad/dirección/zona → guardar; comprobar que el detalle de comprobante muestra «Bogotá, Colombia» (o la ciudad configurada) sin texto técnico. Cambio de zona con pendientes debe bloquearse.
7. **Caja**: como ADMINISTRADOR, `/caja` → abrir con base → registrar pago (efectivo con recibido/cambio o tarjeta/transferencia con referencia) desde `/caja/pagos/nuevo` o desde el comprobante → cerrar con efectivo contado (y observación si difiere). MESERO debe recibir 403 en `/caja`; CLIENTE solo ve sus comprobantes Pagados.
8. **Reportes**: como ADMINISTRADOR, `/reportes` → Cobros/Platos/Reservas/Cierres con rangos → Imprimir o Exportar CSV.
