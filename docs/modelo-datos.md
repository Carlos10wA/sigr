# Modelo y estructura de base de datos — Etapa 2

H2 en archivo `data/sigrdb` (`spring.jpa.hibernate.ddl-auto=update`). Esquema creado por JPA.

## Tabla `usuarios`
| Columna | Tipo | Nulo | Descripción |
|---|---|---|---|
| id | BIGINT AI PK | no | Identificador. |
| nombre_completo | VARCHAR | no | Nombre completo. |
| usuario | VARCHAR(60) único | no | Nombre de usuario único. |
| contrasena | VARCHAR(100) | no | Hash BCrypt (nunca texto plano). |
| rol | VARCHAR(20) | no | `ADMINISTRADOR`, `MESERO`, `CLIENTE`. |
| activo | BOOLEAN | no | `true` permite iniciar sesión. |
| fecha_creacion | TIMESTAMP | no | Fecha de creación (en @PrePersist). |

## Tablas Etapa 3

### `categorias`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| nombre | VARCHAR(60) único | Entradas, Platos principales, Bebidas, Postres. |
| activa | BOOLEAN | `false` oculta del catálogo y bloquea nuevos platos. |
| fecha_creacion | TIMESTAMP | Creación (@PrePersist). |

### `platos`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| nombre | VARCHAR(120) | Nombre del plato. |
| descripcion | VARCHAR(255) | Descripción breve. |
| categoria_id | BIGINT FK → categorias | Categoría activa al crear/editar. |
| precio | DECIMAL(10,2) | COP BigDecimal, `> 0`. |
| foto | VARCHAR(200) | Ruta local `/images/platos/*.jpg`; respaldo `placeholder.jpg`. |
| disponible | BOOLEAN | `false` = no disponible temporalmente (visible). |
| activo | BOOLEAN | `false` = desactivado (oculto, conserva historial futuro). |
| fecha_creacion | TIMESTAMP | Creación. |

## Tablas Pedidos

### `pedidos`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| usuario_id | BIGINT FK → usuarios | Solicitante creador (dueño para CLIENTE). |
| estado | VARCHAR(20) | `PENDIENTE`, `EN_PREPARACION`, `LISTO`, `ENTREGADO`, `CANCELADO`. |
| total | DECIMAL(12,2) | Suma de líneas, calculada en el servidor. |
| fecha_creacion | TIMESTAMP | Creación. |
| fecha_actualizacion | TIMESTAMP | Último cambio de estado. |

### `pedido_detalles`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| pedido_id | BIGINT FK → pedidos | Pedido padre (cascada). |
| plato_id | BIGINT FK → platos | Plato pedido (referencia, no filtra precio). |
| nombre_plato | VARCHAR(120) | Foto del nombre al pedir. |
| precio_unitario | DECIMAL(10,2) | Foto del precio COP al pedir. |
| cantidad | INT | 1–100. |
| subtotal | DECIMAL(12,2) | `precio_unitario × cantidad`. |

Reglas Pedidos: solo `ACTIVO+DISPONIBLE`; misma plato en dos líneas se fusiona sumando cantidades; transiciones `PENDIENTE→EN_PREPARACION→LISTO→ENTREGADO`, `CANCELADO` solo desde pendiente/en preparación (CLIENTE solo cancela lo suyo); CLIENTE con ID ajeno recibe 404.

## Tablas Reservas

### `mesas`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| nombre | VARCHAR(60) único | `Mesa 1`…, `Terraza 1`. |
| capacidad | INT | 1–50. |
| activa | BOOLEAN | `false` bloquea nuevas reservas. |
| fecha_creacion | TIMESTAMP | Creación. |

### `reservas`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| usuario_id | BIGINT FK → usuarios | Creador (dueño para CLIENTE). |
| mesa_id | BIGINT FK → mesas | Mesa reservada. |
| cliente_nombre | VARCHAR(100) | Titular (CLIENTE: forzado a su nombre). |
| fecha | DATE | Fecha (no pasada, America/Bogota). |
| hora_inicio | TIME | Inicio. |
| hora_fin | TIME | Fin (`> inicio`). |
| cantidad_personas | INT | 1 ≤ … ≤ mesa.capacidad. |
| estado | VARCHAR(20) | `CONFIRMADA`, `CANCELADA`. |
| fecha_creacion | TIMESTAMP | Creación. |

Reglas Reservas: superposición = misma mesa + misma fecha + `CONFIRMADA` con `inicio_nuevo < fin_existente && fin_nuevo > inicio_existente`; cancelada no bloquea. CLIENTE con ID ajeno recibe 404. Solo ADMINISTRADOR gestiona mesas. Columnas `motivo_cancelacion` (`MANUAL`/`VENCIMIENTO`) y `fecha_cancelacion` para distinguir «Cancelada» vs «Cancelada por vencimiento».

## Tablas Facturación

### `comprobantes`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| numero | VARCHAR único | `FAC-000001` estable, desde el id. |
| pedido_id | BIGINT único FK → pedidos | Pedido ENTREGADO facturado (uno solo). |
| cliente_nombre | VARCHAR(100) | Foto del cliente al emitir. |
| cliente_usuario | VARCHAR(60) | Foto del usuario al emitir. |
| fecha_emision | TIMESTAMP | Emisión America/Bogota. |
| total | DECIMAL(12,2) | Suma BigDecimal servidor, sin impuestos/propinas/descuentos. |
| estado | VARCHAR(30) | `PENDIENTE_DE_PAGO` (sin cobro). |
| fecha_creacion | TIMESTAMP | Creación. |

### `comprobante_lineas`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT AI PK | Identificador. |
| comprobante_id | BIGINT FK → comprobantes | Comprobante padre (cascada). |
| nombre_plato | VARCHAR(120) | Foto al facturar. |
| precio_unitario | DECIMAL(10,2) | Foto COP al facturar. |
| cantidad | INT | Cantidad del pedido. |
| subtotal | DECIMAL(12,2) | `precio_unitario × cantidad`. |

Reglas Facturación: solo ENTREGADO, sin editar/eliminar, pedido facturado no se cancela; duplicado con bloqueo pesimista + única en BD; «Comprobante académico — sin validez fiscal».

### `comprobantes` (ubicación, snapshot)
Columnas `pais`, `ciudad`, `direccion` y `zona_horaria` copiadas de `configuracion_restaurante` al emitir (vista: «Bogotá, Colombia»; sin texto técnico «(America/Bogota)»).

## Tabla Configuración

### `configuracion_restaurante`
| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGINT PK = 1 | Fila única. |
| pais | VARCHAR(60) | País configurado por ADMINISTRADOR. |
| ciudad | VARCHAR(60) | Ciudad del catálogo (con su zona). |
| direccion | VARCHAR(200) | Dirección del restaurante. |
| zona_horaria | VARCHAR(60) | ID interno (p. ej. `America/Bogota`) para fechas. |
| fecha_actualizacion | TIMESTAMP | Último cambio. |

Reglas Ubicación: inicial Colombia/Bogotá/America-Bogota, sin geolocalización; cambio de zona con confirmadas pendientes se bloquea sin reinterpretar horarios.

## Relaciones y reglas (Etapa 3)
- `platos.categoria_id` → `categorias.id` (N:1). Desactivar categoría/plato conserva referencias (no se elimina).
- Desactivado (`activo=false`) ≠ no disponible (`disponible=false`): el primero oculta; el segundo avisa pero no se puede pedir.
- Zona horaria: America/Bogota. Moneda: COP con BigDecimal.
- Mesas, pedidos, reservas, facturas, pagos y caja: próximas etapas (diagrama completo pendiente).
