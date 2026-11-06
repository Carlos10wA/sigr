# Guía de capturas — evidencias informe

| # | Captura | Requisito | Resultado a comprobar | Apartado informe |
|---|---|---|---|---|
| E-01 | Terminal `java -version` + `.\mvnw.cmd -v` | RQ-01/03 | Java 21.0.2, Maven 3.9.11 | Herramientas / validación |
| E-02 | `.\mvnw.cmd test` BUILD SUCCESS | RQ-01/02 | Tests run 4, 0 fallos | Validación y pruebas |
| E-03 | Arranque `spring-boot:run` Tomcat 8080 | RQ-04 | `Started SigrApplication` | Ejecución inicial |
| E-04 | Navegador http://localhost:8080 | RQ-04 | Página SIGR visible | Ejecución inicial |
| E-05 | Navegador /login con usuario `admin` (clave definida vía SIGR_ADMIN_PASSWORD) | RQ-04 | Acceso y cierre sesión | Módulos / seguridad |
| E-06 | `git status`, `git log --oneline` | RQ-06 | Rama main, commit inicial | Repositorio / versión |
| E-07 | Explorador proyecto `docs/` | RQ-06 | Documentos LB-SIGR-1.0 | Documentación |
| E-08 | Navegador /usuarios como ADMINISTRADOR | RQ-Usuario | Listado, crear/editar/activar-desactivar | Módulos / usuarios |
| E-09 | Navegador /usuarios como MESERO/CLIENTE (403) | RQ-Permisos | «Acceso denegado. Tu usuario no tiene permisos…» + Volver al panel, sin Whitelabel | Permisos |
| E-10 | Navegador desactivar último administrador | RQ-Permisos | Mensaje de protección | Permisos |

Todas deben tomarse de ejecución real en Windows.
