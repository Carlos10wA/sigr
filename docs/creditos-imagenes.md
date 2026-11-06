# Créditos de imágenes del catálogo (fotografías reales)

Las 12 fotografías del catálogo son **fotografías reales con permiso de reutilización** (Creative Commons), guardadas dentro del proyecto en `src/main/resources/static/images/platos/` para que funcionen sin conexión (se sirven localmente en `/images/**`, sin enlaces externos). No hay ilustraciones presentadas como fotografías.

Descarga: miniaturas de **800 px de ancho** desde Wikimedia Commons / Openverse (Flickr) para web (83–268 KB por foto). Se muestran con `object-fit: cover` y proporción uniforme (`aspect-ratio: 4/3`; tarjetas 180 px de alto) en catálogo y detalle. Cada imagen usa `alt` con el nombre del plato y respaldo `onerror` hacia `placeholder.jpg`.

Atribución: en licencias CC BY / CC BY-SA se requiere dar crédito al autor, enlazar la licencia y, en BY-SA, compartir igual. En CC0 / dominio público la atribución no es obligatoria, pero se registra igual.

| Archivo local | Plato | Título original | Autor | Licencia | Fuente |
|---|---|---|---|---|---|
| `empanadas.jpg` | Empanadas | File:Empanada - Stu Spivack.jpg | stu_spivack | [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0) | https://commons.wikimedia.org/wiki/File:Empanada_-_Stu_Spivack.jpg |
| `ceviche.jpg` | Ceviche | File:Ceviche del Perú.jpg | Manuel González Olaechea | [CC BY 3.0](https://creativecommons.org/licenses/by/3.0) | https://commons.wikimedia.org/wiki/File:Ceviche_del_Per%C3%BA.jpg |
| `arepas.jpg` | Arepas rellenas | File:Arepa de Chócolo.jpg | John Alexis Guerra Gómez | [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0) | https://commons.wikimedia.org/wiki/File:Arepa_de_Ch%C3%B3colo.jpg |
| `bandeja-paisa.jpg` | Bandeja paisa | File:Bandeja paisa 30062011.jpg | Dtarazona | [CC BY-SA 3.0](https://creativecommons.org/licenses/by-sa/3.0) | https://commons.wikimedia.org/wiki/File:Bandeja_paisa_30062011.jpg |
| `pollo-plancha.jpg` | Pollo a la plancha | File:Pesto Chicken and Grilled Zucchini.jpg | Infrogmation of New Orleans | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) | https://commons.wikimedia.org/wiki/File:Pesto_Chicken_and_Grilled_Zucchini.jpg |
| `pasta-alfredo.jpg` | Pasta alfredo | File:Chicken fettuccine alfredo.JPG | Dllu | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) | https://commons.wikimedia.org/wiki/File:Chicken_fettuccine_alfredo.JPG |
| `jugo-mango.jpg` | Jugo de mango | File:Mango juice at Agasi, Lajpat Nagar, Delhi (2025-10-04).jpg | Contrapunctus-1 | [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0) | https://commons.wikimedia.org/wiki/File:Mango_juice_at_Agasi,_Lajpat_Nagar,_Delhi_(2025-10-04).jpg |
| `cafe.jpg` | Café de la casa | File:A small cup of coffee.JPG | Julius Schorzman | [CC BY-SA 2.0](https://creativecommons.org/licenses/by-sa/2.0) | https://commons.wikimedia.org/wiki/File:A_small_cup_of_coffee.JPG |
| `limonada.jpg` | Limonada natural | Lemonade | wwarby | [CC BY 2.0](https://creativecommons.org/licenses/by/2.0) | https://www.flickr.com/photos/26782864@N00/11782417484 (vía Openverse) |
| `torta-chocolate.jpg` | Torta de chocolate | File:Piece of chocolate cake on a white plate decorated with chocolate sauce.jpg | Daria Yakovleva | [CC0](http://creativecommons.org/publicdomain/zero/1.0/deed.en) | https://commons.wikimedia.org/wiki/File:Piece_of_chocolate_cake_on_a_white_plate_decorated_with_chocolate_sauce.jpg |
| `flan.jpg` | Flan | File:Homemade Flan.jpg | Cary Bass | [CC BY-SA 2.5](https://creativecommons.org/licenses/by-sa/2.5) | https://commons.wikimedia.org/wiki/File:Homemade_Flan.jpg |
| `helado.jpg` | Helado | File:Strawberry Ice Cream Sundae (26853326174).jpg | Sam Howzit | [CC BY 2.0](https://creativecommons.org/licenses/by/2.0) | https://commons.wikimedia.org/wiki/File:Strawberry_Ice_Cream_Sundae_(26853326174).jpg |

Notas:

- Las 12 fotos se muestran en el catálogo (`/platos`) y el detalle (`/platos/{id}`) con `alt` = nombre del plato.
- `placeholder.jpg` es la imagen de respaldo local (ilustración genérica SIGR, CC0, ver `docs/fotos-catalogo.md`); no corresponde a un plato.
- Wikimedia Commons devolvió 429 en una descarga (limonada); se tomó de Openverse/Flickr con la misma licencia CC BY, documentada arriba.
- Anteriores ilustraciones generadas (BufferedImage, CC0) fueron reemplazadas el 2026-11-06; se conservaron en respaldo local fuera del proyecto, no se versionan como fotografías.
