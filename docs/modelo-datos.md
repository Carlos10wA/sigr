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

## Relaciones y reglas (Etapa 3)
- `platos.categoria_id` → `categorias.id` (N:1). Desactivar categoría/plato conserva referencias (no se elimina).
- Desactivado (`activo=false`) ≠ no disponible (`disponible=false`): el primero oculta; el segundo avisa pero no se puede pedir.
- Zona horaria: America/Bogota. Moneda: COP con BigDecimal.
- Mesas, pedidos, reservas, facturas, pagos y caja: próximas etapas (diagrama completo pendiente).
