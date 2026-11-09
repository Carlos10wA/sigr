# Acta de línea base — SIGR

## Identificador y estado

- **Identificador:** LB-SIGR-1.0
- **Estado:** **CANDIDATA** — pendiente de validación y aprobación.
- **Fecha de registro:** pendiente (se registrará al aprobar).
- **Rama:** `main`
- **Commit definitivo:** por identificar tras la aprobación (ver procedimiento al final).
- **Aprobación/firma:** pendiente de la persona responsable. Sin firmas ni fechas inventadas.
- **Etiqueta `v1.0.0`:** no creada. **Publicación en GitHub:** pendiente de URL oficial.

SIGR es un proyecto demostrativo de gestión de restaurantes, usable como referencia de desarrollo. **No está certificado ni validado para producción.** Los comprobantes son académicos, sin validez fiscal; los pagos son registros internos, sin bancos ni pasarelas.

## Alcance

Usuarios con roles, catálogo de platos/categorías, pedidos con seguimiento automático, reservas sin cruces, comprobantes académicos, caja (apertura/pagos/cierre) y reportes (cobros, platos, reservas, cierres) con CSV, Excel de Cobros e impresión. Ver `docs/verificacion-linea-base.md`.

## Inventario de elementos de configuración

| Grupo | Elementos |
|---|---|
| Código | `src/main/java/.../model, repository, service, controller, dto, security, config` |
| Configuración | `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/wrapper/`, `src/main/resources/application.properties` |
| Dependencias | Spring Boot 3.5.16, Java 21, H2 2.3.232, Apache POI 5.2.5, Maven 3.9.11 |
| Recursos | `src/main/resources/templates/` (usuarios, platos, categorias, pedidos, reservas, facturas, caja, reportes, configuracion, fragments, error), `static/css`, `static/js`, `static/images/platos/` (12 fotos con licencia, creditos en `docs/creditos-imagenes.md`) |
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

- Suite: **69 pruebas, 0 fallos, 0 errores, 0 omitidas** (`.\mvnw.cmd test`, ver `docs/plan-pruebas.md`).
- Historial con **23 commits** reales (ver `docs/estado-avance.md`): 3 seleccionados —
  - `eb3261b` — Reportes (cobros/platos/reservas/cierres, CSV, imprimible, 67/67): aporta los 4 reportes con rangos y permisos.
  - `4b92d4f` — Excel de Cobros (.xlsx Apache POI, 67/67): aporta la exportación profesional con resumen, filtros y total.
  - `8d7a6b5` — Migración ENUM→VARCHAR de estado de pago (64/64): aporta la corrección sin borrado que desbloqueó los pagos.
- Recursos verificados: .gitignore, licencia, importación histórica (ver `docs/verificacion-linea-base.md`).

## Limitaciones y pendientes

- Verificación manual con carga/concurrencia real y recorrido completo en navegador.
- Publicación en GitHub y etiqueta `v1.0.0` (pendientes).
- No validado para producción ni certificado.

## Campos pendientes (no inventar)

- Fecha de aprobación: pendiente.
- Verificador/aprobador: pendiente de la persona responsable.
- Hash del commit definitivo: se identificará tras la aprobación (procedimiento abajo).

## Procedimiento para identificar el commit definitivo

1. Tras la aprobación, fijar en `main` el commit verificado: `git rev-parse HEAD`.
2. Registrar el hash completo en `docs/registro-linea-base.md` y aquí.
3. Crear la etiqueta `v1.0.0` sobre ese commit solo tras la aprobación.
4. Publicar el historial completo en GitHub según `docs/manual-despliegue.md` (URL pendiente).
