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
