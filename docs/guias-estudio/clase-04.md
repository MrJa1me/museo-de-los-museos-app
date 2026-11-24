# Clase 4: HTTP, Retrofit y APIs externas

**Objetivo:** mostrar el "RetrofitService" (paso 4) y explicar la diferencia entre consumir una API externa y un microservicio propio.

## Lo esencial (Guía 14: Consumiendo una API REST)

- **Retrofit** convierte una interfaz Kotlin en llamadas HTTP. Cada función lleva una anotación: `@GET`, `@POST`, `@PUT`, `@PATCH`, `@DELETE`.
- `@Path("id")` reemplaza `{id}` en la ruta; `@Query("q")` agrega `?q=...`; `@Body` manda un objeto como JSON.
- **Gson** (`GsonConverterFactory`) transforma JSON ↔ objetos Kotlin (DTO).
- Las funciones son `suspend`: la red no bloquea la pantalla.
- **Códigos HTTP**: 200 OK, 201 creado, 204 sin contenido, 400 datos inválidos, 404 no existe, 409 conflicto, 500 error del servidor.
- **Emulador → PC:** la IP `10.0.2.2` es el `localhost` del computador. Está en `BuildConfig` (`app/build.gradle.kts`). En un teléfono se usa la IP del PC en el Wi-Fi.
- `network_security_config.xml` permite HTTP solo hacia el PC; la API externa usa HTTPS.

## En nuestro proyecto

| Archivo | Qué hace |
| --- | --- |
| `app/src/main/java/com/example/museo_de_los_museos_app/remote/ClienteHttp.kt` | Arma un Retrofit por URL base (8081 museos, 8082 pedidos, **8083 productos**, API externa) con OkHttp y logging |
| `app/src/main/java/com/example/museo_de_los_museos_app/remote/ProductosApi.kt` | Endpoints de **ms-productos** (`@GET`, `@POST`, `@PUT`, `@PATCH`) |
| `app/src/main/java/com/example/museo_de_los_museos_app/remote/dto/ProductoDto.kt` | Forma exacta del JSON del catálogo |
| `app/src/main/java/com/example/museo_de_los_museos_app/remote/MapeadoresProducto.kt` | DTO ↔ modelo |
| `app/src/main/java/com/example/museo_de_los_museos_app/remote/ErrorRemoto.kt` | Convierte excepciones en `SIN_CONEXION`, `RECHAZADO` (400/404/409), `SERVIDOR` (5xx) |
| `app/src/main/java/com/example/museo_de_los_museos_app/remote/ObrasArteApi.kt` | API externa del Art Institute of Chicago (`@GET("api/v1/artworks/search")` con `@Query`) |

## API externa vs microservicio

| | Microservicio propio (ms-productos) | API externa (Art Institute of Chicago) |
| --- | --- | --- |
| Quién la controla | Nosotros: código, base de datos y reglas | Un tercero |
| Operaciones | CRUD completo (GET, POST, PUT, PATCH) | Solo lectura (GET) |
| Se guarda en Room | Sí, como copia local | No: se consulta en el momento |
| Protocolo | HTTP hacia el PC (10.0.2.2:8083) | HTTPS público |

## Cómo lo explico en la defensa

> "En `ProductosApi` cada función es un endpoint. Por ejemplo `@PATCH("api/productos/{id}/activo")` con `@Path` y un `@Body` `CambioActivoDto`. `ClienteHttp` crea el Retrofit con la URL de BuildConfig y Gson. En Logcat, con el filtro `okhttp`, se ve cada petición con su JSON."

## Práctica (20 min)

1. Con ms-productos encendido, abre Logcat (filtro `okhttp`), desactiva un producto como Administrador y lee la petición `PATCH` y su respuesta.
2. Agrega a `ProductosApi` un `@GET("api/productos/{id}") suspend fun obtener(@Path("id") id: String): ProductoDto`.

## Preguntas del quiz de esta clase (sección 04)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**031. ¿Qué anotación Retrofit declara una solicitud GET a `productos`?**  
✅ C) `@GET("productos")` — `@GET` describe el método HTTP y la ruta relativa del endpoint.

**032. ¿Qué hace `@Path("id") id: Long` en `@GET("productos/{id}")`?**  
✅ D) Sustituye el segmento `{id}` de la URL con el parámetro — `@Path` enlaza un argumento con una variable de la ruta.

**033. ¿Qué anotación Retrofit se usa habitualmente para enviar un objeto JSON en el cuerpo de un POST?**  
✅ A) `@Body` — `@Body` indica el contenido del cuerpo; un convertidor puede serializarlo a JSON.

**034. ¿Qué diferencia hay entre `@Query("page")` y `@Path("id")`?**  
✅ A) `@Query` agrega parámetros de consulta y `@Path` reemplaza segmentos de ruta — Una URL puede incluir `?page=2` y segmentos como `/productos/7`.

**035. ¿Qué significa una respuesta HTTP 201 Created tras un POST?**  
✅ B) El recurso se creó correctamente — 201 indica creación exitosa; puede venir con ubicación o representación del recurso.

**036. ¿Cuál es la interpretación correcta de HTTP 404?**  
✅ D) El recurso solicitado no se encontró — 404 expresa que el servidor no encontró una representación del recurso solicitado.

**037. ¿Qué hace un convertidor como Gson en Retrofit?**  
✅ B) Transforma JSON y objetos Kotlin cuando está configurado — El convertidor serializa/deserializa formatos del cuerpo HTTP.

**038. ¿Por qué no conviene ejecutar peticiones HTTP bloqueantes en el hilo principal de Android?**  
✅ A) Pueden congelar la UI y provocar errores de aplicación no responde — La interfaz debe permanecer receptiva; la red se gestiona de forma asíncrona.

**039. ¿Qué conviene hacer si Retrofit recibe una respuesta 500 del backend?**  
✅ A) Tratarla como error de servidor y mostrar un estado recuperable — 500 indica error interno del servidor; se debe gestionar el fallo sin bloquear la app.

**040. En un emulador Android estándar, ¿qué dirección apunta al host de desarrollo en la red especial del emulador?**  
✅ D) `10.0.2.2` — `10.0.2.2` es el alias al loopback del computador anfitrión en el emulador Android estándar.

---

[← Clase 3](clase-03.md) · [Índice](README.md) · [Clase 5 →](clase-05.md)
