# Changelog

## [0.0.1-SNAPSHOT] — Etapa 1 (2026-11-05)
### Agregado
- Estructura inicial Spring Boot 3.5.16 / Java 21 con Maven Wrapper 3.9.11.
- Spring MVC, Thymeleaf, Security (form + BCrypt + CSRF), JPA, Validation, H2 en archivo.
- Página inicial (`index`) y login (`login`), CSS base.
- Tests: `SigrApplicationTests`, `HomeControllerTest` (4 pruebas).
- Git inicializado en `main`.

### Pendiente
- Módulos Menús, Pedidos, Reservas, Facturación, Caja/Reportes.
- Documentación completa y aprobación de LB-SIGR-1.0.

## [0.0.1-SNAPSHOT] — Etapa 2 (2026-11-05)
### Agregado
- Entidad `Usuario` JPA + `Rol` (ADMINISTRADOR/MESERO/CLIENTE), repositorio, servicio y DTOs con validación.
- Autenticación desde BD (`DbUserDetailsService` + BCrypt); usuarios desactivados no ingresan.
- CRUD de usuarios solo ADMINISTRADOR (listar/crear/editar/activar-desactivar), CSRF, logout POST, mensajes ES.
- Protección del último administrador activo (desactivar / quitar rol/editar rechazado).
- Administrador inicial vía variable de entorno `SIGR_ADMIN_PASSWORD` (`DataInitializer`); sin claves en código/docs/Git.
- Pantallas responsive ES: `/usuarios`, `/usuarios/nuevo`, `/usuarios/{id}/editar`, fragmento nav con usuario/rol.
- Tests `UsuarioIntegrationTest`: 12 totales (login OK/incorrecto, desactivado, duplicado, permisos rol, último admin, persistencia BCrypt).

### Pendiente
- Módulos Menús, Pedidos, Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata).

## [0.0.1-SNAPSHOT] — Etapa 3 (2026-11-06)
### Agregado
- Categorías (Entradas, Platos principales, Bebidas, Postres) y platos JPA con BigDecimal COP, fotografía local, disponible/activo (desactivado ≠ no disponible).
- Catálogo responsive `/platos` (búsqueda + filtro categoría, tarjetas), detalle, formularios ES con validación (precio > 0, categoría activa).
- Permisos: solo ADMINISTRADOR crea/edita/desactiva (procedimiento en servidor vía @PreAuthorize); MESERO/CLIENTE consultan catálogo.
- Datos demo explícitos e idempotentes: botón `Cargar datos de demostración` (POST `/platos/cargar-demo`), 12 platos + respaldo, fotos CC0 en `static/images/platos/` (ver `docs/fotos-catalogo.md`).
- Tests `MenuIntegrationTest` 6 casos: CRUD/desactivación, validación, filtros, demo idempotente, persistencia, permisos 3 roles. Total 20.

### Pendiente
- Módulos Pedidos, Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Colores y vencimiento automático (2026-11-06)
### Cambiado
- Colores: En curso verde, Próxima naranja, Cancelada roja (listado, detalle y leyenda, etiquetas legibles). Finalizada sigue gris.
- Vencimiento automático (America/Bogota, fecha+horaFin completa): CONFIRMADA con `ahora >= fin` → CANCELADA por vencimiento con motivo y fecha (idempotente, bloqueo por mesa, periódica cada 60 s y al iniciar; sin depender de abrir página). Cancelación manual registra MANUAL; ya canceladas intactas. Esquema sin borrar BD (`motivo_cancelacion`, `fecha_cancelacion` por `update`).
- Tests con reloj controlado: antes del fin, fin exacto, después, fecha pasada/futura, manual previa y repetición idempotente. Total 51.

### Pendiente
- Módulos Facturación, Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Indicadores visuales de reservas (2026-11-06)
### Cambiado
- Situación temporal (America/Bogota, sin cambiar estado persistido): Cancelada roja, Próxima verde, En curso naranja (`inicio <= ahora < fin`), Horario finalizado gris. «Horario finalizado» solo indica que pasó el horario, no asistencia.
- Badges con fondo suave, texto legible y etiqueta escrita siempre, en listado y detalle, con leyenda breve. Disponibilidad intacta.
- Tests con reloj controlado (antes/inicio exacto/durante/fin exacto/cancelación) + render listado/detalle. Total 48.

