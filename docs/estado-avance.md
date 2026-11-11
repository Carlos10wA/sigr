# Estado de avance — SIGR (2026-11-11)

Proyecto SIGR (Sistema Integral de Gestión de Restaurante): demostrativo, con línea base **LB-SIGR-1.0 candidata** — pendiente de validación/aprobación. Sin etiqueta `v1.0.0`. Repositorio: https://github.com/Carlos10wA/sigr (publicado).

## Implementado

1. Verificación catálogo, Pedidos, Reservas, configuración de mesas, Facturación, ubicación, Caja y pagos, Reportes + Excel de Cobros, seguimiento automático de pedidos (sondeo 10 s), presentación pública sanitizada, licencia MIT.
2. Incidencia I-05 (Listo→Entregado mostraba "Ocurrió un error") corregida en `PedidoController`: sesión nula y excepciones no controladas manejadas; prueba de regresión `PedidoCambioEstadoRegresionTest`.

## Estado de las pruebas

- Suite completa: `Tests run: 71, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`.

## Commits recientes

- `fd76faa` — presentación pública, URL oficial, licencia MIT.
- `928899d` — ajuste presentación Excel de Cobros.
- `2ef7607` — seguimiento automático de pedidos.
- (este commit) — corrección I-05 + documentación.

## Pendientes

- **Reportes**: implementados (`/reportes`, `/reportes/cobros`, `/platos`, `/reservas`, `/cierres` + CSV y Excel de Cobros). Pendiente solo revisión final de línea base y verificación manual (carga, concurrencia real, recorrido completo).
- Aprobación de línea base LB-SIGR-1.0 y creación de `v1.0.0`: no aprobados ni creados.
- Publicación en GitHub: repositorio en https://github.com/Carlos10wA/sigr (publicado). La publicación y la aprobación de la línea base son acontecimientos distintos (aprobación pendiente).
- Servicios de pago: ninguno instalado. Secretos: ninguno registrado (solo `SIGR_ADMIN_PASSWORD` local vía entorno).
- Verificación manual: la presentación del Excel fue revisada visualmente. El seguimiento automático entre ventanas continúa pendiente hasta comprobarlo después de la corrección.
- Licencia MIT aplicada (`LICENSE.txt`); sin datos de institución/docente en presentación pública (informe separado).

## Pasos para iniciar y revisar

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:SIGR_ADMIN_PASSWORD="SuClaveLocal"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Abrir http://localhost:8080. Sin etiqueta `v1.0.0`.
