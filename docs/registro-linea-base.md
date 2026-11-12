# Registro de línea base — SIGR

## Registro histórico (2026-11-05)

- Identificador: **LB-SIGR-1.0**
- Estado en ese momento: **CANDIDATA — pendiente de aprobación**
- Fecha registro: 2026-11-05
- Rama: `main`
- Hash del commit: `c9e286de72a25de0309fa67a1de4c3c8e3f54d50`
- Componentes: Etapa 1 verificada (estructura inicial) + Etapa 2 (usuarios JPA, autenticación DB, permisos, BCrypt).
- Resultados: Etapa 1 `Tests run: 4`; Etapa 2 `Tests run: 12`; arranque Tomcat 8080 OK; GET / y /login 200.
- Versiones: Java 21.0.2, Spring Boot 3.5.16, Maven 3.9.11, H2 2.3.232, Hibernate 6.6.53.Final, Tomcat 10.1.55, Spring Security 6.5.11, Spring Framework 6.2.19.
- Observaciones (históricas): módulos de negocio pendientes. Sin etiqueta v1.0.0. Sin URL GitHub en esa fecha.
- Aprobación/firma: pendiente en esa fecha.

## Aprobación (11/11/2026)

- Identificador: **LB-SIGR-1.0**
- Estado: **APROBADA para alcance demostrativo** (11/11/2026).
- Rama: `main`
- Commit de código aprobado: `45cca343f34f2e55f5da86dfacfd0fe278534381`
- Commit documental de cierre: posterior a la aprobación (este commit).
- Responsable de validación y aprobación: autor del proyecto (titular de la licencia MIT en `LICENSE.txt`).
- Resultado de pruebas (previamente reportado, no nueva ejecución): 71 ejecutadas, 0 fallos, 0 errores, 0 omitidas.
- Comprobación manual: el pedido 34 cambió de Listo a Entregado y la segunda pestaña actualizó su estado sin recargar manualmente (sondeo verificado).
- Revisión visual del Excel de Cobros realizada y aprobada.
- Repositorio publicado: https://github.com/Carlos10wA/sigr
- Limitaciones aceptadas: carga, concurrencia e impresión; no certificado para producción.
- Etiqueta `v1.0.0`: no creada.
