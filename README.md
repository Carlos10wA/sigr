# SIGR — Sistema Integral de Gestión de Restaurante

Proyecto académico — Gestión del Software, Unidad 2: Gestión de la configuración.
Institución: Tecnológica del Oriente. Estudiante: Carlos Alberto Ayala Larrahondo.
Docente: Carlos Carrascal Avendaño.

## Estado
Línea base candidata **LB-SIGR-1.0** — Etapa 1 completada (estructura inicial verificada).
Pendiente de revisión/aprobación. No se creó etiqueta `v1.0.0` todavía.

## Tecnologías y versiones
- Java **21.0.2 LTS** (`C:\Program Files\Java\jdk-21`), proyecto configurado para Java 21.
- Spring Boot **3.5.16** (última estable 3.5.x verificada en Maven Central, sin SNAPSHOT/RC).
- Spring MVC + Thymeleaf (HTML5, CSS3), Spring Security, Spring Data JPA, Validation.
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

## Instalación y ejecución local
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```
Abrir http://localhost:8080 — usuario demo Etapa 1: `admin` / `admin123` (temporal, se reemplazará por usuarios JPA).

## Pruebas
```powershell
.\mvnw.cmd test
```
Resultados Etapa 1: `Tests run: 4, Failures: 0, Errors: 0` (ver `docs/plan-pruebas.md`).

## Estructura
```
pom.xml, mvnw, mvnw.cmd, .mvn/wrapper/
src/main/java/.../config, controller
src/main/resources/templates, static/css, application.properties
src/test/java/...
docs/
data/ (H2 local, no versionar)
```

## Alcance planificado (6 módulos)
Usuarios, Menús, Pedidos, Reservas, Facturación, Caja y Reportes. Solo la base de la Etapa 1 está implementada; los demás módulos están pendientes.

## Limitaciones de la Etapa 1
- Usuario en memoria (no JPA), sin entidades de negocio todavía.
- Sin datos reales en el panel (se implementa en etapas siguientes).
- El resto de los documentos están en `docs/`; la aprobación de la línea base sigue pendiente.
