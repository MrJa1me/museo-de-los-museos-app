# Guía de defensa individual – Alanis (P4: catálogo, carrito y ms-productos)

La defensa es **individual, dura 15 minutos y es sin IA**. El profesor evalúa lo que haces y lo que explicas; la **modificación en vivo vale 40 %**. Si la app no corre, los ítems siguientes valen cero: llega con todo encendido.

## Antes de entrar (checklist)

- [ ] Los tres microservicios corriendo, cada uno en su terminal (`./mvnw spring-boot:run`; en Windows `mvnw.cmd spring-boot:run`):
  - `microservicios/ms-museos` → http://localhost:8081/api/museos
  - `microservicios/ms-pedidos` → http://localhost:8082/api/pedidos
  - `microservicios/ms-productos` → http://localhost:8083/api/productos
- [ ] La app abierta en el emulador, con el catálogo cargado y **sin** el aviso «Sin conexión».
- [ ] Las pruebas pasan: `./gradlew testDebugUnitTest` y `./mvnw test` en `ms-productos`.
- [ ] A mano: `entrega/museo-de-los-museos-v1.0-release.apk`, `app/keystore/museo-release.jks`, Swagger de ms-productos (http://localhost:8083/swagger-ui.html) y la consola H2 (http://localhost:8083/h2-console, JDBC `jdbc:h2:file:./data/productos`, usuario `sa`).
- [ ] GitHub abierto en tus commits y tu Pull Request; Trello en tus tarjetas.
- [ ] Practicaste al menos 3 ejercicios de modificación en vivo sin mirar la solución.

Usuarios de prueba: Administrador `admin@museo.test` / `Admin123` · Cliente `cliente1@museo.test` / `Cliente123`.

## Los 9 pasos con tu parte

### 1. Entorno
- **Muestra** la app en el emulador y las tres consolas de Spring Boot.
- **Di**: app con Compose + Material 3, ViewModel + StateFlow, Room, Retrofit + Gson, OkHttp logging, Coil y MockK. Microservicios con starter-web, data-jpa, validation, H2/MySQL y springdoc.
- **Abre**: `gradle/libs.versions.toml`, `app/build.gradle.kts` (las tres URL en `buildConfigField`, la de productos es `URL_MS_PRODUCTOS` → puerto 8083) y `microservicios/ms-productos/pom.xml`.

### 2. Arquitectura
- App: `model`, `domain`, `remote`, `repository` (+ `db`), `viewmodel`, `ui`, `navigation`.
- ms-productos: `controller` → `service` → `repository` → H2, más `model`, `dto`, `exception` y `config`.
- **Ejemplo para abrir**: `viewmodel/catalogo/CatalogoViewModel.kt` y `ui/screens/catalogo/CatalogoScreen.kt`.

### 3. Microservicios (tu microservicio: ms-productos)
- Entidad `Producto` (tabla `productos`): id, nombre, categoría, precio, stock, pesoKg, descripción, activo.
- Reglas: el servidor asigna el id por categoría (c05, p03, m03k, a03); el peso es obligatorio salvo en cursos; editar no cambia si está activo; desactivar no borra; descontar stock es todo o nada.
- Errores: 400 (campo inválido), 404 (no existe), 409 (id repetido o stock insuficiente).
- **Muestra en vivo**: como Administrador desactiva «Bolso de tela» en la app y recarga http://localhost:8083/api/productos/a01 en el navegador: `"activo": false`. En Logcat (filtro `okhttp`) se ve el `PATCH`.

### 4. API externa
- `remote/ObrasArteApi.kt`, `remote/ClienteHttp.kt`, `repository/ObrasArteRepository.kt`. Dashboard → Obras de arte.
- Diferencia: ms-productos es nuestro, con CRUD y base de datos; la API externa es de un tercero, solo lectura y no se guarda.

### 5. Persistencia remota (flujo de crear un producto)
1. `FormularioProductoScreen` → Guardar → `viewModel.guardar()`.
2. El ViewModel valida con `Validadores`; con errores no llama al servidor.
3. `ProductRepositoryRemoto.crear()` → `api.crear(...)` → `POST /api/productos` (sin id).
4. Servidor: `ProductoController` → `@Valid ProductoRequest` → `ProductoService.crear` (id + activo) → `save()`.
5. La respuesta se guarda en Room con `dao.guardar(...)` y el catálogo (que observa Room) lo muestra solo.

- Insertar: crear producto o confirmar un carrito. Actualizar: editar, activar/desactivar, descontar stock. Eliminar: desactivar (borrado lógico) o `DELETE` desde Swagger.
- **Prueba el error**: apaga ms-productos e intenta guardar: aparece el Snackbar y no se guarda nada; el catálogo muestra «Sin conexión» con Reintentar.

### 6. Pruebas unitarias
- Ejecuta: clic derecho en `app/src/test` → Run Tests, o `./gradlew testDebugUnitTest` (224 pruebas). Cobertura: `./gradlew coberturaLogica`.
- **Prueba para explicar**: `repository/ProductRepositoryRemotoTest` → `sincronizar_sinRed_noTocaRoom_yAvisaSinConexion`.
  - Se simula `ProductosApi` con `mockk<ProductosApi>()` y `coEvery { api.listar() } throws IOException(...)` (servidor apagado). El DAO con `mockk<ProductoDao>(relaxed = true)`.
  - Se verifica: el error es `SIN_CONEXION`, Room no se borra (`coVerify(exactly = 0) { dao.reemplazarTodos(any()) }`) y el estado de conexión cambia.
- **Otra para nombrar**: `CarritoStockServidorTest.otroClienteComproAntes_noCreaPedido_yAvisaQueProductoNoAlcanza` (fake `ProductosConFallo`) y en el backend `ProductoServiceTest.descontarStock_sinStock_lanza409YNoGuardaNada` (Mockito).

### 7. APK firmado
Build → Generate Signed App Bundle / APK → APK → Create new (ruta del `.jks`, contraseña, alias `museo`, validez) → release → Create. En el proyecto: `signingConfigs` en `app/build.gradle.kts` + `./gradlew assembleRelease`. Verificar con `apksigner verify --print-certs`.

### 8. Colaboración
`git log --author="Alanis" --oneline`, tu rama y tu Pull Request. En Trello, tus tarjetas de catálogo, carrito y ms-productos.

### 9. Modificación en tiempo real
Piensa en voz alta: «voy a tocar el *repository* porque…, luego el *service*…, luego el *controller*…».

## Preguntas probables sobre tu parte

| Pregunta | Respuesta corta | Dónde mostrarlo |
| --- | --- | --- |
| ¿Por qué un microservicio para el catálogo? | El stock real debe estar en un solo lugar para todos los teléfonos; en Room cada teléfono tenía su propia copia | `ProductoService` |
| ¿Cómo se valida la disponibilidad con el backend? | Antes de crear la solicitud, `POST /api/productos/disponibilidad`; si hay problemas no se crea el pedido y se muestra cuál producto no alcanza | `CarritoViewModel.confirmarPedido` |
| ¿Qué pasa si dos clientes compran la última unidad? | El segundo recibe el aviso de stock: la disponibilidad se revisa en el servidor y el descuento es `@Transactional` (todo o nada, 409 si no alcanza) | `ProductoService.descontarStock` |
| ¿Por qué desactivar y no borrar? | Los pedidos antiguos siguen nombrando el producto; el Cliente deja de verlo y el Administrador lo puede reactivar | `PATCH /{id}/activo` |
| ¿Quién asigna el id? | El servidor, según la categoría; la app lo manda vacío | `ProductoService.siguienteId`, `aDto(incluirId = false)` |
| ¿Por qué el peso se valida en el service y no con una anotación? | Depende de otro campo (la categoría); las anotaciones revisan un campo a la vez | `validarPeso` → `DatoInvalidoException` |
| ¿Qué hace `@Valid`? | Revisa las anotaciones del DTO y si falla responde 400 con el campo | `ProductoRequest` |
| ¿Para qué sirve Room si existe el microservicio? | Es la copia local: el catálogo se ve aunque el servidor esté apagado | Aviso «Sin conexión» |
| ¿Qué se mockea? | La interfaz Retrofit y el DAO con MockK; en los ViewModels, fakes en memoria | `ProductRepositoryRemotoTest` |
| ¿Por qué los campos del DTO son anulables? | Gson no respeta los valores por defecto de Kotlin; el mapeador pone valores seguros | `ProductoDto`, `MapeadoresProducto` |

## Ejercicios para la modificación en vivo (haz cada uno sin mirar la solución)

- [ ] **A. Nuevo endpoint: buscar productos por texto.**
  1. `ProductoRepository`: `List<Producto> findByNombreContainingIgnoreCaseOrderByIdAsc(String texto);`
  2. `ProductoService`: `public List<Producto> buscar(String texto) { return repositorio.findByNombreContainingIgnoreCaseOrderByIdAsc(texto.trim()); }`
  3. `ProductoController`: `@GetMapping("/buscar") public List<Producto> buscar(@RequestParam String texto)`.
  4. Reinicia y abre http://localhost:8083/api/productos/buscar?texto=museo.
  5. App: en `ProductosApi`, `@GET("api/productos/buscar") suspend fun buscar(@Query("texto") texto: String): List<ProductoDto>`.
- [ ] **B. Nueva regla: máximo 10 unidades por línea.** En `ItemStockRequest` agrega `@Max(value = 10, message = "Máximo 10 unidades por producto")` a `cantidad`. Prueba en Swagger `disponibilidad` con 11 → 400.
- [ ] **C. Cambiar una regla de negocio: precio máximo de 500.000.** En `ProductoRequest` cambia `@Max(value = 9_999_999, ...)` por `@Max(value = 500_000, message = "El precio máximo es $500.000")`. Edita un producto en la app con 600000: el Snackbar muestra el mensaje del servidor.
- [ ] **D. Nueva prueba.** En `DisponibilidadStockTest`:
  ```kotlin
  @Test
  fun pedirMasQueElStock_esSinStock() {
      val problema = DisponibilidadStock.revisar(listOf(ItemStock("p01", 26)), productos).single()
      assertEquals(MotivoStock.SIN_STOCK, problema.motivo)
  }
  ```
- [ ] **E. Nuevo filtro en la app.** Mostrar solo productos con precio menor a 20.000: en `CatalogoViewModel.filtrar` agrega la condición `producto.precio < 20000` y en `CatalogoViewModelTest` verifica que `c01` (45.000) no aparece.
- [ ] **F. Corregir un error.** Pide a alguien que borre `@Valid` en `ProductoController.crear`. Ejecuta `./mvnw test`: falla `crear_precioCero_devuelve400ConElCampo` y te indica dónde mirar.

## Guion del video individual (~10 min)

| Minuto | En pantalla | Qué dices |
| --- | --- | --- |
| 0:00–0:45 | Tu nombre y el README | Quién eres, el proyecto y tu parte (catálogo, carrito y ms-productos) |
| 0:45–1:45 | Las tres consolas y la app | Entorno, dependencias y por qué `10.0.2.2` |
| 1:45–3:00 | Paquetes de la app y de ms-productos | MVVM y controller → service → repository |
| 3:00–4:30 | Swagger de ms-productos y la app | Endpoints y reglas (id por categoría, peso, stock) |
| 4:30–5:30 | Obras de arte y `ObrasArteApi.kt` | Retrofit y diferencia con el microservicio |
| 5:30–7:00 | Crear, editar y desactivar un producto; confirmar un carrito; consola H2 | Flujo de punta a punta (CRUD + stock) |
| 7:00–8:00 | Ejecución de pruebas y reporte de cobertura | Qué se prueba, qué se simula y cómo |
| 8:00–9:00 | `build.gradle.kts` (signingConfigs) y el APK en `entrega/` | Cómo se generó y firmó |
| 9:00–10:00 | Tus commits, tu PR y Trello | Tu aporte concreto |

Consejos: ensaya una vez, agranda la letra del IDE (Ctrl + rueda), habla mientras haces cada cosa y, si algo falla, explícalo: el aviso «Sin conexión» también es una funcionalidad.
