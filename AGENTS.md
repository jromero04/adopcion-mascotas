# Proyecto: adopción de mascotas — Entregable 4

## Objetivo y alcance

Aplicación web pequeña para Programación Avanzada 2026, desarrollada por un grupo de dos personas. El objetivo principal es demostrar CI/CD con Jenkins, pruebas automáticas y trabajo colaborativo con Git/GitHub. Mantener las funcionalidades y la interfaz sencillas.

Este archivo reúne decisiones del equipo e instrucciones para trabajar en el repositorio. Revisar primero el código y los POM existentes; no asumir que las tareas descritas ya están implementadas. La consigna PDF es la fuente para los requisitos académicos y este archivo no la sustituye.

## Decisiones técnicas acordadas

- Java 21, Spring Boot, Maven y Thymeleaf.
- IDE: IntelliJ IDEA.
- Proyecto Maven principal: `adopcion-mascotas`, con `packaging` de tipo `pom`.
- Coordenadas del proyecto: `uy.edu.um:adopcion-mascotas:1.0.0-SNAPSHOT`.
- Dos módulos Maven: `registro-mascotas` y `catalogo-web`.
- Una única aplicación ejecutable Spring Boot; no separar en microservicios.
- Versión de Spring Boot indicada en el último paso de configuración: `4.0.9`. Comprobar el POM real antes de editar; mantener una versión común y no actualizarla como cambio incidental.
- Persistencia: H2 en archivo. Pruebas con una base separada, preferentemente H2 en memoria.
- No usar Docker. Ejecutar Jenkins y la aplicación directamente en el equipo.
- GitHub para el código y el pipeline; pull requests con revisión de la otra integrante antes de mergear.

## Responsabilidades y estructura

| Elemento | Responsabilidad |
| --- | --- |
| `pom.xml` principal | Configuración compartida y declaración de módulos. |
| `registro-mascotas` | Modelo de mascota, validaciones, registro y persistencia. |
| `catalogo-web` | Inicio de Spring Boot, controladores web, vistas, listado y lógica de filtros. Depende de `registro-mascotas`. |
| `Jenkinsfile` | Pipeline declarativo versionado junto al código. |
| `README.md` | Requisitos y pasos reproducibles de construcción, pruebas y ejecución. |

Cada módulo tiene su propio `src/main` y `src/test`. El proyecto principal no necesita código Java. No borrar código existente sin revisar su contenido.

Paquete base propuesto: `uy.edu.um.adopcion`. La clase de inicio debe poder descubrir los componentes, entidades y repositorios de ambos módulos.

## Funcionalidades

1. Registrar una mascota encontrada.
2. Mostrar un catálogo con la fotografía y los datos de cada mascota.
3. Buscar por especie, sexo y rango de edad.

No incorporar por iniciativa propia autenticación, usuarios, solicitudes de adopción, pagos, chat ni otras funcionalidades ajenas al alcance acordado.

## Datos y reglas

| Dato | Regla |
| --- | --- |
| ID | Identificador generado automáticamente. |
| Nombre | Opcional; si falta, mostrar «Sin nombre». |
| Especie | Obligatoria. Definir un conjunto coherente de opciones en modelo, formulario y filtros. |
| Sexo | Macho, Hembra o Desconocido. Debe poder registrarse sin conocerlo. |
| Edad aproximada | En meses, opcional y no negativa. Usar ausencia de valor para edad desconocida; `0` es una edad válida. |
| Descripción | Campo previsto; no se acordó que sea obligatorio. |
| Foto | URL obligatoria en el formulario. No implementar carga de archivos. |

Aplicar las validaciones en el servidor además de las ayudas del formulario. No convertir los datos desconocidos en valores ficticios.

### Fotografías

- Guardar la URL como texto y mostrar la foto dentro del catálogo, mediante un elemento de imagen.
- Solicitar una URL HTTP/HTTPS que permita acceder a la imagen; un enlace a una página que contiene una foto puede no funcionar.
- Rechazar una URL vacía o con un esquema distinto de HTTP/HTTPS.
- No exigir que la URL termine en `.jpg` o `.png`: puede haber URLs de imagen sin extensión.
- Mostrar una imagen local de respaldo y texto alternativo si la imagen externa no carga. La obligatoriedad de la URL no garantiza su disponibilidad futura.
- Evitar depender de servicios externos para ejecutar las pruebas.

### Filtros

