# Adopción de mascotas

Aplicación web académica para registrar mascotas encontradas y consultarlas en un catálogo. El proyecto demuestra construcción multimódulo con Maven, pruebas automáticas, integración continua y despliegue local mediante Jenkins.

## Funcionalidades

- Registrar una mascota encontrada.
- Mostrar fotografía, nombre, especie, sexo, edad aproximada y descripción.
- Buscar por especie, sexo y rango de edad con filtros combinables.
- Informar búsquedas sin resultados y permitir limpiar los filtros.
- Mostrar una imagen local de respaldo cuando una fotografía externa no carga.

Las especies disponibles son Perro, Gato y Otro. El sexo puede ser Macho, Hembra o Desconocido. El nombre y la edad son opcionales; la URL HTTP/HTTPS de la fotografía es obligatoria.

## Tecnologías

| Componente | Versión o uso |
| --- | --- |
| Java | 21 |
| Spring Boot | 4.0.9 |
| Maven | Proyecto multimódulo |
| Thymeleaf | Vistas HTML |
| Spring Data JPA | Persistencia |
| H2 | Base en archivo; memoria durante integración |
| JUnit, Surefire y Failsafe | Pruebas unitarias y de integración |
| Jenkins | CI/CD local en Windows |

No se utiliza Docker. Jenkins y la aplicación se ejecutan directamente en Windows.

## Estructura

```text
adopcion-mascotas/
├── pom.xml
├── Jenkinsfile
├── registro-mascotas/
│   ├── pom.xml
│   └── src/
├── catalogo-web/
│   ├── pom.xml
│   └── src/
├── scripts/windows/
│   ├── Deploy-AdopcionMascotas.ps1
│   └── Test-AdopcionMascotas.ps1
└── docs/
    └── evidencias-ci-cd.md
```

- `registro-mascotas`: modelo, validaciones, repositorio JPA y servicio de registro.
- `catalogo-web`: aplicación ejecutable, controladores, filtros, vistas y dependencia hacia `registro-mascotas`.
- `Jenkinsfile`: pipeline declarativo versionado.
- `scripts/windows`: despliegue con rollback y comprobación HTTP.

## Requisitos

- Windows.
- JDK 21 con `JAVA_HOME` configurado.
- Maven 3.9.12 disponible como `mvn`.
- Git.
- Jenkins 2.580.2 LTS para reproducir el pipeline local verificado.

Comprobar el entorno desde PowerShell:

```powershell
java -version
mvn -version
git --version
```

`mvn -version` debe informar que Maven está utilizando Java 21.

## Obtener y construir el proyecto

```powershell
git clone https://github.com/jromero04/adopcion-mascotas.git
Set-Location adopcion-mascotas
mvn clean verify
```

`mvn clean verify` construye los dos módulos, ejecuta 23 pruebas unitarias y 1 prueba de integración, y genera el JAR ejecutable:

```text
catalogo-web\target\catalogo-web-1.0.0-SNAPSHOT.jar
```

Comandos útiles:

```powershell
# Pruebas unitarias, sin integración
mvn test -DskipITs=true

# Integración con Failsafe, omitiendo las unitarias
mvn verify -DskipUnitTests=true

# Empaquetado sin repetir pruebas ya verificadas
mvn package -DskipUnitTests=true -DskipITs=true
```

## Ejecutar en desarrollo

Jenkins utiliza el puerto 8080. Para poder ejecutar ambos procesos simultáneamente, se recomienda usar el puerto 8082 para desarrollo:

```powershell
$env:SERVER_PORT = '8082'
$env:ADOPCION_DB_URL = 'jdbc:h2:file:./datos/adopcion-dev'
java -jar .\catalogo-web\target\catalogo-web-1.0.0-SNAPSHOT.jar
```

Abrir:

- Inicio: `http://localhost:8082/`
- Registro: `http://localhost:8082/mascotas/nueva`
- Catálogo y filtros: `http://localhost:8082/mascotas`

Para detener la aplicación, presionar `Ctrl+C` en la consola. Las variables anteriores afectan únicamente esa sesión de PowerShell.

## Separación de ambientes y datos

