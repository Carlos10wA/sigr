# Plan de pruebas y resultados — SIGR

## Etapa 1 (2026-11-05)
| Prueba | Comando | Resultado real |
|---|---|---|
| `SigrApplicationTests.contextLoads` | `.\mvnw.cmd test` | OK |
| `HomeControllerTest.homePermitida...` | `.\mvnw.cmd test` | OK (200, vista index) |
| `HomeControllerTest.loginAccesible` | `.\mvnw.cmd test` | OK (200, vista login) |
| `HomeControllerTest.loginConCredenciales...` | `.\mvnw.cmd test` | OK (redirección 3xx) |
| Arranque real | `.\mvnw.cmd spring-boot:run` | Tomcat en 8080, `Started SigrApplication` |
| Acceso HTTP | `Invoke-WebRequest http://localhost:8080/` y `/login` | 200 en ambos |

Resumen: `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS`.

## Etapa 2 (2026-11-05)
| Prueba (`UsuarioIntegrationTest`) | Resultado real |
|---|---|
| `loginCorrectoRedirige` | OK (3xx) |
| `loginIncorrectoFalla` | OK (`/login?error`) |
| `usuarioDesactivadoNoPuedeIniciarSesion` | OK (`/login?error`) |
| `usuarioDuplicadoRechazado` | OK (excepción duplicado) |
| `noAdminNoPuedeAccederUsuarios` | OK (403 CLIENTE) |
| `adminPuedeAccederUsuarios` | OK (200, vista listado) |
| `anonimoNoPuedeAccederUsuarios` | OK (redirección login) |
| `ultimoAdministradorNoSeDesactiva` | OK (excepción último admin) |
| `persistenciaTrasLimpiarContexto` | OK (hash BCrypt `$2`, reload tras `clear`) |
| 3 tests previos (`HomeControllerTest` 2 + `SigrApplicationTests` 1) | OK |

Resumen final: `Tests run: 12, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS` (2026-11-05). Persistencia tras reinicio real: verificada por `persistenciaTrasLimpiarContexto` + revisión manual pendiente de captura tras reiniciar la app.

## Ajuste 403 (2026-11-06)
| Prueba | Resultado real |
|---|---|
| `meseroRecibe403ConPaginaPersonalizada` — MESERO `/usuarios` → 403 + `forwardedUrl` | OK |
| `noAdminNoPuedeAccederUsuarios` — CLIENTE `/usuarios` → 403 + página ES | OK |
| `pagina403PersonalizadaEnEspanolSinDetallesTecnicos` — `GET /error/403` 403, texto «Acceso denegado…», sin Whitelabel/stacktrace | OK |
| `adminPuedeAccederUsuarios` — ADMINISTRADOR `/usuarios` → 200 | OK |
Resumen: `Tests run: 14, Failures: 0 — BUILD SUCCESS`.

## Etapa 3 — Menús (2026-11-06)
| Prueba (`MenuIntegrationTest`) | Resultado real |
|---|---|
| `crearEditarDesactivarPlato` | OK (CRUD + desactivado oculta del catálogo) |
| `validacionPrecioYCategoria` | OK (categoría desactivada rechazada) |
| `filtroCatalogoPorNombreYCategoria` | OK (búsqueda + filtro) |
| `demoIdempotenteNoDuplica` | OK (2ª carga 0 nuevos, ≥12 platos) |
| `persistenciaTrasLimpiarContexto` | OK (reload tras `clear`, BigDecimal COP) |
| `permisosTresRolesEnCatalogoYGestion` | OK (MESERO/CLIENTE leen 200, gestión 403; ADMIN 200; precio 0 → errores form) |

Resumen: `Tests run: 20, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Ajuste catálogo 500 (2026-11-06)
| Prueba (`MenuIntegrationTest`) | Resultado real |
|---|---|
| `catalogoRenderizaDatosDemoConNombresCategoriasPreciosYFotos` — demo + `GET /platos` 200 con Empanadas/Entradas/$12.000,00 COP//images/platos/empanadas.jpg + Bandeja paisa y detalle | OK |
| `errorGenericoEnEspanolSinWhitelabel` — ruta inexistente 404 + `GET /error` (Accept HTML + attrs) 404 con «Ocurrió un error», sin Whitelabel | OK |
| 6 pruebas previas de Menús | OK |

Resumen: `Tests run: 22, Failures: 0, Errors: 0 — BUILD SUCCESS` (2026-11-06). Diagnóstico verificado: con catálogo vacío no se ejecutaba la expresión de precio (por eso pasaba antes); al cargar la demo, Thymeleaf fallaba al parsearla y además la categoría LAZY podía fallar fuera de transacción. Corrección: `${...}` en precio + `@EntityGraph` en `PlatoRepository`.

## Fotografías reales (2026-11-06)
| Prueba (`MenuIntegrationTest`) | Resultado real |
|---|---|
| `fotografiasRealesCarganEnCatalogoYDetalle` — GET `/images/platos/*.jpg` 200 ×12 (empanadas, ceviche, arepas, bandeja-paisa, pollo-plancha, pasta-alfredo, jugo-mango, cafe, limonada, torta-chocolate, flan, helado) + catálogo con `limonada.jpg`/`cafe.jpg` y `loading="lazy"` | OK |

Resumen: `Tests run: 23, Failures: 0, Errors: 0 — BUILD SUCCESS`. Fotografías CC locales 800 px (83–268 KB); ver `docs/creditos-imagenes.md`. Wikimedia devolvió 429 en una descarga (limonada); se tomó vía Openverse/Flickr (CC BY) y se documentó.

## Pedidos (2026-11-06)
| Prueba (`PedidoIntegrationTest`) | Resultado real |
|---|---|
| `crearCalculaTotalEnServidorConSnapshot` — 2 × $12.000 = $24.000,00; cambio posterior de precio no altera total histórico | OK |
| `rechazaNoDisponibleDesactivadoYCantidadInvalida` — no disponible, desactivado, cantidad 0/negativa/inexistente rechazados | OK |
| `transicionesValidasEInvalidas` — PENDIENTE→EN_PREPARACION→LISTO→ENTREGADO; saltos y cancelar tras listo rechazados | OK |
| `clienteSoloVeYCancelarLosSuyosEIdorDevuelve404` — detalle propio 200 con datos, detalle ajeno 404, lista solo propia, cliente no avanza | OK |
| `meseroVeTodosYAdminGestionaTodos` — mesero ve todos y crea, admin ve todos, anónimo redirige a login | OK |

Resumen: `Tests run: 28, Failures: 0, Errors: 0 — BUILD SUCCESS`. Verificación de catálogo previa: búsqueda, filtros y detalle siguen pasando (23 previos).

## Pendiente (próximas etapas)
Pedidos, reservas, facturación duplicada, caja/reportes, recorrido completo. Ver matriz de trazabilidad.
Estado: **pendiente de ejecución** (no inventado).
