# Guías de estudio por clases – Evaluación 4 y Defensa (Control 3)

Estas guías juntan el material de la asignatura (Guía 14 *Consumiendo una API REST*, Guía 15 *Pruebas unitarias e instrumentadas*, las diapositivas de servicios de datos, pruebas y APK firmado, la pauta del Encargo y de la Defensa, y el quiz *Aprende este Quiz para tu Defensa Individual*) y las aterrizan en **nuestro proyecto**.

Cada clase tiene: lo esencial, dónde está en el código, una frase para explicarlo en la defensa, una práctica corta y **las 10 preguntas del quiz de esa sección con su respuesta** (el quiz saca 7 preguntas textuales de estas 100).

| Clase | Tema | Paso de la defensa | Sección del quiz |
| --- | --- | --- | --- |
| [1](clase-01.md) | Kotlin: lenguaje y concurrencia | Base para todos | 01 |
| [2](clase-02.md) | Jetpack Compose: frontend y navegación | 1 | 02 |
| [3](clase-03.md) | Arquitectura MVVM, Model y Repository | 2 | 03 |
| [4](clase-04.md) | HTTP, Retrofit y APIs externas | 4 | 04 |
| [5](clase-05.md) | Spring Boot: controller, service y arquitectura backend | 2 y 3 | 05 |
| [6](clase-06.md) | Base de datos, JPA y persistencia | 5 | 06 |
| [7](clase-07.md) | Microservicios, Eureka y API Gateway | 3 | 07 |
| [8](clase-08.md) | Integración CRUD y análisis de fallos | 5 y 9 | 08 |
| [9](clase-09.md) | Pruebas unitarias, JUnit, MockK y cobertura | 6 | 09 |
| [10](clase-10.md) | Firma de APK, GitHub, Trello y diagnóstico | 7 y 8 | 10 |
| [Defensa](defensa-alanis.md) | Guion, preguntas, modificación en vivo y video | 1 a 9 | — |
| [Resumen](resumen-rama-alanis.md) | Qué se agregó en esta rama y por qué | — | — |

## Plan de estudio (5 sesiones de 1 a 2 horas)

1. **Clases 1, 2 y 3**: lenguaje, pantallas y MVVM. Levanta la app y recorre los paquetes explicando en voz alta.
2. **Clases 5, 6 y 7**: backend. Levanta los tres microservicios, abre Swagger de ms-productos y la consola H2.
3. **Clases 4 y 8**: Retrofit y CRUD. Haz el CRUD de un producto mirando Logcat (filtro `okhttp`).
4. **Clases 9 y 10**: pruebas y APK. Ejecuta las pruebas, el reporte de cobertura y genera el APK firmado.
5. **Ensayo general** con [defensa-alanis.md](defensa-alanis.md): 3 ejercicios de modificación en vivo sin mirar la solución, el quiz completo en voz alta y la grabación del video.

## La defensa en una frase por paso

1. **Entorno**: la app (Compose, MVVM, Room, Retrofit) corre en el emulador y llega al PC por `10.0.2.2`; los tres microservicios Spring Boot corren en 8081, 8082 y 8083.
2. **Arquitectura**: View → ViewModel → Repository → (Retrofit → microservicio) + Room como copia; en el backend controller → service → repository → base de datos.
3. **Microservicios**: museos, pedidos y **productos**, cada uno con su base de datos y sus reglas.
4. **API externa**: Art Institute of Chicago, solo lectura, no se guarda.
5. **Persistencia remota**: primero el servidor, después Room; si el servidor no responde se muestra la copia con el aviso «Sin conexión».
6. **Pruebas**: MockK simula Retrofit y Room; fakes en memoria para los ViewModels; Mockito y MockMvc en los microservicios.
7. **APK**: firmado con `app/keystore/museo-release.jks` mediante `signingConfigs`.
8. **Colaboración**: ramas, Pull Requests y tarjetas en Trello.
9. **Modificación en vivo**: decir qué archivo se toca y por qué antes de escribir.
