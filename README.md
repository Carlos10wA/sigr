# SIGR — Sistema Integral de Gestión de Restaurante

SIGR — Sistema Integral de Gestión de Restaurante.
Aplicación web demostrativa para gestionar usuarios, menú, pedidos, reservas, comprobantes de venta, caja y reportes.

Proyecto demostrativo de gestión de restaurantes que puede utilizarse como referencia de desarrollo. No constituye un sistema certificado ni validado para producción. Los comprobantes no tienen validez fiscal; los pagos se registran manualmente y no hay integración con bancos ni pasarelas de pago.

## Propósito

Gestionar el flujo operativo de un restaurante: autenticación por roles, catálogo de platos, pedidos, reservas de mesa, comprobantes de venta, caja y reportes con exportación (CSV, Excel e impresión).

## Alcance y módulos

Usuarios ✅ — Menús ✅ — Pedidos ✅ (con seguimiento automático del estado) — Reservas ✅ — Facturación ✅ (emisión de comprobantes de venta y consulta de su estado de pago) — Caja ✅ (apertura/pagos/cierre) — Reportes ✅ (Cobros CSV + Excel .xlsx Apache POI 5.2.5 + imprimible).

## Tecnologías y versiones utilizadas

- Java 21.0.2 (LTS). Ruta de ejemplo adaptable: `C:\Program Files\Java\jdk-21`.
- Spring Boot 3.5.16.
- Spring MVC + Thymeleaf (HTML5, CSS3 responsive), Spring Security, Spring Data JPA, Bean Validation.
- H2 2.3.232 en archivo: `jdbc:h2:file:./data/sigrdb`.
- Maven 3.9.11 + Maven Wrapper (`mvnw.cmd`) para Windows.
- Apache POI 5.2.5 (exportación Excel de Cobros).
- JUnit 5 / Spring Boot Test / Spring Security Test.
- Git rama `main`. Repositorio: https://github.com/Carlos10wA/sigr

## Requisitos

- JDK 21, Windows 10/11, navegador.
- No requiere servicios de pago ni base de datos externa.

