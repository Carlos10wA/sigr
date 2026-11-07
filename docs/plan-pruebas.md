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

## Reservas (2026-11-06)
| Prueba (`ReservaIntegrationTest`) | Resultado real |
|---|---|
| `crearValidaCapacidadFechaYHorario` — capacidad excedida, fin≤inicio y fecha pasada rechazados; titular CLIENTE forzado | OK |
| `previeneSuperposicionYPermiteCancelada` — solapamiento rechazado, colindante permitido, hueco libre tras cancelar | OK |
| `cancelarYClienteSoloLoSuyoIdor404` — detalle propio 200, ajeno 404, cancelar propia → CANCELADA, lista solo propia | OK |
| `meseroYAdminVeTodasYMesasSoloAdmin` — mesero crea/ve, mesas 403 para mesero, admin 200, anónimo redirige | OK |
| `mesasDemoIdempotente` — 2ª carga 0 nuevas, contiene Mesa 1–4 + Terraza 1 | OK |

Resumen: `Tests run: 33, Failures: 0, Errors: 0 — BUILD SUCCESS` (2026-11-06). Pedidos y catálogo siguen pasando.

## Claridad Reservas y Mesas (2026-11-06)
| Prueba (`ReservaIntegrationTest`) | Resultado real |
|---|---|
| `claridadHorariosNumeracionYFechas` — formulario con listas 24 h + ayuda, sin `type=time`, selector «Mesa — N personas»; listado con «N.º de reserva»+dd/MM/yyyy+HH:mm; detalle idéntico; POST con hora/minutos crea y fin≤inicio se rechaza | OK |
| `mesasDemoRepetibleSinDuplicarPorHttp` — doble POST `/mesas/cargar-demo` no duplica | OK |

Resumen: `Tests run: 35, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Selección de mesa por disponibilidad (2026-11-06)
| Prueba (`ReservaIntegrationTest`) | Resultado real |
|---|---|
| `disponibilidadFiltraCapacidadYCruces` — cruce bloquea, colindante/fechas/mesas distintas permiten, capacidad filtra, cancelada libera y queda en historial | OK |
| `sinDisponiblesInformaYPermiteCambiar` — sin hueco avisa «No hay mesas disponibles…» | OK |
| `cambioDeHorarioLimpiaSeleccionInvalida` — al cambiar a horario cruzado la mesa ocupada no aparece | OK |
| `errorConservaDatosIntroducidos` — POST cruzado re-render con error + fecha/horas/personas conservadas | OK |
| `previeneSuperposicionYPermiteCancelada` — incluye horario idéntico 12:00-14:00 rechazado | OK |

Resumen: `Tests run: 39, Failures: 0, Errors: 0 — BUILD SUCCESS` (2026-11-06). Bloqueo pesimista por mesa en el `crear` para solicitudes simultáneas.

## Configuración de mesas e integración (2026-11-06)
| Prueba (`ReservaIntegrationTest`, BD aislada) | Resultado real |
|---|---|
| `tresMesasMesa1OcupadaOfrece2y3` — capacidad 4, Tres 1 ocupada 12:00-16:42, búsqueda 13:00-14:00 para 4 ofrece Tres 2 y 3 (servicio + HTTP) | OK |
| `configuracionAmpliarReduceProtegeReactiva` — ampliar crea faltantes únicos, reducir protegida rechaza con explicación, cancela+reactiva conserva historial, listado con resumen, 403 mesero | OK |

Resumen: `Tests run: 41, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Indicadores de reservas (2026-11-06)
| Prueba | Resultado real |
|---|---|
| `SituacionReservaTest` 6 — antes del inicio (Próxima), inicio exacto/durante (En curso), fin exacto/después (Finalizado), cancelada siempre en todos los momentos | OK |
| `indicadoresTemporalesEnListadoYDetalle` — listado con Leyenda + Próxima y detalle Próxima→Cancelada tras cancelar, persistido intacto | OK |

