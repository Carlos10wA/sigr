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

## Relaciones y reglas (Etapa 3)
- `platos.categoria_id` → `categorias.id` (N:1). Desactivar categoría/plato conserva referencias (no se elimina).
- Desactivado (`activo=false`) ≠ no disponible (`disponible=false`): el primero oculta; el segundo avisa pero no se puede pedir.
- Zona horaria: America/Bogota. Moneda: COP con BigDecimal.
- Mesas, pedidos, reservas, facturas, pagos y caja: próximas etapas (diagrama completo pendiente).
