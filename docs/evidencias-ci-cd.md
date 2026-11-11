# Evidencias de integración y despliegue continuos

## Alcance

Este documento registra las verificaciones reales realizadas hasta la construcción #9 del 11/11/2026 sobre el pipeline de CI/CD del proyecto. Las ejecuciones exitosas, los ajustes preventivos y las demostraciones controladas se presentan por separado para no describir pruebas planificadas como incidentes reales.

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
| #7 | 11/11/2026 09:02 | `SCMTriggerCause` | `b13ae44`, `main` | Activación automática tras integrar las evidencias | `SUCCESS` |
| #8 | 11/11/2026 09:12 | Manual | `c77d360`, `demo/falla-controlada-pipeline` | Demostración planificada de una prueba unitaria fallida | `FAILURE` esperado |
| #9 | 11/11/2026 09:16 | Manual | `b13ae44`, `main` | Restauración del job y nueva ejecución completa | `SUCCESS` |

En las construcciones #3 a #7 y #9, el log registra tanto `Despliegue correcto` como `Comprobacion correcta. HTTP 200`. La construcción #5 demuestra que el cambio de la PR #8 fue detectado sin presionar **Construir ahora**. Las #6 y #7 confirman que el comportamiento se repitió después de cambios posteriores.

## Despliegue, persistencia y recuperación

El despliegue utiliza `scripts/windows/Deploy-AdopcionMascotas.ps1` y conserva fuera del workspace:

- el JAR activo y su respaldo;
- la base H2 de producción simulada;
- el PID de la aplicación;
- los logs de salida y error.

Se verificó un segundo despliegue y los datos registrados permanecieron disponibles. También se realizó una prueba controlada con un artefacto inválido: la versión nueva no alcanzó el estado saludable, el script restauró el JAR anterior y la aplicación volvió a responder HTTP 200 conservando los datos. Esta fue una demostración planificada del rollback, no un incidente de producción.

## Problemas reales y correcciones

### Historial de Jenkins

Las construcciones #1 a #7 finalizaron con `SUCCESS`; no se registró una falla espontánea de Jenkins. La construcción #8 terminó en `FAILURE` como parte de una demostración planificada y se documenta como tal, no como un incidente.

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
| #10 | Documento consolidado de evidencias CI/CD |

## Protección de `main`

El 11/11/2026 se consultó la configuración mediante la API de GitHub. La ruleset `Protección de main` aparece activa, apunta a la rama predeterminada y no tiene actores con permiso de bypass. Sus reglas exigen una pull request con una aprobación, descartan aprobaciones cuando se agregan commits y requieren resolver las conversaciones. También bloquean la eliminación de la rama y los force pushes. No se exigen status checks porque el Jenkins local no publica checks en GitHub.

La PR creada desde `docs/verificar-proteccion-main` se utiliza para comprobar funcionalmente que GitHub impide el merge antes de la aprobación de la otra integrante. El resultado de esa comprobación debe registrarse antes de integrar la rama.

## Demostración de falla controlada

La rama temporal `demo/falla-controlada-pipeline` agregó `FallaControladaPipelineTest`, una prueba que falla con el mensaje `FALLA CONTROLADA: verificar que Jenkins no ejecute el despliegue`. La prueba se validó localmente y luego se construyó manualmente en Jenkins. Esta rama no se integró a `main`.

La construcción #8 produjo la siguiente evidencia:

- obtuvo la revisión `c77d360` de la rama de demostración;
- falló en `Pruebas unitarias` por el motivo previsto;
- Jenkins marcó `Pruebas de integracion`, `Empaquetar`, `Desplegar` y `Comprobar despliegue` como omitidas debido a la falla anterior;
- finalizó con estado `FAILURE`.

Antes de ejecutar la #8, la aplicación desplegada tenía PID `34600`, el JAR tenía SHA-256 `6F1E19EF51E194A8A980BB1A81530AB309F8B353E4BC26FB5B36F9D5B0952354` y la ruta `/` respondía HTTP 200. Después de la falla se comprobaron nuevamente los tres valores: el PID y el hash permanecieron sin cambios y la aplicación continuó respondiendo HTTP 200. Esto demuestra que una prueba fallida detiene el pipeline antes de reemplazar la versión desplegada.

Finalmente, el job se restauró a `*/main`. La construcción manual #9 obtuvo `b13ae44`, ejecutó todas las etapas, desplegó con PID `10288`, pasó el smoke test HTTP y terminó con `SUCCESS`. Al finalizar, el job continuaba configurado para construir `main`.