## Configuración de Java 21 en Windows (reproducible)

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
java -version
javac -version
.\mvnw.cmd -v
```

> Ajuste la ruta del JDK si su instalación está en otra ubicación.

## Instalación y ejecución local

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:SIGR_ADMIN_PASSWORD="CambieEstoPorSuClaveLocal"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Abrir http://localhost:8080.

## Clonado del repositorio

```powershell
git clone https://github.com/Carlos10wA/sigr
cd sigr
```

## Administrador inicial (seguro)

La contraseña inicial del administrador no está en el código, la documentación, los registros ni Git. Es usted quien define una contraseña local en la terminal donde ejecutará la aplicación (nunca use una contraseña compartida):

```powershell
$env:SIGR_ADMIN_PASSWORD="CambieEstoPorSuClaveLocal"
.\mvnw.cmd spring-boot:run
```

- Al arrancar con la tabla `usuarios` vacía, se crea el usuario `admin` (rol ADMINISTRADOR, activo) con la contraseña cifrada con BCrypt.
- Si la variable no está definida, no se crea ningún usuario y la aplicación indica que la defina y reinicie.
- La variable vive solo en la sesión de PowerShell; al cerrar la terminal desaparece. Para cambiarla entre reinicios, vuelva a definirla antes de `spring-boot:run`.
- Primer administrador vs. acceso posterior: el valor de `SIGR_ADMIN_PASSWORD` solo se usa cuando la tabla está vacía (creando el primer administrador). En arranques posteriores la variable no altera los usuarios existentes: inicie sesión con el usuario y la clave ya persistidos en la base de datos.

## Seguridad

- Autenticación: formulario de login con Spring Security.
- Autorización: por rol (ADMINISTRADOR, MESERO, CLIENTE) aplicada en el servidor; rutas protegidas exigen autenticación.
- Protección CSRF habilitada en formularios.
- Contraseñas cifradas con BCrypt (unidireccional).

## Roles y permisos

| Rol | Acceso |
|---|---|
| ADMINISTRADOR | Todo: usuarios, mesas, pedidos, comprobantes, caja, configuración y reportes. |
| MESERO | Pedidos y comprobantes de todos los clientes. Caja y reportes: 403. |
| CLIENTE | Solo sus propios pedidos (IDOR 404 ajenos), reservas y comprobantes pagados propios. Caja y reportes: 403. |

## Pruebas

```powershell
.\mvnw.cmd test
```

La suite usa una base de datos H2 en memoria aislada (no toca `data/sigrdb.mv.db`). Ver `docs/plan-pruebas.md` para el detalle. Ver `docs/verificacion-linea-base.md` para la verificación de requisitos.

## Seguimiento automático de pedidos

El estado se actualiza por sondeo periódico cada 10 s desde el navegador, sin recargar la página: el JS consume `GET /pedidos/{id}/estado` (JSON) y solo toca el texto de estado. No usa WebSockets ni tiempo real. Detalles: sin solicitudes superpuestas, pausado si la pestaña no está visible, conserva formularios y selecciones y mantiene el último estado si falla la conexión. Autorización en servidor: CLIENTE solo sus pedidos, MESERO y ADMINISTRADOR según sus permisos vigentes.

## Estructura

```
pom.xml, mvnw, mvnw.cmd, .mvn/wrapper/
src/main/java/.../model, repository, service, controller, dto, security, config
src/main/resources/templates (usuarios, platos, categorias, pedidos, reservas, facturas, caja, reportes, configuracion, fragments, error), static/css, static/js, static/images/platos, application.properties
src/test/java/... (tests de integración), src/test/resources/application.properties
docs/
data/ (H2 local, no versionar)
```

## Documentación

- `docs/verificacion-linea-base.md` — verificación de requisitos (automatizado/manual, resultados, pendientes).
- `docs/manual-despliegue.md` — instalación, configuración segura, persistencia, respaldo y solución de incidencias.
- `docs/modelo-datos.md` — entidades y relaciones.
- `docs/plan-pruebas.md` — historial y resultados de pruebas.
- `docs/matriz-trazabilidad.md` — requisito → implementación → prueba.
- `docs/guia-capturas.md` — evidencias recomendadas.
- `docs/estado-avance.md` — estado del proyecto.
- `docs/seguimiento-incidencias.md` — incidencias reales y procedimiento de cambios.
- `docs/acta-linea-base.md` — acta de línea base candidata LB-SIGR-1.0 (esquema, sin aprobación).

## Catálogo de demostración

Tras iniciar sesión como ADMINISTRADOR, abra `/platos` y pulse **Cargar datos de demostración**. Crea si faltan las 4 categorías y los 12 platos (3 por categoría), con fotografías reales con licencia explícita (`docs/creditos-imagenes.md`); si ya existen, no duplica ni sobrescribe.

Desactivado ≠ no disponible: desactivar oculta del catálogo (conserva historial futuro); no disponible deja ver el plato con aviso.

## Zona horaria y moneda

- Zona del restaurante configurable por ciudad (valores iniciales: Colombia/Bogotá, `America/Bogota`); país y zona se leen solo desde la configuración en el servidor. Las horas en pantalla usan `dd/MM/yyyy` y `HH:mm`.
- Importes en COP con `BigDecimal`. La base inicial de caja no es un ingreso.

## Limitaciones

- Eficiencia de consultas y prueba de concurrencia bajo carga: pendientes de verificación manual.
- No validado para producción. No implementa facturación electrónica fiscal ni integración con bancos o pasarelas de pago. Los pagos se registran manualmente y los comprobantes no tienen validez fiscal.
- Línea base LB-SIGR-1.0 en estado CANDIDATA; aprobación pendiente. No se ha creado la etiqueta `v1.0.0`.

## Licencia

Licencia MIT (`LICENSE.txt`) aplicada al código propio. Las fotografías del catálogo conservan sus licencias originales y créditos (`docs/creditos-imagenes.md`); las dependencias conservan las suyas.
