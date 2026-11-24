# Guía de estudio para la defensa individual (Evaluación Parcial 5)

La defensa dura 15 minutos y es **sin IA ni apoyo**. Esta guía sigue los 9 pasos de la pauta: qué mostrar, qué archivo abrir y qué responder. Ensaya cada paso al menos una vez en tu computador **antes** del día de la defensa.

> Recuerda: si la app no se ejecuta, no se evalúan los ítems siguientes. Llega con los dos microservicios y el emulador ya probados.

---

## Antes de entrar (checklist)

- [ ] `ms-museos` y `ms-pedidos` arrancan (`./mvnw spring-boot:run`) y responden en el navegador.
- [ ] La app corre en el emulador y el mapa muestra los museos (sin el aviso "Sin conexión").
- [ ] `./gradlew testDebugUnitTest` pasa en tu computador.
- [ ] Tienes a mano `entrega/museo-de-los-museos-v1.0-release.apk` y `app/keystore/museo-release.jks`.
- [ ] Practicaste al menos 2 de los ejercicios de la sección 9.

---

## 1. Inicio del proyecto y entorno

**Muestra**: la app corriendo en el emulador y los dos microservicios corriendo (consola de Spring Boot + `http://localhost:8081/api/museos` en el navegador).

**Explica**:
- *Dependencias de la app* (`gradle/libs.versions.toml` + `app/build.gradle.kts`): Compose y Material 3 (UI), Navigation, ViewModel y StateFlow (MVVM), Room (base local), **Retrofit + Gson** (HTTP y JSON), **OkHttp logging** (ver peticiones en Logcat), **Coil** (imágenes de internet), **MockK** (pruebas).
- *Dependencias de los microservicios* (`pom.xml`): `spring-boot-starter-web` (REST), `data-jpa` (persistencia), `validation` (`@Valid`), `h2` y `mysql-connector-j` (bases de datos), `springdoc` (Swagger).
- *Por qué `10.0.2.2`*: es la IP con la que el emulador llega al `localhost` del PC. Está en `BuildConfig.URL_MS_MUSEOS` (definido en `app/build.gradle.kts`).
- *Por qué `network_security_config.xml`*: Android bloquea HTTP sin cifrar. Se permite solo hacia el PC; la API externa usa HTTPS.

## 2. Arquitectura general

**MVVM en la app** (abre `viewmodel/historial/HistorialViewModel.kt` y `ui/screens/historial/HistorialScreen.kt`):
- La **View** (Compose) solo dibuja el `UiState` y llama funciones del ViewModel (`viewModel.avanzarEstado(id)`).
- El **ViewModel** tiene la lógica y expone `StateFlow<UiState>`. Sobrevive a la rotación de pantalla.
- El **Model/Repositorio** decide de dónde salen los datos (microservicio + Room). El ViewModel no sabe si son de internet o locales.
- *Por qué*: se puede probar la lógica sin la interfaz, y cada capa cambia sin romper las otras.

**Paquetes de la app**: `model`, `domain` (reglas puras), `remote` (Retrofit), `repository` (+ `db` Room), `viewmodel`, `ui`, `navigation`.

**Capas de cada microservicio**: `controller` (HTTP) → `service` (reglas de negocio) → `repository` (Spring Data JPA) → base de datos. Además `model` (entidades `@Entity`), `dto` (lo que llega en el JSON, con `@Valid`), `exception` (`GlobalExceptionHandler` convierte errores en JSON 400/404/409) y `config` (`DatosIniciales` carga los datos de prueba).

## 3. Microservicios creados

| | `ms-museos` | `ms-pedidos` |
|---|---|---|
| Entidad | `Museo` (tabla `museos`) | `Pedido` 1 → N `PedidoItem` (tablas `pedidos` y `pedido_items`) |
| Regla clave | Una ficha nueva queda `publicado = false`; solo `PATCH /aprobar` la publica. El servidor asigna el id (`siguienteId()`). | El estado solo avanza **un paso** (`EstadoPedido.siguiente()`); no se cancela un pedido ENVIADO; el servidor **recalcula** subtotal y total. |
| Errores | 404 si no existe, 409 si el id ya existe, 400 con el campo inválido | 409 si se salta un estado o se cancela un enviado |