- Los filtros se pueden combinar: todas las condiciones seleccionadas deben cumplirse (AND).
- Sexo: Todos, Macho, Hembra y Desconocido. Todos incluye los registros con sexo desconocido.
- Edad: mínimo y máximo en meses, con límites inclusivos; permitir informar uno solo de los límites.
- Sin filtro de edad, incluir mascotas con edad desconocida.
- Con al menos un límite de edad, excluir mascotas con edad desconocida.
- Rechazar límites negativos y un mínimo mayor que el máximo, con un mensaje comprensible.
- Si no hay coincidencias, mostrar un mensaje y permitir modificar o limpiar los filtros.

## Pruebas y construcción

- Incluir pruebas unitarias de cada módulo y pruebas de integración entre módulos.
- Casos de registro: datos válidos, nombre ausente, edad/sexo desconocidos, edad negativa, especie ausente y URL de foto ausente o inválida.
- Casos de búsqueda: filtros individuales y combinados, extremos inclusivos, un único límite, edades desconocidas y búsqueda sin resultados.
- Integración: registrar una mascota a través de la aplicación y verificar que se conserva y aparece en el catálogo y la búsqueda que corresponde.
- Mantener los datos de pruebas aislados de desarrollo y producción simulada.
- Configurar Maven para que `mvn clean verify`, desde la raíz, construya todos los módulos y ejecute pruebas unitarias y de integración. Comprobar que no se omiten silenciosamente las de integración.
- Configurar el plugin ejecutable de Spring Boot en `catalogo-web`, no en el módulo biblioteca.
- No dar por aprobadas verificaciones que no se hayan ejecutado; indicar lo pendiente.

## Jenkins y despliegue

- Pipeline declarativo en `Jenkinsfile`, dentro del repositorio.
- Detectar automáticamente cambios en GitHub. La propuesta base es desplegar cambios de `main`; usar webhook o consulta periódica según la conectividad disponible. La configuración concreta del disparador sigue pendiente.
- Secuencia: checkout, compilación, pruebas unitarias, pruebas de integración, empaquetado, despliegue y comprobación de respuesta de la aplicación.
- Si falla la compilación o una prueba, detener el proceso antes del despliegue y conservar funcionando la versión anterior.
- Desplegar el `.jar` de `catalogo-web` en un ambiente local de producción simulado. Automatizar la actualización y el arranque; no limitarse a compilar o copiar el archivo sin mostrar la aplicación funcionando.
- Guardar la base H2 fuera del workspace de Jenkins y de las carpetas reemplazadas al desplegar; conservar los datos entre actualizaciones.
- Separar la ejecución de desarrollo de la desplegada, incluyendo sus archivos de datos. Si corren simultáneamente, usar puertos distintos.
- Jenkins también corre localmente. No es necesario publicar Jenkins ni la web en Internet.
- No añadir aprobación manual ni entorno UAT como requisito: el extra corresponde a grupos de tres personas.
- Registrar resultados de pruebas y errores reales del pipeline, con su diagnóstico y solución.
- No guardar credenciales ni tokens en Git. Usar las credenciales de Jenkins o la configuración externa apropiada.
- Antes de escribir scripts de despliegue, verificar el sistema operativo, las rutas y la forma de ejecución de Jenkins. No asumir que scripts Linux funcionan en Windows.

## GitHub y colaboración

- Trabajar en ramas cortas y abrir pull requests hacia `main`.
- Configurar revisión obligatoria de la otra integrante antes de mergear, según las opciones disponibles en el repositorio. Verificar la configuración real; no afirmar que está activa solo porque se acordó.
- No hacer push directo a `main` ni mergear sin la revisión acordada.
- Excluir de Git archivos generados (`target/`), bases H2 locales, logs, credenciales y configuración personal del IDE.

## Entrega y defensa

- Entregar código, pipeline, README dentro del ZIP solicitado por la consigna.
- No inventar incidentes como si hubieran ocurrido: distinguir demostraciones planificadas de errores reales.

## Estado conocido al redactar este archivo

