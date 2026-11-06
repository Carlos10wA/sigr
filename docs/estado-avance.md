# Estado de avance — SIGR (2026-11-06)

Proyecto académico SIGR (Sistema Integral de Gestión de Restaurante), Gestión del Software Unidad 2, Tecnológica del Oriente.
Estudiante: Carlos Alberto Ayala Larrahondo. Docente: Carlos Carrascal Avendaño.
Línea base **LB-SIGR-1.0 candidata** — pendiente de validación/aprobación. Sin etiqueta `v1.0.0`. GitHub: sin remoto (no se publicó).

## Implementado en esta sesión

1. **Verificación catálogo (búsqueda, filtros, detalle)** — Verificada: tests previos 23/23 pasaban, incluidas renderizadas con datos demo (nombres, categorías, precios COP, fotos reales CC). Sin cambios de catálogo salvo navegación.
2. **Pedidos** — Platos solo activos+disponibles, cantidades positivas 1–100, precio histórico por línea (nombre/precio al pedir), total BigDecimal calculado en el servidor (nunca confiado al cliente), transiciones `PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO` y `CANCELADO` desde pendiente/en preparación. Pantallas ES en `templates/pedidos/`.
3. **Reservas** — Mesas (nombre único, capacidad 1–50, activa, solo ADMINISTRADOR), reservas con fecha (no pasada, America/Bogota), horario (fin > inicio), capacidad (personas ≤ mesa), titulares, anti-superposición por mesa+fecha (cancelada libera el hueco) y cancelación. Pantallas ES en `templates/mesas/` y `templates/reservas/`.

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
- Tras Reservas (final): `Tests run: 33, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`
  - `HomeControllerTest` 2, `MenuIntegrationTest` 9, `PedidoIntegrationTest` 5, `ReservaIntegrationTest` 5, `SigrApplicationTests` 1, `UsuarioIntegrationTest` 11.
  - Comando: `.\mvnw.cmd test` con `JAVA_HOME=C:\Program Files\Java\jdk-21` (Java 21.0.2, Spring Boot 3.5.16). Probado 2026-11-06 con H2 en memoria (tests) — la BD archivo `data/sigrdb.mv.db` no se borró ni reinició.

## Commits (rama `main`, árbol limpio salvo este documento)

- `d29fea44eb79192b05cbb0a99ad2004fbb52e49d` — `feat(pedidos): ... 28/28 BUILD SUCCESS (LB-SIGR-1.0 candidata)`
- `9dc9bad90b0600768bf93f2b2bb9d7c1c34fd0b2` — `feat(reservas): ... 33/33 BUILD SUCCESS (LB-SIGR-1.0 candidata)`

Historial previo conservado: `237f060`, `5285f29`, `b7d01b0`, `13430e9`, `3fc326c`, `0ec61b3`, `c9e286d`.

## Pendientes (no implementados)

- **Facturación, Caja y Reportes**: no implementados por orden explícita. No hay rutas `/facturas`, `/caja` ni `/reportes`.
- Aprobación de línea base LB-SIGR-1.0 y creación de `v1.0.0`: no aprobados ni creados.
- Publicación en GitHub: no realizada (sin remoto).
- Servicios de pago: ninguno instalado. Secretos: ninguno registrado (solo `SIGR_ADMIN_PASSWORD` local vía entorno).

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
4. **Mesas/Reservas**: como ADMINISTRADOR, `/reservas` → «Cargar mesas de demostración» → `/reservas/nueva` (fecha futura, horario coherente, personas ≤ capacidad) → detalle `/reservas/{id}` → cancelar. Como CLIENTE, verificar que solo ve lo suyo y que un ID ajeno da 404.
