# Manual de instalación y despliegue local (Etapa 1)

1. Instalar JDK 21 (ya presente en `C:\Program Files\Java\jdk-21`).
2. En PowerShell, en `C:\Users\angel\Documents\SIGR`:
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
java -version; javac -version; .\mvnw.cmd -v
```
3. Compilar y probar:
```powershell
.\mvnw.cmd test
```
4. Ejecutar:
```powershell
.\mvnw.cmd spring-boot:run
```
5. Abrir http://localhost:8080 — login demo: `admin` / `admin123`.
6. Detener con Ctrl+C.
BD local: `data/sigrdb.mv.db` (no versionar). Despliegue a producción: fuera de alcance de esta etapa.
