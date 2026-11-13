# Evidencias de integración y despliegue continuos

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