### Pendiente
- Módulos Facturación, Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Configuración de mesas y disponibilidad integrada (2026-11-06)
### Cambiado
- Solo ADMINISTRADOR ajusta la cantidad total de mesas activas (sin cantidad arbitraria ni creación automática al iniciar): al subir crea faltantes con numeración única y capacidad indicada; al bajar elige cuáles desactivar sin borrar/renumerar. Resumen calculado desde mesas reales (registradas/activas/inactivas + capacidad activa).
- Desactivación protegida: no se desactiva mesa con confirmadas pendientes (se explican IDs/fechas a resolver primero); historial conservado y reactivación desde listado/edición.
- Reservas consultan todas las activas con capacidad; solo excluyen las ocupadas en fecha/intervalo (Mesa 1 ocupada no bloquea Mesa 2/3); bloqueo pesimista conservado; mensajes distintos (sin mesas / capacidad / ocupadas) con enlace a Gestionar mesas para admin.
- Tests: tres mesas cap 4, Tres 1 ocupada 12:00-16:42, búsqueda 13:00-14:00 para 4 ofrece Tres 2 y 3; ampliación, reducción protegida, reactivación, numeración única, permisos. Total 41.

### Pendiente
- Módulos Facturación, Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Selección de mesa por disponibilidad (2026-11-06)
### Cambiado
- El formulario pide fecha, horarios (24 h) y personas antes de elegir mesa; «Actualizar mesas disponibles» (GET) filtra solo mesas activas con capacidad suficiente y libres en todo el intervalo. Al cambiar datos, la opción inválida se limpia; sin disponibilidad se informa («No hay mesas disponibles…») y se puede cambiar horario/personas.
- Servidor revalida al guardar con **bloqueo pesimista por mesa** (`PESSIMISTIC_WRITE`) para impedir cruces simultáneos; canceladas liberan horario y quedan en historial; nombres únicos y mesas/reservas existentes intactos.
- Tests de cruces, horarios iguales/separados, fechas distintas, dos mesas a la misma hora, cancelada que libera, selección limpia y conservación de datos ante error. Total 39.

### Pendiente
- Módulos Facturación, Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Claridad Reservas y Mesas (2026-11-06)
### Cambiado
- Horarios con selección explícita 24 h (hora 00–23 + minutos 00–59 en listas identificadas, sin `type=time` del navegador); ayuda «12:00 = mediodía; 00:00 = medianoche»; sin precarga arbitraria (listas vacías). El servidor sigue validando fin>inicio y anti-superposición.
- Fechas en listado/detalle como `dd/MM/yyyy` y horarios `HH:mm`; encabezado «#» → «N.º de reserva» (columna «Mesa» independiente, IDs sin renumerar); selector «Mesa 1 — 4 personas». Carga demo de mesas repetible sin duplicar.
- Tests `claridadHorariosNumeracionYFechas` + `mesasDemoRepetibleSinDuplicarPorHttp`. Total 35.

### Pendiente
- Módulos Facturación, Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Reservas (2026-11-06)
### Agregado
- Entidades `Mesa` (nombre único, capacidad 1–50, activa) y `Reserva` (dueño, mesa, titular, fecha, horarios, personas, `CONFIRMADA/CANCELADA`).
- Reglas: mesa activa, fecha no pasada (America/Bogota), fin después de inicio, personas ≤ capacidad, y **anti-superposición por mesa**: misma mesa + fecha + horarios solapados en `CONFIRMADA` se rechaza; cancelada libera el hueco.
- Permisos servidor: ADMINISTRADOR gestiona mesas y todas las reservas; MESERO ve/crea/cancela la operación (todas); CLIENTE solo crea/consulta/cancela las suyas — ID ajeno devuelve 404. Titular de CLIENTE forzado a su nombre.
- Mesas demo idempotentes (botón ADMINISTRADOR): Mesa 1–4 + Terraza 1. Pantallas ES `/mesas`, `/reservas`, `/reservas/nueva`, `/reservas/{id}` con render de datos.
- Tests `ReservaIntegrationTest` 5 casos: capacidad/fecha/horario, superposición + hueco libre tras cancelar, IDOR 404 + cancelar propia, roles (mesas solo ADMIN), mesas demo. Total 33.

### Pendiente
- Módulos Facturación, Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Pedidos (2026-11-06)
### Agregado
- Entidades `Pedido` + `DetallePedido` con precio histórico por línea (nombre/precio al momento del pedido), total BigDecimal calculado en el servidor (nunca confiado al cliente), cantidades 1–100 y solo platos activos+disponibles.
- Estados `PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO` y `CANCELADO` desde pendiente/en preparación; transiciones validadas en el servidor.
- Permisos servidor: ADMINISTRADOR ve/gestiona todos; MESERO crea y gestiona la operación (todos); CLIENTE solo crea/consulta/cancela los suyos — ID ajeno devuelve 404 (sin revelar existencia). El pedido queda asociado al creador autenticado.
- Pantallas ES `/pedidos`, `/pedidos/nuevo`, `/pedidos/{id}` con render de líneas, subtotales y total COP; navegación y panel principal actualizados.
- Tests `PedidoIntegrationTest` 5 casos: total servidor + snapshot, rechazo no disponible/desactivado/cantidad, transiciones, IDOR 404 + render detalle/lista, roles. Total 28.

