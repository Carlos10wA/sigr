# Notas de avance — informe Word

- **Introducción/objetivos:** SIGR gestiona restaurante académico; Unidad 2 control de configuración.
- **Descripción:** monolito por capas Java 21 + Spring Boot 3.5.16 + Thymeleaf + JPA + H2.
- **Componentes línea base LB-SIGR-1.0:** Etapa 1 verificada (estructura inicial, wrapper, arranque 8080) + Etapa 2 (usuarios JPA, BCrypt, roles ADMINISTRADOR/MESERO/CLIENTE, permisos servidor, 12 tests OK). Línea base completa pendiente validación/aprobación.
- **Versionado:** Git `main`, primer commit Etapa 1; v1.0.0 pendiente aprobación.
- **Criterios aceptación:** registrados en matriz de trazabilidad; módulos pendientes.
- **Herramientas:** JDK 21.0.2, Maven 3.9.11, Git 2.51, H2 2.3.232.
- **Documentación:** README, CHANGELOG, manual, inventario, trazabilidad, guía capturas.
- **Validación/aprobación:** pruebas reales OK; aprobación pendiente del responsable.
- **Conclusiones/referencias:** Spring Boot 3.5.16 (Maven Central), Spring Initializr, docs oficiales Spring.
