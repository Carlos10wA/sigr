# Changelog

## [0.0.1-SNAPSHOT] — Etapa 1 (2026-11-05)
### Agregado
- Estructura inicial Spring Boot 3.5.16 / Java 21 con Maven Wrapper 3.9.11.
- Spring MVC, Thymeleaf, Security (form + BCrypt + CSRF), JPA, Validation, H2 en archivo.
- Página inicial (`index`) y login (`login`), CSS base.
- Tests: `SigrApplicationTests`, `HomeControllerTest` (4 pruebas).
- Git inicializado en `main`.

### Pendiente
- Módulos Menús, Pedidos, Reservas, Facturación, Caja/Reportes.
- Documentación completa y aprobación de LB-SIGR-1.0.

## [0.0.1-SNAPSHOT] — Etapa 2 (2026-11-05)
### Agregado
- Entidad `Usuario` JPA + `Rol` (ADMINISTRADOR/MESERO/CLIENTE), repositorio, servicio y DTOs con validación.
- Autenticación desde BD (`DbUserDetailsService` + BCrypt); usuarios desactivados no ingresan.
- CRUD de usuarios solo ADMINISTRADOR (listar/crear/editar/activar-desactivar), CSRF, logout POST, mensajes ES.
- Protección del último administrador activo (desactivar / quitar rol/editar rechazado).
- Administrador inicial vía variable de entorno `SIGR_ADMIN_PASSWORD` (`DataInitializer`); sin claves en código/docs/Git.
- Pantallas responsive ES: `/usuarios`, `/usuarios/nuevo`, `/usuarios/{id}/editar`, fragmento nav con usuario/rol.
- Tests `UsuarioIntegrationTest`: 12 totales (login OK/incorrecto, desactivado, duplicado, permisos rol, último admin, persistencia BCrypt).

### Pendiente
- Módulos Menús, Pedidos, Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata).

## [0.0.1-SNAPSHOT] — Ajuste 403 (2026-11-06)
### Cambiado
- Página personalizada 403 en español («Acceso denegado…») con enlace al panel, vía `error/403.html` + `/error/403` (estado HTTP 403, sin Whitelabel ni detalles técnicos). Restricción conservada.
- Tests: CLIENTE y MESERO → 403 con `forwardedUrl`, página `/error/403` verificada (14 totales).

### Pendiente
- Módulos Menús, Pedidos, Reservas, Facturación, Caja/Reportes.
- Aprobación de LB-SIGR-1.0 (sigue candidata).