Resumen: `Tests run: 48, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Colores y vencimiento automático (2026-11-06)
| Prueba | Resultado real |
|---|---|
| `SituacionReservaTest.canceladaPorVencimientoSeDistingue` — etiqueta y clase roja para vencimiento; En curso verde, Próxima naranja | OK |
| `vencimientoAutomaticoConRelojControlado` — antes del fin no vence, fin exacto vence con motivo VENCIMIENTO, repetir idempotente | OK |
| `vencimientoNoCancelaFuturaNiManualPrevia` — futura (fin dia 09:00 a las 20:00 del día anterior) intacta, manual previa intacta | OK |

Resumen: `Tests run: 51, Failures: 0, Errors: 0 — BUILD SUCCESS`. Programada cada 60 s + al iniciar (Bogota), bloqueo por mesa.

## Facturación (2026-11-07)
| Prueba (`FacturacionIntegrationTest`, BD aislada) | Resultado real |
|---|---|
| `emitirRequiereEntregadoYPendientePago` — pendiente/en preparación/listo rechazados; entregado emite con `Pendiente de pago` | OK |
| `duplicadoSeImpideYReabreExistente` — 2ª emisión informa y redirige al existente sin crear otro | OK |
| `precioHistoricoYTotal66Mil` — 2×28.000 + 2×5.000 = 66.000 COP, precio actual no altera histórico, cliente propio 200/ajeno 404/emitir 403, facturado no se cancela | OK |
| `rechazaCanceladosYPendientes` — cancelado y pendiente rechazados | OK |

Resumen: `Tests run: 55, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Ubicación del restaurante (2026-11-07)
| Prueba (`UbicacionIntegrationTest`, BD aislada) | Resultado real |
|---|---|
| `soloAdminConfiguraYCiudadExigeSuZona` — MESERO 403, ADMIN 200, Bogotá con zona México rechazada | OK |
| `comprobanteGuardaCopiaYCambioNoAlteraEmitido` — Bogotá al emitir, cambio a Medellín no altera emitido, vista sin `(America/Bogota)` | OK |
| `bloqueaCambioDeZonaConPendientes` — zona distinta con pendiente bloqueada explicada, misma zona permite | OK |

Resumen: `Tests run: 58, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Corrección /configuracion (Cali, 2026-11-07)
| Prueba (`UbicacionIntegrationTest.caliMuestraSoloSuZonaSinOtraCiudad`) | Resultado real |
|---|---|
| POST Cali con país/zona manipulados → servidor deriva Colombia/America-Bogota; GET muestra solo lectura sin «Bucaramanga — America/Bogota» | OK |

Resumen final: `Tests run: 59, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Caja y pagos (2026-11-07)
| Prueba (`CajaIntegrationTest`, BD aislada) | Resultado real |
|---|---|
| `escenario66000EfectivoYCierre` — base 100.000 + 66.000 con 70.000 → cambio 4.000, esperado 166.000, PAGADO y render con método | OK |
| `permisosYAperturaUnicaYPagoSinCaja` — MESERO 403, pago sin caja y segunda apertura rechazados, duplicado rechazado | OK |
| `tarjetaYTransferenciaNoAumentanEfectivo` — tarjeta/transferencia no suben efectivo esperado | OK |
| `validacionesEfectivoReferenciaYCierreConDiferencia` — recibido bajo y referencia vacía rechazados, diferencia exige observación | OK |

Resumen final: `Tests run: 63, Failures: 0, Errors: 0 — BUILD SUCCESS`.

## Migración ENUM (2026-11-07)
| Prueba (`MigracionEstadoPagoTest`, BD aislada) | Resultado real |
|---|---|
| `migracionIdempotenteYRegresion66000` — migración sobre VARCHAR sin error, escenario 66.000/70.000/4.000, PAGADO, 1 pago, datos conservados | OK |

Resumen final: `Tests run: 64, Failures: 0, Errors: 0 — BUILD SUCCESS`. Verificación local con app en marcha no posible (bloqueo de archivo); migración se aplica al reiniciar y se verifica entonces.

## Claridad de cierre de Caja (2026-11-07)
| Prueba (`CajaIntegrationTest.cierreMuestraResumenCon166000YFaltante`) | Resultado real |
|---|---|
| Panel con base/efectivo/tarjeta/transferencia/esperado, etiqueta nueva, ayuda de conteo y diferencia estimada | OK |

Resumen final: `Tests run: 65, Failures: 0, Errors: 0 — BUILD SUCCESS`. Caso base 100.000 + 66.000 = 166.000; 66.000 contado → faltante 100.000.

## Pendiente (próximas etapas)
Pedidos, reservas, facturación duplicada, caja/reportes, recorrido completo. Ver matriz de trazabilidad.
Estado: **pendiente de ejecución** (no inventado).