### Pendiente
- Módulos Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Fotografías reales del catálogo (2026-11-06)
### Cambiado
- Las 12 ilustraciones generadas fueron reemplazadas por fotografías reales con permiso de reutilización (CC BY / CC BY-SA / CC0), guardadas en `src/main/resources/static/images/platos/` (miniaturas 800 px, 83–268 KB, funcionamiento sin conexión). Sin ilustraciones presentadas como fotografías.
- Proporción uniforme: `.card img` y `.plato-foto-detalle` con `object-fit: cover`, `aspect-ratio: 4/3`; `alt` con nombre del plato y respaldo `onerror` a `placeholder.jpg` en catálogo y detalle.
- Atribución documentada en `docs/creditos-imagenes.md` (título, autor, licencia y fuente por foto). Nombres, precios, categorías, usuarios y datos conservados; no se duplicó ni se sobrescribió lógica.
- Tests: `fotografiasRealesCarganEnCatalogoYDetalle` (GET `/images/platos/*.jpg` 200 ×12 + catálogo con `limonada.jpg`/`cafe.jpg`). Total 23.

### Pendiente
- Módulos Pedidos, Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Ajuste catálogo 500 (2026-11-06)
### Corregido
- Se conserva LAZY en `Plato.categoria` y se carga con `@EntityGraph(attributePaths="categoria")` en las consultas del catálogo/detalle (`PlatoRepository`), evitando `LazyInitializationException`.
- Se corrige la expresión de precio Thymeleaf en `platos/catalogo.html` y `platos/detalle.html` (`${'$' + #numbers.formatDecimal(...) + ' COP'}`, antes fallaba con datos reales y el catálogo vacío lo enmascaraba).
- Se crea la plantilla general `templates/error.html` en español («Ocurrió un error…») sin detalles técnicos, conservando el estado HTTP; `error/403.html` sigue para 403.
- Tests: `catalogoRenderizaDatosDemoConNombresCategoriasPreciosYFotos` (verifica 200, Empanadas/Entradas, $12.000,00 COP, /images/platos/empanadas.jpg, Bandeja paisa y detalle) y `errorGenericoEnEspanolSinWhitelabel` (404 conservado + plantilla ES sin Whitelabel). Total 22.

### Pendiente
- Módulos Pedidos, Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Ajuste 403 (2026-11-06)
### Cambiado
- Página personalizada 403 en español («Acceso denegado…») con enlace al panel, vía `error/403.html` + `/error/403` (estado HTTP 403, sin Whitelabel ni detalles técnicos). Restricción conservada.
- Tests: CLIENTE y MESERO → 403 con `forwardedUrl`, página `/error/403` verificada (14 totales).

### Pendiente
- Módulos Menús, Pedidos, Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata).

## [0.0.1-SNAPSHOT] — Facturación académica (2026-11-07)
### Agregado
- Comprobante único por pedido ENTREGADO («Comprobante académico — sin validez fiscal», pendiente de pago, sin cobro): número FAC- estable, fecha Bogota, copia histórica de cliente/platos/precios/cantidades/subtotales/total BigDecimal servidor, sin impuestos ni propinas.
- Anti-duplicado con bloqueo pesimista del pedido + restricción única; si ya existe informa y abre el existente. Sin editar/eliminar; pedido facturado no se cancela.
- Permisos: ADMINISTRADOR/MESERO emiten y ven todos; CLIENTE solo consulta los suyos (ajeno 404, emitir 403). Listado, detalle imprimible (`print.css`) y enlace desde pedido entregado. ES COP dd/MM/yyyy HH:mm con CSRF.
- Tests: emisión, rechazados, totales, histórico, permisos, duplicado y render; escenario solo de pruebas 2×28.000 + 2×5.000 = 66.000 COP. Total 55.

### Pendiente
- Módulos Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Ubicación del restaurante (2026-11-07)
### Agregado
- Configuración solo ADMINISTRADOR (`/configuracion`): país, ciudad (catálogo con zona explícita, no deducida del país), dirección y zona; inicial America/Bogota, sin geolocalización. Internamente se conserva la zona y calcula fechas (reservas, comprobantes, vencimiento).
- Comprobante muestra ciudad y país visibles (sin «(America/Bogota)» técnico) y guarda copia de ubicación/zona al emitir; cambios posteriores no alteran emitidos.
- Cambio de zona bloqueado con reservas confirmadas pendientes (explica motivo) para no reinterpretar horarios.
- Tests: solo admin, ciudad exige su zona, copia inmutable, bloqueo de zona. Total 58.
- Corrección posterior (misma versión): la ciudad es la única selección; país/zona son solo lectura derivada por el servidor (Cali muestra solo «America/Bogota»); valores manipulados se ignoran. Total 59.