**Muestra en tiempo real**: crea una ficha como Investigador → ábrela en `http://localhost:8081/api/museos?publicado=false` → apruébala como Administrador → vuelve a mirar el navegador. Logcat (filtro `okhttp`) muestra cada petición con su JSON.

**Pregunta típica: ¿por qué el servidor recalcula el total?** Porque nunca se confía en los montos que manda el cliente; un cliente modificado podría mandar un total falso.

## 4. Consumo de API externa

Abre `remote/ObrasArteApi.kt`, `remote/ClienteHttp.kt` y `repository/ObrasArteRepository.kt`.

- `@GET("api/v1/artworks/search")` con `@Query("q")`, `@Query("limit")`, `@Query("fields")` y un `@Header`.
- `ClienteHttp` arma un `Retrofit` por URL base con `GsonConverterFactory` (JSON ↔ objetos Kotlin) y el mismo `OkHttpClient` (tiempo de espera y logging).
- Los `@SerializedName` de `ObraDto` traducen `artist_display` → `artista`.

**Diferencias entre API externa y microservicio** (pregunta segura):

| | Microservicios propios | API externa |
|---|---|---|
| Quién la controla | Nosotros (código, base de datos, reglas) | Un tercero (Art Institute of Chicago) |
| Operaciones | CRUD completo (GET, POST, PUT, PATCH, DELETE) | Solo lectura (GET) |
| Persistencia | Fuente de verdad + copia en Room | No se guarda: se consulta en el momento |
| Protocolo | HTTP en la red local | HTTPS en internet |
| Si falla | La app muestra la copia local | Se muestra el error y *Reintentar* |

## 5. Persistencia remota

Flujo para **crear una ficha** (síguelo con el docente):

1. `DetalleMuseoScreen` → botón Guardar → `viewModel.guardar()`.
2. `DetalleMuseoViewModel.guardar()` valida con `ValidadorFichaMuseo`; si hay errores no llama al servidor.
3. `museos.crear(datos)` → `MuseumRepositoryRemoto.crear()` → `api.crear(museo.aDto(incluirId = false))` → **POST /api/museos**.
4. Servidor: `MuseoController.crear` → `@Valid MuseoRequest` → `MuseoService.crear` (asigna id, `publicado=false`) → `repositorio.save()` → H2.
5. La respuesta (con el id `m11`) vuelve a la app y se guarda en Room con `dao.guardar(...)`.
6. El mapa observa Room (`Flow`), así que la ficha aparece sola.

**Si el servidor está apagado**: `llamar { ... }` convierte la excepción en `ErrorRemoto(SIN_CONEXION)`, nada se guarda y el Snackbar lo explica. **Pruébalo**: detén `ms-museos` y trata de guardar.

**Insertar / actualizar / eliminar según pida el docente**:
- Insertar: Investigador → Mapa → ícono «Nueva ficha» (alfiler con +) → completar → Guardar. O Cliente → Carrito → Confirmar (POST pedido).
- Actualizar: Investigador → ficha → ✏️ (PUT). Administrador → Dashboard → Aprobar (PATCH). Administrador → Historial → Avanzar estado (PATCH).
- Eliminar: ficha → 🗑️ → Eliminar (DELETE). Historial → Cancelar solicitud (DELETE).
- Muestra el resultado en `http://localhost:8081/h2-console` (JDBC URL `jdbc:h2:file:./data/museos`, usuario `sa`, sin contraseña) o en el navegador.

## 6. Pruebas unitarias

Ejecuta en vivo: clic derecho en `app/src/test` → *Run Tests*, o `./gradlew testDebugUnitTest`. Para la cobertura: `./gradlew coberturaLogica` y abre el HTML (94,9 % de las líneas de lógica).

**Prueba buena para explicar**: `repository/MuseumRepositoryRemotoTest.sincronizar_sinRed_noTocaRoom_yAvisaSinConexion`
- **Qué se mockea**: `MuseosApi` con `mockk<MuseosApi>()` y `coEvery { api.listar() } throws IOException()` (simula el servidor apagado); el DAO con `mockk<MuseoDao>(relaxed = true)`.
- **Qué se prueba**: que el resultado sea `SIN_CONEXION`, que **no** se borre Room (`coVerify(exactly = 0) { dao.reemplazarTodos(any()) }`) y que el aviso de conexión cambie.
- **Por qué mockear**: la prueba no depende de internet ni del emulador; es rápida y siempre da el mismo resultado.