| Ambiente | Puerto | Base de datos |
| --- | ---: | --- |
| Desarrollo recomendado | 8082 | `./datos/adopcion-dev` |
| Pruebas de integración | Aleatorio | H2 en memoria `adopcion-integracion` |
| Producción local simulada | 8081 | `C:\ProgramData\AdopcionMascotas\produccion\data\adopcion-prod` |
| Jenkins | 8080 | No utiliza la base de la aplicación |

La base de producción simulada está fuera del workspace de Jenkins para conservar los datos entre construcciones y despliegues.

## Despliegue local en Windows

Después de construir el JAR, ejecutar desde la raíz del repositorio:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\windows\Deploy-AdopcionMascotas.ps1 `
  -ArtifactPath .\catalogo-web\target\catalogo-web-1.0.0-SNAPSHOT.jar `
  -JavaHome $env:JAVA_HOME
```

El script:

1. copia el artefacto fuera del workspace;
2. conserva un respaldo del JAR anterior;
3. inicia la aplicación en el puerto 8081 con una H2 persistente;
4. espera una respuesta HTTP 200;
5. restaura y vuelve a iniciar la versión anterior si la nueva no queda saludable.

La comprobación independiente se puede ejecutar con:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass `
  -File .\scripts\windows\Test-AdopcionMascotas.ps1 `
  -Uri http://127.0.0.1:8081/
```

Los archivos desplegados se organizan debajo de `C:\ProgramData\AdopcionMascotas\produccion` en las carpetas `app`, `backup`, `data`, `logs` y `run`.

## Configurar Jenkins

La configuración verificada utiliza Jenkins como servicio local de Windows.

### Herramientas globales

En **Administrar Jenkins → Herramientas**, configurar:

- JDK con nombre exacto `jdk-21` y ruta a un JDK 21.
- Maven con nombre exacto `maven-3.9.12` y ruta a Maven 3.9.12.

Los nombres deben coincidir con los declarados en el `Jenkinsfile`.

### Job Pipeline

1. Crear un job de tipo **Pipeline**.
2. Seleccionar **Pipeline script from SCM**.
3. Elegir Git como SCM.
4. Configurar `https://github.com/jromero04/adopcion-mascotas.git`.
5. Usar las credenciales de Jenkins si el repositorio las requiere; no guardar tokens en Git.
6. Indicar `*/main` como **Branch Specifier**.
7. Mantener `Jenkinsfile` como **Script Path**.
8. Guardar y ejecutar una primera construcción manual.

Esa primera ejecución carga el disparador versionado `pollSCM('H/5 * * * *')`. Luego Jenkins consulta Git aproximadamente cada cinco minutos y construye cuando detecta una revisión nueva de `main`.

### Etapas

```text
Checkout
  → Verificar entorno
  → Compilar
  → Pruebas unitarias
  → Pruebas de integración
  → Empaquetar
  → Desplegar
  → Comprobar despliegue
```

Jenkins publica los XML de Surefire y Failsafe mediante JUnit, y archiva el JAR con huella digital. Si la compilación o una prueba falla, las etapas posteriores no se ejecutan y la versión desplegada anteriormente permanece funcionando.

## Trabajo colaborativo

- Crear ramas cortas para cada cambio.
- Abrir pull requests hacia `main`.
- Solicitar revisión de la otra integrante antes del merge.
- No hacer push directo a `main`.
- No versionar `target/`, bases H2, logs, credenciales ni configuración personal del IDE.

El repositorio tiene una ruleset activa sobre `main` que exige pull request, una aprobación y resolución de conversaciones, descarta aprobaciones obsoletas y bloquea eliminaciones y force pushes. La validación funcional del bloqueo se registra en el documento de evidencias.

## Evidencias

Los resultados de pruebas, construcciones de Jenkins, disparadores automáticos, despliegues, rollback y demostración de falla controlada están registrados en [docs/evidencias-ci-cd.md](docs/evidencias-ci-cd.md).

La rama `demo/falla-controlada-pipeline` contiene una prueba intencionalmente fallida utilizada únicamente como evidencia. No debe integrarse a `main`.
