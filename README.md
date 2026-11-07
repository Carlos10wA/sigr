# SIGR — Sistema Integral de Gestión de Restaurante

Proyecto académico — Gestión del Software, Unidad 2: Gestión de la configuración.
Institución: Tecnológica del Oriente. Estudiante: Carlos Alberto Ayala Larrahondo.
Docente: Carlos Carrascal Avendaño.

## Estado
Línea base candidata **LB-SIGR-1.0** — Etapa 1 verificada, Etapa 2 (usuarios) y Etapa 3 (menús/platos) implementadas. **Línea base completa pendiente de validación y aprobación.** Sin etiqueta `v1.0.0`.

## Tecnologías y versiones
- Java **21.0.2 LTS** (`C:\Program Files\Java\jdk-21`), proyecto configurado para Java 21.
- Spring Boot **3.5.16** (última estable 3.5.x verificada en Maven Central, sin SNAPSHOT/RC).
- Spring MVC + Thymeleaf (HTML5, CSS3 responsive), Spring Security + BCrypt/CSRF, Spring Data JPA, Validation.
- H2 **2.3.232** en archivo: `jdbc:h2:file:./data/sigrdb`.
- Maven **3.9.11** + Maven Wrapper (`mvnw.cmd`) para Windows.
- JUnit 5 / Spring Boot Test / Spring Security Test.
- Git rama `main`. GitHub: pendiente URL real.

## Requisitos
- JDK 21, Windows 10/11, navegador.
- No requiere servicios de pago ni BD externa.

## Configurar Java 21 en PowerShell (reproducible)
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
java -version
javac -version
.\mvnw.cmd -v
```
> Nota: sin esto, `java` puede resolver a 17 y `javac`/Maven a 23/25 según PATH.

## Crear el administrador inicial (Etapa 2)
La contraseña inicial del administrador **no está en el código, docs, logs ni Git**. En PowerShell, **en la misma terminal** donde ejecutará la app, defínala usted mismo (no la comparta en el chat):
```powershell
$env:SIGR_ADMIN_PASSWORD="CambieEstoPorSuClaveLocal"
.\mvnw.cmd spring-boot:run
```
- Al arrancar con la tabla `usuarios` vacía, se crea el usuario `admin` (rol ADMINISTRADOR, activo) con la contraseña cifrada BCrypt.
- Si la variable no está definida, no se crea ningún usuario y el registro indica que la defina y reinicie.
- La variable vive solo en la sesión PowerShell; al cerrar la terminal desaparece. Para cambiarla entre reinicios, vuelva a definirla antes de `spring-boot:run`.
- Usuario demo de Etapa 1 eliminado: ya no existe en memoria ni en pantallas.

Permisos (servidor): solo `ADMINISTRADOR` accede a `/usuarios/**` (listar/crear/editar/activar-desactivar). `MESERO` y `CLIENTE` reciben 403 en esas rutas. No se puede desactivar ni quitar el rol al único administrador activo.

## Instalación y ejecución local
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:SIGR_ADMIN_PASSWORD="CambieEstoPorSuClaveLocal"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
Abrir http://localhost:8080 — el administrador ya existe en BD si ya definió la variable alguna vez; si no, defínala en esta terminal y reinicie. Plaza de persistencia: `data/sigrdb.mv.db`.

## Pruebas
```powershell
.\mvnw.cmd test
```
Resultados: Etapa 1 `4/4`, Etapa 2 `12/12`, 403 `14/14`, Etapa 3 `20/20`, ajuste catálogo `22/22`, fotografías reales `23/23`, Pedidos `28/28`, Reservas `33/33`, claridad Reservas/Mesas `35/35`, selección de mesa `39/39`, configuración de mesas `41/41`, indicadores `48/48`, colores + vencimiento `51/51`, Facturación `55/55`, ubicación `58/58` (config admin, snapshot, bloqueo de zona). Ver `docs/plan-pruebas.md` y `docs/estado-avance.md`.

## Estructura
```
pom.xml, mvnw, mvnw.cmd, .mvn/wrapper/
src/main/java/.../model, repository, service, controller, dto, security, config
src/main/resources/templates (usuarios, platos, categorias, fragments, error), static/css + static/images/platos, application.properties
src/test/java/... (UsuarioIntegrationTest, MenuIntegrationTest), src/test/resources/application.properties
docs/
data/ (H2 local, no versionar)
```

## Alcance (6 módulos)
Usuarios ✅ (Etapa 2) — Menús ✅ (Etapa 3) — Pedidos ✅ — Reservas ✅ — Facturación ✅ (académica, pendiente de pago) — Caja/Reportes pendientes (no implementados).

## Catálogo de demostración (Etapa 3)
- Tras iniciar sesión como ADMINISTRADOR, abra `/platos` y pulse **Cargar datos de demostración**. Crea si faltan las 4 categorías y los 12 platos (3 por categoría), con fotografías reales locales CC (`docs/creditos-imagenes.md`); si ya existen, no duplica ni sobrescribe. Para revisar en vivo, reinicie con su `SIGR_ADMIN_PASSWORD` habitual y repita el botón; luego anote la captura.
- Desactivado ≠ no disponible: desactivar oculta del catálogo (conserva historial futuro); no disponible deja ver el plato con aviso.

## Limitaciones
- Panel principal aún sin datos de negocio (etapas siguientes).
- El commit histórico de Etapa 1 contenía una clave demo obsoleta; desde Etapa 2 está eliminada del working tree y de las pantallas y ningún usuario depende de ella.
