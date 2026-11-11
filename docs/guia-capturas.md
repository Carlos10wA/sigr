# Guía de capturas — evidencias

Todas deben tomarse de ejecución real en Windows. No se fabrican capturas ni resultados; evitar contraseñas y datos personales.

| # | Captura | Requisito | Resultado a comprobar | Apartado para el informe |
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
| E-11 | Catálogo `/platos` tras Cargar demo (12 platos) | RQ-Menús | Tarjetas con foto, precio COP, búsqueda/filtro | Módulos / menús |
| E-12 | Detalle `/platos/{id}` y formulario admin | RQ-Menús | Editar/desactivar/no disponible diferenciados | Módulos / menús |
| E-13 | `/categorias` como ADMINISTRADOR | RQ-Menús | 4 categorías activas | Módulos / menús |
| E-14 | Error genérico (ruta inexistente) | RQ-Error | «Ocurrió un error…» con estado HTTP, sin Whitelabel | Errores |
| E-15 | Pedido entregado → Emitir comprobante | RQ-Facturación | «Comprobante de venta — Sin validez fiscal», pendiente de pago, FAC- y total COP | Facturación |
| E-16 | Detalle comprobante imprimible | RQ-Facturación | Líneas históricas, total, botón Imprimir | Facturación |
| E-17 | `/reportes/cobros` 66.000 | RQ-Reportes | Total, pagados y desglose por método con filtros | Reportes |
| E-18 | `/reportes/cobros/csv` | RQ-Reportes | CSV con escape y protección anti-fórmulas | Reportes |
| E-19 | `/reportes/cobros/xlsx` | RQ-Reportes | Excel «Descargar Excel»: título, resumen 66.000, tabla con total y nota | Excel |
| E-20 | `/pedidos` y `/pedidos/{id}` con script `actualizar-estado.js` | RQ-Pedidos | Estado se actualiza por sondeo 10 s sin recargar; sin formularios tocados | Pedidos |
| E-21 | `git log --oneline` (≥3 commits reales) | RQ-Git | Historial con 3+ commits descriptivos | Repositorio |
| E-22 | Repositorio e Issues (tras publicar) | RQ-GitHub | Pendiente de publicación (URL: https://github.com/Carlos10wA/sigr) | Repositorio |

## Informe separado

El informe académico se elaborará **fuera del repositorio público** (sin portada con datos de institución/docente en la documentación técnica pública). Fuentes técnicas dentro del repo: README, `docs/verificacion-linea-base.md`, `docs/plan-pruebas.md`, `docs/matriz-trazabilidad.md`, `docs/modelo-datos.md` y `docs/acta-linea-base.md`. Borradores académicos, si existieran, se conservan separados de los archivos de publicación sin moverlos fuera del proyecto sin informar. El PDF final se preparará después como `Taller_LineaBase_SIGR.pdf` (máximo 20 páginas, sin portada).