### Pendiente
- Módulos Caja/Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Caja y pagos (2026-11-07)
### Agregado
- Sesión única abierta solo ADMINISTRADOR (`/caja`): responsable, fecha/hora configurada, base inicial ≥ 0, zona histórica; sin ingresos/egresos manuales. Sin editar/reabrir cerradas.
- Pago completo único por comprobante pendiente (efectivo/tarjeta/transferencia, sin abonos/mixtos): importe desde servidor BigDecimal; efectivo valida recibido ≥ total con cambio e ingreso = total; tarjeta/transferencia con referencia obligatoria sin datos sensibles. Registro + PAGADO en una transacción, con bloqueo y restricción única; sin editar/eliminar.
- Cierre con base + cobros por método, esperado = base + efectivo, diferencia con observación obligatoria y resumen definitivo. Tarjeta/transferencia no aumentan efectivo esperado.
- MESERO sin Caja (403); CLIENTE solo ve sus comprobantes Pagados. Tests: base 100.000 + 66.000 en efectivo (70.000 → cambio 4.000, esperado 166.000), permisos, apertura única, sin caja, duplicados, validaciones, cierre. Total 63.

### Pendiente
- Módulos Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Migración ENUMEstadoPago (2026-11-07)
### Corregido
- `comprobantes.estado` antiguo ENUM solo admitía PENDIENTE_DE_PAGO (error 22030 al pagar). Migración controlada idempotente al iniciar (`MigracionEstadoPago`, INFORMATION_SCHEMA): si es ENUM lo convierte a VARCHAR(30) con ALTER COLUMN conservando datos/relaciones/restricciones; `update` no lo corrige. Entidad con `columnDefinition=VARCHAR(30)` para BD nuevas. Sin borrado ni create-drop; pago solo vía transacción (intento fallido revierte pago+estado).
- Verificación de solo lectura de la BD local pendiente por bloqueo de la app en ejecución: al reiniciar se aplica la migración y se verifica. Tests: `MigracionEstadoPagoTest` (66.000, cambio 4.000, PAGADO, 1 pago, idempotencia) + suite 64.

### Pendiente
- Módulos Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Claridad de cierre de Caja (2026-11-07)
### Cambiado
- El panel muestra antes del cierre: base inicial, total en efectivo (cobrado, sin recibido antes del cambio), tarjeta y transferencia por separado y efectivo esperado = base + efectivo. Sin pedir pagos de nuevo; tarjeta/transferencia no suman al esperado.
- «Dinero contado físicamente en caja (incluye la base inicial)», ayuda de conteo, diferencia estimada al escribir y recálculo/validación en el servidor de contado − esperado; observación obligatoria si difiere. Contado sin precarga.
- Caso: base 100.000 + 66.000 = 166.000; contado 166.000 → 0, contado 66.000 → faltante 100.000.
- Tests: `cierreMuestraResumenCon166000YFaltante` + suite 65.

### Pendiente
- Módulos Reportes (no implementados).
- Aprobación de LB-SIGR-1.0 (sigue candidata, sin v1.0.0).

## [0.0.1-SNAPSHOT] — Reportes (2026-11-07)
### Agregado
- Solo ADMINISTRADOR (`/reportes`, MESERO/CLIENTE 403): Cobros por fecha de pago (total, cantidad, desglose por método, detalle comprobante/pedido/fecha/método/importe; base/cambio/pendientes excluidos, sin duplicar líneas), Platos históricos por fecha de pago (cantidad/importe por plato, desde líneas de pagados, orden cantidad desc), Reservas por fecha/mesa (confirmadas/manuales/vencimiento/sin motivo, sin presentar confirmadas como asistencia) y Cierres por fecha de cierre (base/cobros/esperado/contado/diferencia, sin sumar base como venta).
- Rangos completos por zona configurada, validación ES de incompletos/invertidos, COP y dd/MM/yyyy HH:mm, totales cero sin resultados, imprimible y CSV por reporte con escape y protección anti-fórmulas.
- Tests: escenario base 100.000 + 66.000 (2 líneas) + pendiente → 66.000/1 pagado, método/platos/reservas/cierres, render y CSV, permisos, rangos. Total 67.

### Agregado (Excel)
- Exportación Excel (.xlsx, Apache POI 5.2.5) del reporte de Cobros: botón «Descargar Excel», misma validación/datos del CSV. Título, ubicación (ciudad/país), periodo, COP, resumen, tabla con filtros y fila de total, nota sobre base inicial, sin colores, negritas, bordes, columnas autoajustadas, importes numéricos con formato monetario, fechas como fechas de Excel y textos como texto (sin fórmulas). Verificado en `ReportesIntegrationTest` (66.000, tipos, rango vacío, permisos). Total 67 (mismos casos ampliados).

### Pendiente
- Aprobación de LB-SIGR-1.0 y revisión final (sigue candidata, sin v1.0.0).
