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

## Relaciones y reglas (Etapa 2)
- Sin relaciones externas todavía; el resto (categorías, platos, mesas, pedidos, reservas, facturas, pagos, caja) llega en módulos siguientes con diagrama y diccionario completos.
- Unicidad de `usuario` garantizada en BD. Protección del último administrador activo en `UsuarioService`.
- Zona horaria: America/Bogota. Moneda: COP con BigDecimal (módulos futuros).
