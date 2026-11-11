# Manual de despliegue local — SIGR

Guía operativa para instalar, ejecutar, respaldar y recuperar el proyecto en Windows. La línea base está en estado CANDIDATA (aprobación pendiente) y no se ha creado la etiqueta `v1.0.0`; estos pasos son para desarrollo local, no despliegue en producción.

## Versiones reales y requisitos

| Componente | Versión verificada |
|---|---|
| Java (JDK) | 21.0.2 LTS (ruta local `C:\Program Files\Java\jdk-21`) |
| Spring Boot | 3.5.16 (Maven Central, sin SNAPSHOT/RC) |
| Maven | 3.9.11 (vía Maven Wrapper `mvnw.cmd`) |
| H2 | 2.3.232 (archivo local) |
| Hibernate | 6.6.53.Final |
| Tomcat | 10.1.55 |
| Spring Security | 6.5.11 / Spring Framework 6.2.19 |
| Apache POI | 5.2.5 (exportación Excel de Cobros) |
| Sistema | Windows 10/11, PowerShell 5.1+ |

**Verificado:** arranque `spring-boot:run` en `http://localhost:8080` (`Started SigrApplication`).
**Pendiente:** despliegues públicos, certificación o integraciones externas — no afirmadas.

## Clonado

```powershell
git clone https://github.com/Carlos10wA/sigr
cd sigr
```

La URL oficial del repositorio es https://github.com/Carlos10wA/sigr.

## Configuración de Java en Windows + Maven Wrapper

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
java -version; javac -version; .\mvnw.cmd -v
```

> Si `java`/`javac` no responden a 21, revise `JAVA_HOME` y `PATH` en esta misma terminal.

## Contraseña segura de arranque: SIGR_ADMIN_PASSWORD

La contraseña inicial del administrador **nunca forma parte del código, la documentación versionada, los registros ni Git**. Usted define una contraseña local en su propia terminal (las cadenas de este manual son valores de ejemplo, no claves reales):

```powershell
$env:SIGR_ADMIN_PASSWORD="CambieEstoPorSuClaveLocal"
.\mvnw.cmd spring-boot:run
```

### Primer administrador vs. acceso existente

| Situación | Comportamiento |
|---|---|
| Tabla `usuarios` vacía + variable definida | Se crea el único usuario `admin` (ADMINISTRADOR) con la clave en BCrypt. |
| Tabla `usuarios` vacía + variable ausente | No se crea ningún usuario; la aplicación indica definirla y reiniciar. |
| BD ya inicializada (arranque posterior) | La variable **no** modifica usuarios existentes. Inicie sesión con el usuario/clave ya persistidos. |

La variable vive solo en la sesión de PowerShell; al cerrar la terminal desaparece.

## Pruebas (BD aislada)

```powershell
.\mvnw.cmd test
```

Las pruebas usan H2 en memoria (`src/test/resources/application.properties`) y **no tocan `data/sigrdb.mv.db`**. No use `create-drop` sobre la BD local.

## Inicio, acceso y parada

```powershell
.\mvnw.cmd spring-boot:run
```

- Acceso local: http://localhost:8080 → `/login` (usuario `admin` o uno creado por el administrador).
- Parada: `Ctrl+C` en la terminal. **Detenga siempre la aplicación antes de respaldar o manipular la BD.**

## Persistencia, respaldo y restauración

- BD local en archivo: `data/sigrdb.mv.db` (no versionada; `.gitignore` excluye `data/`).
- **Respaldo:** con la aplicación detenida, copie `data/sigrdb.mv.db` a un respaldo con nombre y fecha (por ejemplo `sigrdb-2026-11-08.mv.db`) en una carpeta fuera del proyecto o en el control de versiones del respaldo del responsable.
- **Restauración sin sobrescrituras inadvertidas:** antes de restaurar, verifique que la aplicación está detenida; copie el respaldo fuera de `data/` como copia de trabajo; compare tamaños/fechas; solo entonces reemplace `data/sigrdb.mv.db` y arranque para validar el acceso con el usuario existente. Jamás restaure en caliente ni use `create-drop`.

## Solución de incidencias habituales

| Síntoma | Causa probable | Acción |
|---|---|---|
| `java` no es 21 | PATH/JAVA_HOME no fijados en la terminal | Reconfigure Java 21 en esta misma sesión (arriba) |
| Sin usuario admin tras arrancar | Variable `SIGR_ADMIN_PASSWORD` ausente con BD vacía | Defínala y reinicie una sola vez |
| Acceso denegado en rutas admin | Rol no ADMINISTRADOR | Ingrese con `admin`; mensaje 403 esperado para MESERO/CLIENTE |
| BD bloqueada al respaldar | Aplicación en marcha | Detenga con `Ctrl+C` y repita |
| Whitelabel / error 500 | Revisar registro y `docs/seguimiento-incidencias.md` | Diagnóstico y corrección versionada (no edición en caliente) |
| Página de error sin permisos | Ruta protegida | Controle el rol; la seguridad es de servidor, no por interfaz |

## Publicación

El repositorio está disponible en https://github.com/Carlos10wA/sigr. La publicación del historial completo y la aprobación de la línea base son acontecimientos distintos: la línea base LB-SIGR-1.0 sigue CANDIDATA y no se ha creado `v1.0.0`.