- El proyecto principal y los módulos `registro-mascotas` y `catalogo-web` están creados.
- `registro-mascotas` contiene el modelo, las validaciones, el repositorio JPA y el servicio de registro.
- El 06/11/2026 se ejecutó `mvn clean verify` con Java 21: los dos módulos se construyeron y las 7 pruebas unitarias de registro finalizaron sin fallos.
- `catalogo-web` usa H2 en archivo para desarrollo y permite configurar otra ubicación mediante `ADOPCION_DB_URL`.
- El 06/11/2026 se inició el JAR, se verificó la conexión H2 y la ruta `/` respondió HTTP 200.
- `catalogo-web` contiene el formulario de registro, el catálogo con imagen de respaldo y filtros combinables por especie, sexo y rango de edad.
- Se verificaron mediante HTTP el registro, el catálogo, los filtros individuales y combinados, los límites inclusivos, las edades desconocidas, la búsqueda vacía y el mensaje de rango inválido.
- El 06/11/2026 se ejecutó nuevamente `mvn clean verify`: las 7 pruebas de `registro-mascotas` y las 16 pruebas de `catalogo-web` finalizaron sin fallos.
- El 09/11/2026 se configuró Maven Failsafe y se agregó una prueba de integración que levanta la aplicación con H2 en memoria, registra una mascota por HTTP y verifica su persistencia, catálogo y búsqueda. `mvn clean verify` ejecutó 23 pruebas unitarias y 1 de integración sin fallos.
- El 09/11/2026 se agregó un `Jenkinsfile` declarativo base para Windows con checkout, verificación del entorno, compilación, pruebas unitarias, pruebas de integración, empaquetado, publicación JUnit y archivado del JAR.
- El 09/11/2026 se instaló Jenkins 2.580.2 LTS como servicio local de Windows, se configuraron Java 21 y Maven 3.9.12 y se ejecutó el pipeline desde `feature/ci-integracion-jenkins`. Las etapas finalizaron correctamente, los resultados de pruebas se publicaron, el JAR se archivó y Jenkins informó `Finished: SUCCESS`.
- El 09/11/2026 se implementó y verificó el despliegue local de Windows en `C:\ProgramData\AdopcionMascotas\produccion`, separado del workspace de Jenkins y de desarrollo. La aplicación desplegada usa el puerto 8081 y una H2 persistente externa, conserva un JAR de respaldo y restaura automáticamente la versión anterior si la nueva no responde HTTP 200. Se comprobó un segundo despliegue con conservación de datos y un rollback controlado exitoso.
- El 11/11/2026 se ejecutó desde Jenkins el pipeline con las etapas de despliegue y comprobación HTTP incorporadas. La construcción completa finalizó con estado `SUCCESS` y los cambios de despliegue y pipeline quedaron integrados a `main` mediante las PR #5 y #6.
- El 11/11/2026 el job de Jenkins se configuró para construir `main` y una ejecución manual desde esa rama finalizó con estado `SUCCESS`. Tras el merge de la PR #8, `pollSCM` detectó el cambio e inició automáticamente la construcción #5 con causa `SCMTriggerCause`; el despliegue, la comprobación HTTP y el pipeline completo finalizaron con estado `SUCCESS`.
- El 11/11/2026 se agregó `docs/evidencias-ci-cd.md` con el entorno, las etapas, los resultados de pruebas, el historial real de Jenkins, el despliegue, el rollback y los problemas efectivamente observados. El documento se actualizó con las construcciones #1 a #9.
- El 11/11/2026 se ejecutó la demostración planificada desde `demo/falla-controlada-pipeline`: la construcción #8 falló en pruebas unitarias, omitió integración, empaquetado, despliegue y smoke test, mientras la aplicación anterior conservó PID, hash del JAR y respuesta HTTP 200. El job se restauró a `main` y la construcción #9 finalizó con `SUCCESS`. La rama de demostración no debe integrarse.
- El 11/11/2026 se agregó `README.md` con descripción funcional, arquitectura, requisitos, comandos reproducibles de construcción y pruebas, ejecución de desarrollo, separación de ambientes, despliegue local, configuración de Jenkins, colaboración y enlace a las evidencias.
- El 11/11/2026 la API de GitHub confirmó una ruleset activa sobre `main`, sin bypass, que exige pull request, una aprobación y resolución de conversaciones, descarta aprobaciones obsoletas y bloquea eliminaciones y force pushes. Antes de recibir revisiones, la PR #13 informó `mergeable_state: blocked` y una lista de revisiones vacía, verificando funcionalmente el bloqueo.

## Forma de trabajo

- Respetar estas decisiones y mantener cambios pequeños, claros y comprobables.
- Explicar en español qué se cambia y cómo comprobarlo; si se guía a la usuaria en IntelliJ, indicar módulo y ubicación de cada archivo.
- Revisar el código existente y completar lo pendiente sin recrear innecesariamente el proyecto.
- Actualizar el estado documentado cuando se verifique un avance. No presentar propuestas como implementaciones terminadas.
- Consultar al equipo antes de cambiar decisiones de arquitectura o ampliar el alcance; resolver los detalles rutinarios dentro de este alcance sin bloquear el avance.
