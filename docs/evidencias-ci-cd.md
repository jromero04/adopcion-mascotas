# Evidencias de integración y despliegue continuos

## Alcance

Este documento registra las verificaciones reales realizadas hasta el 11/11/2026 sobre el pipeline de CI/CD del proyecto. Las ejecuciones exitosas, los ajustes preventivos y las demostraciones controladas se presentan por separado para no describir pruebas planificadas como incidentes reales.

## Entorno verificado

| Componente | Configuración |
| --- | --- |
| Sistema operativo | Windows |
| Jenkins | 2.580.2 LTS, ejecutado como servicio local |
| Java | JDK 21, herramienta de Jenkins `jdk-21` |
| Maven | 3.9.12, herramienta de Jenkins `maven-3.9.12` |
| Repositorio | `https://github.com/jromero04/adopcion-mascotas.git` |
| Rama construida | `main` |
| Disparador | Consulta periódica de Git mediante `pollSCM('H/5 * * * *')` |
| Aplicación desplegada | `http://127.0.0.1:8081/` |
| Directorio de despliegue | `C:\ProgramData\AdopcionMascotas\produccion` |

Jenkins, la aplicación de desarrollo y la aplicación desplegada son ejecuciones separadas. La versión desplegada utiliza el puerto 8081 y una base H2 persistente fuera del workspace de Jenkins.

## Secuencia del pipeline

El `Jenkinsfile` ejecuta las siguientes etapas en orden:

| Etapa | Evidencia esperada |
| --- | --- |
| Checkout | Obtiene la revisión configurada desde GitHub. |
| Verificar entorno | Muestra las versiones efectivas de Java y Maven. |
| Compilar | Compila todos los módulos sin ejecutar pruebas. |
| Pruebas unitarias | Ejecuta las pruebas de Surefire. |
| Pruebas de integración | Ejecuta Failsafe durante `verify`. |
| Empaquetar | Genera y archiva el JAR ejecutable de `catalogo-web`. |
| Desplegar | Actualiza la instalación local y espera una respuesta HTTP 200; restaura el respaldo si la versión nueva falla. |
| Comprobar despliegue | Realiza un smoke test HTTP independiente y comprueba contenido de la página. |

Las etapas son secuenciales. Un error de compilación o pruebas impide alcanzar el despliegue.

## Resultados de pruebas

La ejecución completa realiza 24 pruebas automáticas:

| Tipo | Módulo | Cantidad | Resultado verificado |
| --- | --- | ---: | --- |
| Unitarias | `registro-mascotas` | 7 | Sin fallos |
| Unitarias | `catalogo-web` | 16 | Sin fallos |
| Integración | `catalogo-web` | 1 | Sin fallos |
| **Total** | Ambos módulos | **24** | **23 unitarias y 1 de integración** |

La prueba de integración inicia la aplicación con H2 en memoria, registra una mascota mediante HTTP y comprueba su persistencia, aparición en el catálogo y búsqueda correspondiente. Los resultados XML de Surefire y Failsafe se publican con JUnit en Jenkins.

El artefacto generado y archivado es `catalogo-web/target/catalogo-web-1.0.0-SNAPSHOT.jar`.

## Historial real de Jenkins

| Construcción | Inicio | Causa | Revisión y rama | Alcance | Resultado |
| ---: | --- | --- | --- | --- | --- |
| #1 | 09/11/2026 11:12 | Manual | `09cc4c1`, `feature/ci-integracion-jenkins` | Pipeline base, pruebas y artefacto | `SUCCESS` |
| #2 | 09/11/2026 11:33 | Manual | `37f8db2`, `main` | Pipeline base desde `main` | `SUCCESS` |
| #3 | 11/11/2026 07:39 | Manual | `cf512cb`, `feature/pipeline-despliegue` | Pipeline completo con despliegue y smoke test | `SUCCESS` |
| #4 | 11/11/2026 07:51 | Manual | `0cb2aa3`, `main` | Primera ejecución completa desde `main` | `SUCCESS` |
| #5 | 11/11/2026 07:57 | `SCMTriggerCause` | `d57a6e7`, `main` | Primera activación automática comprobada | `SUCCESS` |
| #6 | 11/11/2026 08:02 | `SCMTriggerCause` | `694321b`, `main` | Segunda activación automática comprobada | `SUCCESS` |

En las construcciones #3 a #6, el log registra tanto `Despliegue correcto` como `Comprobacion correcta. HTTP 200`. La construcción #5 demuestra que el cambio de la PR #8 fue detectado sin presionar **Construir ahora**. La #6 confirma que el comportamiento se repitió después de la PR #9.

## Despliegue, persistencia y recuperación

El despliegue utiliza `scripts/windows/Deploy-AdopcionMascotas.ps1` y conserva fuera del workspace:

- el JAR activo y su respaldo;
- la base H2 de producción simulada;
- el PID de la aplicación;
- los logs de salida y error.

Se verificó un segundo despliegue y los datos registrados permanecieron disponibles. También se realizó una prueba controlada con un artefacto inválido: la versión nueva no alcanzó el estado saludable, el script restauró el JAR anterior y la aplicación volvió a responder HTTP 200 conservando los datos. Esta fue una demostración planificada del rollback, no un incidente de producción.

## Problemas reales y correcciones

### Historial de Jenkins

Hasta la construcción #6 no se registraron compilaciones fallidas en Jenkins: las seis finalizaron con `SUCCESS`. Por esta razón no se atribuye al pipeline ningún incidente que no haya ocurrido.

### Selección inicial de rama

Antes de verificar el disparador, el job todavía apuntaba a `feature/pipeline-despliegue`. Se diagnosticó leyendo la configuración del job y se cambió manualmente el especificador a `*/main`. Después de una ejecución manual desde `main`, Jenkins cargó el disparador versionado y las construcciones #5 y #6 se iniciaron automáticamente.

### Compatibilidad con Windows PowerShell 5.1

Durante la revisión previa a la ejecución completa se detectó que el script utilizaba una sobrecarga de `String.Contains` no disponible en Windows PowerShell 5.1. Se sustituyó por `String.IndexOf` con comparación explícita. Fue una corrección preventiva: no llegó a provocar una falla de Jenkins.

### Java utilizado en una verificación local

Una ejecución local inicial de `mvn clean verify` tomó Java 17 y falló con `release version 21 not supported`. El diagnóstico fue un `JAVA_HOME` distinto del requerido; al seleccionar el JDK 21 configurado para Jenkins, la misma verificación finalizó correctamente. Este problema ocurrió en la consola local de comprobación, no en una construcción de Jenkins.

## Trazabilidad en GitHub

| PR | Cambio principal |
| ---: | --- |
| #3 | Pruebas de integración y pipeline base |
| #4 | Disparador periódico de Jenkins |
| #5 | Despliegue local reversible en Windows |
| #6 | Despliegue y smoke test dentro del pipeline |
| #7 | Registro del pipeline completo exitoso |
| #8 | Registro de la configuración del disparador |
| #9 | Evidencia de la activación automática |

## Evidencia pendiente

La tarea siguiente realizará una falla controlada en una rama de demostración. Deberá comprobar y documentar que:

1. la compilación o las pruebas fallan de forma visible;
2. Jenkins no ejecuta las etapas de despliegue ni de comprobación;
3. la versión desplegada anteriormente continúa respondiendo;
4. el cambio de demostración no se integra a `main`;
5. la evidencia se identifica como una prueba planificada y no como un incidente real.