Otras: `HistorialRemotoTest` (fakes que simulan un 409 del servidor), `ObrasArteViewModelTest` (búsqueda inválida no llama a la API), y en los microservicios `PedidoServiceTest.cambiarEstado_saltandoPasos_lanzaConflicto` (Mockito).

**Cómo aportan a la seguridad, estabilidad y confiabilidad**: detectan regresiones al modificar código (por ejemplo, si alguien rompe la regla de "no cancelar un pedido enviado"); prueban los casos de error (sin red, 400, 409) que son difíciles de reproducir a mano; validan que los datos inválidos no lleguen a la base de datos; y documentan cómo debe comportarse cada parte.

## 7. Firma y generación del APK

1. *Build → Generate Signed App Bundle / APK → APK*.
2. *Create new...* → ruta `app/keystore/museo-release.jks`, contraseña, alias `museo`, validez en años, datos del certificado. (Equivalente: el comando `keytool` del README.)
3. Elegir la variante `release` → *Create*.
4. En el proyecto la firma es automática: `signingConfigs` en `app/build.gradle.kts` lee `keystore/keystore.properties`, y `./gradlew assembleRelease` genera el APK firmado.
5. Verificar con `apksigner verify --print-certs <apk>`.

**Preguntas típicas**:
- *¿Por qué firmar?* Android solo instala APK firmados. La firma identifica al autor, y las actualizaciones deben firmarse con la **misma llave**.
- *¿Qué pasa si se pierde el .jks?* No se pueden publicar actualizaciones de la app con el mismo nombre de paquete.
- *¿Debug vs release?* Debug se firma con una llave de depuración automática; release usa nuestra llave y activa R8 (optimización).

## 8. Colaboración

- GitHub: muestra tus commits (`git log --author="Tu nombre"`), tus ramas `feature/...` y los Pull Requests.
- Trello: muestra tus tarjetas, en qué columna estaban y cómo avanzaron.

## 9. Modificación en tiempo real (40 % de la nota): ejercicios para practicar

Practica estos cambios **sin mirar la solución**. Cada uno toca la app, el microservicio o la base de datos.

**A. Nuevo endpoint en `ms-museos`: buscar por país**
1. `MuseoRepository`: `List<Museo> findByPaisIgnoreCaseOrderByNombreAsc(String pais);`
2. `MuseoService`: `public List<Museo> porPais(String pais) { return repositorio.findByPaisIgnoreCaseOrderByNombreAsc(pais); }`
3. `MuseoController`: `@GetMapping("/pais/{pais}") public List<Museo> porPais(@PathVariable String pais) { return servicio.porPais(pais); }`
4. Reinicia y prueba `http://localhost:8081/api/museos/pais/Chile`.
5. App: en `MuseosApi` agrega `@GET("api/museos/pais/{pais}") suspend fun porPais(@Path("pais") pais: String): List<MuseoDto>`.

**B. Nueva regla de negocio**: no permitir pedidos de más de 10 unidades de un producto.
En `PedidoItemRequest` cambia a `@Min(1) @Max(value = 10, message = "Máximo 10 unidades") int cantidad`. Prueba con un POST de 11 unidades: responde 400 y la app muestra el mensaje en el Snackbar del carrito.

**C. Nuevo campo**: agregar `telefono` al pedido.
Entidad `Pedido` (columna) → `PedidoRequest` → `PedidoService.crear` (copiar el valor) → en la app `PedidoDto` y `Order.aDto()`. Con `ddl-auto=update` la columna se crea sola.

**D. Nueva prueba unitaria**: en `ObrasArteViewModelTest`:
```kotlin
@Test
fun consultaConEspacios_seRecorta() {
    viewModel.onConsultaChange("  Chile  ")
    viewModel.buscar()
    assertEquals(listOf("Chile"), repositorio.consultas)
}
```

**E. Cambio en la API externa**: mostrar 10 obras en vez de 20 → `limite: Int = 10` en `ObrasArteApi.buscar` y actualiza `ObrasArteRepositoryTest` (el `coVerify` espera 20).

**F. Corregir un error**: el docente puede romper algo (por ejemplo, borrar `@Valid` del controlador). Ejecuta las pruebas (`./mvnw test`): `crear_conDatosInvalidos_devuelve400ConElCampo` falla y te dice dónde mirar.
