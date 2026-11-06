# Procedimiento de solicitud y control de cambios

1. Crear Issue en GitHub (cuando se configure la URL) describiendo el cambio.
2. Crear rama desde `main`, implementar con commits atómicos por etapa.
3. Ejecutar `.\mvnw.cmd test` y registrar resultados en `docs/plan-pruebas.md`.
4. Actualizar CHANGELOG, matriz de trazabilidad e inventario.
5. Solicitar revisión/aprobación; registrar hash y decisión en `docs/registro-linea-base.md`.
6. Abrir PR contra `main` (relacionada con el Issue), fusionar y etiquetar solo tras aceptación.
Jenkins: mejora futura opcional; el proyecto ya es compatible con un pipeline Maven.
