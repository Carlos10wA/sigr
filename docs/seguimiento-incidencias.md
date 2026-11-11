# Seguimiento de incidencias — SIGR

Solo incidencias reales con solución y/o commit relacionado. Repositorio: https://github.com/Carlos10wA/sigr (la gestión de Issues se realizará allí).

## Incidencias históricas resueltas

| # | Descripción | Estado | Solución | Commit |
|---|---|---|---|---|
| I-01 | Cobrar un comprobante fallaba por ENUM local `comprobantes.estado` que solo admitía `PENDIENTE_DE_PAGO` (error 22030 al pagar) | Resuelta | Migración controlada idempotente al iniciar (`MigracionEstadoPago`): convierte a `VARCHAR(30)` conservando datos/relaciones/restricciones, sin borrado ni create-drop; pago transaccional (intento fallido revierte pago+estado) + prueba de regresión (66.000, recibido 70.000, cambio 4.000, PAGADO, 1 pago) | `8d7a6b5` |
| I-02 | Errores 500 al renderizar catálogo por relaciones LAZY sin `@EntityGraph` y expresión de precio Thymeleaf | Resuelta | `@EntityGraph` donde se renderiza, corrección de expresión, `error.html` genérico en español y prueba de renderizado con datos demo | (suite en su momento) |
| I-03 | Cali mostraba zona duplicada/incorrecta al elegir ciudad | Resuelta | Ciudad como única selección; país y zona solo lectura derivados en servidor desde catálogo; cambio de zona bloqueado si hay confirmadas futuras | `7ac3239`, `441949e` |
| I-04 | Clave demo obsoleta de Etapa 1 en el working tree | Resuelta | Eliminada; admin inicial solo vía `SIGR_ADMIN_PASSWORD` | (Etapa 2) |

## Mejoras pendientes (no incidencias)

- Verificación manual con carga y concurrencia real (reservas/caja).
- Publicación en GitHub y etiqueta `v1.0.0` (pendientes).
- Aprobación de LB-SIGR-1.0 (candidata).

## Procedimiento futuro

1. Registrar un Issue en GitHub describiendo el cambio y su impacto.
2. Analizar impacto (requisitos, datos, permisos, pruebas).
3. Implementar en una rama desde `main`, con commits atómicos por cambio coherente.
4. Ejecutar `.\mvnw.cmd test` con BD aislada y registrar resultados en `docs/plan-pruebas.md`.
5. Revisar (código y documentación: CHANGELOG, matriz, estado).
6. Integrar a `main`, actualizar versión/documentación y solo entonces etiquetar.

Jenkins se menciona como **alternativa futura** (pipeline Maven); no se afirma su uso.
