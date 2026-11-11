# Procedencia y licencia de fotografías del catálogo (Etapa 3) — HISTÓRICO, REEMPLAZADO

> **2026-11-06:** Las 12 ilustraciones generadas listadas abajo fueron **reemplazadas por fotografías reales con licencia Creative Commons** (ver `docs/creditos-imagenes.md`, documento vigente). Este archivo se conserva solo como historial de la Etapa 3; no presenta las ilustraciones como fotografías.

Las 13 imágenes están en `src/main/resources/static/images/platos/` y se sirven localmente (`/images/**`), sin enlaces externos.

| Archivo | Plato | Procedencia | Licencia |
|---|---|---|---|
| empanadas.jpg | Empanadas | Generadas originalmente para SIGR (programa Java BufferedImage) | CC0 - dominio público, uso libre |
| ceviche.jpg | Ceviche | ídem | CC0 |
| arepas.jpg | Arepas rellenas | ídem | CC0 |
| bandeja-paisa.jpg | Bandeja paisa | ídem | CC0 |
| pollo-plancha.jpg | Pollo a la plancha | ídem | CC0 |
| pasta-alfredo.jpg | Pasta alfredo | ídem | CC0 |
| jugo-mango.jpg | Jugo de mango | ídem | CC0 |
| cafe.jpg | Café de la casa | ídem | CC0 |
| limonada.jpg | Limonada natural | ídem | CC0 |
| torta-chocolate.jpg | Torta de chocolate | ídem | CC0 |
| flan.jpg | Flan | ídem | CC0 |
| helado.jpg | Helado | ídem | CC0 |
| placeholder.jpg | Imagen de respaldo | ídem (SIGR) | CC0 |

Si el usuario no tiene foto, el formulario guarda `placeholder.jpg` y la vista usa `onerror` hacia `placeholder.jpg` + `alt` con el nombre del plato. El texto alternativo es el nombre del plato.
