# Acta de línea base — SIGR

## Identificador y estado

- **Identificador:** LB-SIGR-1.0
- **Estado:** **APROBADA para alcance demostrativo** (11/11/2026). Sin etiqueta `v1.0.0`.
- **Fecha de aprobación:** 11/11/2026.
- **Rama:** `main`
- **Commit de código aprobado:** `45cca343f34f2e55f5da86dfacfd0fe278534381`
- **Commit documental de cierre:** posterior a la aprobación (este commit).
- **Responsable de validación y aprobación:** autor del proyecto (titular de la licencia MIT en `LICENSE.txt`).
- **Etiqueta `v1.0.0`:** no creada. **Repositorio:** https://github.com/Carlos10wA/sigr (publicado).

SIGR es un proyecto demostrativo de gestión de restaurantes, usable como referencia de desarrollo. **No está certificado ni validado para producción.** Los comprobantes no tienen validez fiscal; los pagos son registros internos, sin bancos ni pasarelas.

## Alcance

Usuarios con roles, catálogo de platos/categorías, pedidos con seguimiento automático, reservas sin cruces, comprobantes de venta, caja (apertura/pagos/cierre) y reportes (cobros, platos, reservas, cierres) con CSV, Excel de Cobros e impresión. Ver `docs/verificacion-linea-base.md`.

## Inventario de elementos de configuración

| Grupo | Elementos |
|---|---|
| Código | `src/main/java/.../model, repository, service, controller, dto, security, config` |
| Configuración | `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/`, `src/main/resources/application.properties` |
| Dependencias | Spring Boot 3.5.16, Java 21, H2 2.3.232, Apache POI 5.2.5, Maven 3.9.11 |
| Recursos | `src/main/resources/templates/` (usuarios, platos, categorias, pedidos, reservas, facturas, caja, reportes, configuracion, fragments, error), `static/css`, `static/js`, `static/images/platos/` (12 fotos con licencia, créditos en `docs/creditos-imagenes.md`) |
| Pruebas | `src/test/java/...` (JUnit 5, BD H2 en memoria aislada) |
| Documentación | `README.md`, `CHANGELOG.md`, `docs/` (modelo-datos, plan-pruebas, matriz-trazabilidad, guia-capturas, estado-avance, manual-despliegue, verificacion-linea-base, seguimiento-incidencias, acta-linea-base, creditos-imagenes, inventario-configuracion, registro-linea-base, notas-avance) |

**Versionado vs. datos operativos locales:** se versionan el esquema (migración controlada `MigracionEstadoPago`, idempotente, sin borrado) y la configuración; los datos operativos locales (`data/sigrdb.mv.db`, usuarios, pedidos, pagos) **no deben publicarse** (`.gitignore` excluye `data/`, `*.mv.db`, `target/`, logs y secretos). La BD local no debe borrarse ni recrearse con create-drop.

## Criterios de aceptación

- Suite completa en verde con BD aislada (ver `docs/plan-pruebas.md`).
- Permisos de servidor por rol (incl. IDOR 404 y 403 propio).
- Exportaciones consistentes con las consultas (CSV/Excel/impresión).
- Documentación reproducible y licencia MIT del código propio.
- Sin secretos versionados; contraseñas solo vía `SIGR_ADMIN_PASSWORD`.

## Resultados verificables

- Suite: **71 pruebas, 0 fallos, 0 errores, 0 omitidas** (`.\mvnw.cmd test`, ver `docs/plan-pruebas.md`). Referencia histórica: 69 pruebas antes de la corrección I-05.
- Historial con commits reales; tres seleccionados:
  - `eb3261b` — Reportes (cobros/platos/reservas/cierres, CSV, imprimible).
  - `4b92d4f` — Excel de Cobros (.xlsx Apache POI).
  - `8d7a6b5` — Migración ENUM→VARCHAR de estado de pago.
- Comprobación manual: el pedido 34 cambió de Listo a Entregado y la segunda pestaña actualizó su estado sin recargar manualmente (sondeo verificado el 11/11/2026).
- Revisión visual del Excel de Cobros realizada y aprobada.
- Repositorio publicado: https://github.com/Carlos10wA/sigr

## Limitaciones aceptadas

- No validado para producción ni certificado.
- Limitaciones documentadas de carga, concurrencia e impresión aceptadas por el responsable de validación (11/11/2026).
- Sin facturación electrónica fiscal ni integración con bancos o pasarelas de pago; pagos registrados manualmente; comprobantes sin validez fiscal.

## Campos pendientes (no inventar)

- Etiqueta `v1.0.0`: no creada (decisión pendiente posterior a la aprobación).
- Aprobación de línea base: registrada el 11/11/2026 para alcance demostrativo.

## Procedimiento para identificar el commit definitivo

1. Tras la aprobación, fijar en `main` el commit verificado: `git rev-parse HEAD` (código aprobado: `45cca343f34f2e55f5da86dfacfd0fe278534381`).
2. Registrar el hash completo en `docs/registro-linea-base.md` y aquí.
3. Crear la etiqueta `v1.0.0` sobre ese commit solo tras una decisión posterior.
4. El repositorio está publicado en https://github.com/Carlos10wA/sigr.
