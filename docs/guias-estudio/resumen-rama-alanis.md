# Resumen de la rama de Alanis – Evaluación 4

Esta rama parte de la rama de Benjamín (`feature/ev4-microservicios`, Pull Request #29), que ya tenía `ms-museos`, `ms-pedidos`, la API externa, el APK firmado y las pruebas. Agrega lo que faltaba de la parte P4 (catálogo y carrito): **el catálogo seguía guardado solo en Room**, así que cada teléfono tenía su propio stock y la pauta del caso pide que el catálogo «se sincronice mediante Retrofit» y que la app «valide la disponibilidad con el backend antes de confirmar una solicitud».

## Qué se agregó

| Requisito de la pauta | Qué se hizo | Dónde está |
| --- | --- | --- |
| Microservicios con base de datos (IE 3.1.2) | Tercer microservicio **`ms-productos`** (puerto 8083): CRUD del catálogo, activar/desactivar, revisar disponibilidad y descontar stock. H2 en archivo o MySQL | `microservicios/ms-productos/` |
| App integrada con CRUD (IE 3.1.3) | El catálogo lee de ms-productos (con copia en Room); el Administrador crea, edita y activa/desactiva en el servidor; el carrito valida y descuenta stock en el servidor | `remote/ProductosApi.kt`, `repository/ProductRepositoryRemoto.kt`, ViewModels de catálogo, formulario, detalle y carrito |
| Pantallas sin errores (IE 3.1.1) | Catálogo con botón Actualizar y aviso «Sin conexión»; formulario y detalle muestran el error del servidor en un Snackbar | `ui/screens/catalogo`, `formularioproducto`, `detalleproducto` |
| Pruebas ≥ 80 % (IE 3.2.1) | 30 pruebas nuevas en la app (MockK + fakes) y 29 en ms-productos (Mockito + MockMvc) | `app/src/test/`, `microservicios/ms-productos/src/test/` |
| README | Endpoints de ms-productos, pasos para levantarlo y conteo de pruebas | `README.md` |
| Estudio para la defensa | 10 guías por clase con las 100 preguntas del quiz, guía de defensa personal y guion del video | `docs/guias-estudio/` |

**Arreglo para compilar:** a la rama de la Evaluación 4 le faltaban en `gradle/libs.versions.toml` las librerías Retrofit, OkHttp logging, Coil y MockK que usa `app/build.gradle.kts` (sin ellas Gradle no sincroniza). Se agregaron con las versiones que indica el README.

## Cómo funciona confirmar el carrito ahora

```
CarritoViewModel.confirmarPedido()
  1. ms-productos  POST /api/productos/disponibilidad   → ¿hay stock real y está activo?
        ├─ servidor apagado → Snackbar "No hay conexión…", el carrito queda igual
        └─ falta stock      → "No hay stock suficiente de …", se vuelve a leer el catálogo
  2. ms-pedidos    POST /api/pedidos                    → guarda la solicitud (recalcula el total)
  3. ms-productos  POST /api/productos/descontar-stock  → descuenta (todo o nada, 409 si no alcanza)
  4. se vacía el carrito
```

## Archivos nuevos o modificados (app)

| Capa | Archivo | Cambio |
| --- | --- | --- |
| model | `model/Stock.kt` | `ItemStock`, `ProblemaStock`, `MotivoStock` |
| domain | `domain/IdsProducto.kt`, `domain/DisponibilidadStock.kt` | Reglas puras (las mismas del servidor) |
| remote | `remote/ProductosApi.kt`, `remote/dto/ProductoDto.kt`, `remote/MapeadoresProducto.kt`, `remote/ClienteHttp.kt` | Retrofit de ms-productos |
| repository | `ProductRepository.kt` (contrato + memoria), `ProductRepositoryRemoto.kt` (nuevo), `db/ProductoDao.kt` (sincronización) | Reemplaza a `RoomProductRepository` |
| viewmodel | Catálogo, FormularioProducto, DetalleProducto, Carrito | Llamadas al servidor y manejo de errores |
| ui | CatalogoScreen, FormularioProductoScreen, DetalleProductoScreen | Botón Actualizar, aviso de conexión y Snackbars |
| build | `app/build.gradle.kts` (`URL_MS_PRODUCTOS`), `gradle/libs.versions.toml` | Puerto 8083 y librerías |

## Pendiente antes de entregar

- [ ] Probar en el emulador: crear, editar y desactivar un producto; confirmar un carrito; dejar un producto sin stock desde Swagger y ver el aviso; apagar ms-productos y ver «Sin conexión».
- [ ] Volver a generar el APK firmado (`./gradlew assembleRelease`) para que incluya el catálogo conectado y copiarlo a `entrega/`.
- [ ] Mover en Trello las tarjetas de ms-productos, integración del catálogo, stock, pruebas y guías.
- [ ] Coordinar con el equipo el orden de los Pull Requests: primero se une el #29 y después esta rama (o esta rama apunta a `feature/ev4-microservicios`).
