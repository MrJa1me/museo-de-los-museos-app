# Museo de los Museos – App móvil

Proyecto de Desarrollo de Aplicaciones Móviles (DSY1105, Duoc UC · Evaluación Parcial 2).
App móvil Android que unifica el catálogo de cursos y productos museológicos (con solicitudes de compra y cálculo de envío simulado) y el Mapa de Museos Virtuales de Iberoamérica. Todos los datos son ficticios.

## Integrantes

| Parte | Integrante | GitHub | Responsabilidad |
| --- | --- | --- | --- |
| P1 | Alan | [Nariyato] | Base técnica del proyecto, persistencia con Room/SQLite, repositorios, `SessionManager`, tema (modo claro/oscuro), registro, login y perfil con cámara/galería |
| P2 | Nicolas | [MrJa1me] | Navegación, bienvenida, dashboard y notificaciones |
| P3 | Benjamin | [benjalbsp] | Mapa de museos, detalle de museo, lector QR, favoritos e historial de QR |
| P4 | Alanis | [AlanisVerdugo] | Catálogo, detalle de producto, carrito, cálculo de envío e historial de pedidos |

## Funcionalidades

- **Cuentas y sesión**: registro e inicio de sesión con tres roles (Administrador, Investigador y Cliente). La sesión se mantiene al cerrar la app.
- **Bienvenida y dashboard**: pantalla de bienvenida y panel principal según el rol del usuario.
- **Mapa de museos**: mapa de OpenStreetMap (osmdroid) con los museos virtuales de Iberoamérica y filtro por país.
- **Detalle de museo**: ficha del museo con opción de agregarlo a favoritos.
- **Favoritos**: lista de museos favoritos guardada por usuario.
- **Lector QR**: escaneo de códigos QR con Google Code Scanner, pantalla de resultado e historial de QR escaneados por usuario.
- **Catálogo**: listado de cursos y productos museológicos y detalle de cada producto.
- **Carrito y envío**: carrito por usuario y cálculo de envío simulado al confirmar la solicitud.
- **Historial de pedidos**: solicitudes realizadas y su estado.
- **Notificaciones**: avisos dentro de la app por usuario, que se pueden marcar como leídos.
- **Perfil**: edición de datos y foto de perfil tomada con la cámara o elegida desde la galería (imagen circular con ícono por defecto).
- **Tema propio**: colores personalizados de la app con modo oscuro automático según el sistema. El ícono de la app es adaptativo y admite íconos temáticos.

## Tecnologías

| Tecnología | Versión | Uso |
| --- | --- | --- |
| Kotlin | 2.2.10 | Lenguaje |
| Android Gradle Plugin | 9.4.1 | Compilación (minSdk 26, targetSdk 37) |
| Jetpack Compose (BOM) + Material 3 | 2026.02.01 | Interfaz |
| MVVM con ViewModel + StateFlow | lifecycle-viewmodel-compose 2.8.0-rc02 · coroutines 1.7.3 | Arquitectura y estado de pantallas |
| Navigation Compose | 2.7.7 | Navegación entre pantallas |
| Room (SQLite) + kapt (`com.android.legacy-kapt`) | 2.8.5 | Base de datos local |
| DataStore Preferences | 1.1.0 | **Solo** para guardar la sesión activa |
| osmdroid | 6.1.20 | Mapa (OpenStreetMap) |
| Google Code Scanner (Play Services) | 16.1.0 | Lectura de códigos QR |
| FileProvider + `TakePicture` / `GetContent` | — | Foto de perfil con cámara o galería |
| JUnit | 4.13.2 | Pruebas unitarias |

## Persistencia (SQLite con Room)

Todos los datos de la app se guardan en una base SQLite local llamada **`museo.db`** (clase `AppDatabase`, versión 5). Al iniciar por primera vez se carga con datos semilla (`SeedData`).

| Tabla | Entidad | Contenido |
| --- | --- | --- |
| `usuarios` | `UsuarioEntity` | Cuentas registradas (correo único) |
| `museos` | `MuseoEntity` | Museos virtuales del mapa |
| `productos` | `ProductoEntity` | Cursos y productos del catálogo |
| `favoritos` | `FavoritoEntity` | Museos favoritos por usuario (clave: `userId` + `museoId`) |
| `historial_qr` | `HistorialQrEntity` | QR escaneados por usuario (clave: `userId` + `contenido`) |
| `carrito` | `CarritoEntity` | Productos en el carrito por usuario (clave: `userId` + `productoId`) |
| `pedidos` | `PedidoEntity` | Solicitudes de compra |
| `pedido_items` | `PedidoItemEntity` | Detalle de productos de cada pedido |

> DataStore se usa únicamente para recordar la sesión. Las **notificaciones** son datos de prueba definidos en el código (`InMemoryNotificationRepository`), no se guardan en SQLite.

## Arquitectura

La app sigue el patrón **MVVM**:

- **Model**: clases de datos (`model/`) y acceso a datos mediante repositorios (`repository/`), que leen y escriben en Room.
- **ViewModel**: cada pantalla tiene su `ViewModel`, que expone un `UiState` con `StateFlow` y contiene la lógica de la pantalla.
- **View**: las pantallas en Jetpack Compose (`ui/screens/`) solo muestran el estado y envían las acciones del usuario al ViewModel.

### Estructura del proyecto

```
app/src/main/java/com/example/museo_de_los_museos_app/
├── MainActivity.kt
├── model/              clases de datos y enumeraciones (User, Museum, Product, Order, CartItem, ...)
├── domain/             reglas de negocio y validaciones (CalculadoraEnvio, LectorQr, Validadores, ...)
├── repository/         repositorios, SeedData, SessionManager y AppDataStore (sesión)
│   └── db/             AppDatabase, entidades (@Entity) y DAOs de Room
├── navigation/         rutas (Routes) y AppNavHost
├── viewmodel/          un paquete por pantalla (ViewModel + UiState)
│   ├── bienvenida/  carrito/  catalogo/  dashboard/  detallemuseo/  detalleproducto/
│   ├── escanerqr/  favoritos/  historial/  login/  mapa/  notificaciones/
│   └── perfil/  registro/  resultadoqr/
└── ui/
    ├── screens/        una carpeta por pantalla (solo el Screen)
    │   └── (mismas carpetas que viewmodel/)
    ├── components/     componentes reutilizables (BotonPrincipal, CampoTextoValidado, ImgInteligente, ...)
    ├── theme/          colores, tema claro/oscuro y tipografía
    └── utils/          formato y mensajes de UI
```

## Cómo ejecutar

1. Clonar el repositorio:
   ```
   git clone https://github.com/MrJa1me/museo-de-los-museos-app.git
   ```
2. Abrir el proyecto en Android Studio y esperar la sincronización de Gradle (necesita Internet la primera vez).
3. Usar un emulador **Google Play** (API 37) o un teléfono físico (el lector QR requiere Google Play Services).
4. Ejecutar el módulo `app`.

### Usuarios de prueba (ficticios, definidos en `SeedData`)

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | admin@museo.test | Admin123 |
| Investigador | investigador@museo.test | Invest123 |
| Cliente | cliente1@museo.test | Cliente123 |
| Cliente | cliente2@museo.test | Cliente123 |

## Tests

Las pruebas unitarias están en `app/src/test/`. Para ejecutarlas:

```
./gradlew test
```

(o desde Android Studio: clic derecho en la carpeta `test` → *Run Tests*).

| Paquete | Clases de prueba |
| --- | --- |
| `domain` | `CalculadoraEnvioTest`, `LectorQrTest`, `ValidadorFichaMuseoTest`, `ValidadoresCuentaTest`, `ValidadoresTest` |
| `repository` | `HistorialQrTest` |
| `ui.utils` | `FormatoTest` |
| `viewmodel` | `CarritoViewModelTest`, `DashboardViewModelTest`, `DetalleProductoViewModelTest`, `HistorialViewModelTest`, `NotificacionesViewModelTest` |

## Flujo de trabajo del equipo

- `main` es la rama estable: solo P1 integra.
- Cada integrante trabaja en su rama `feature/pX-...` y hace commits pequeños con mensajes técnicos en español.
- El avance se registra en el tablero de Trello del proyecto.

## Enlaces

- Repositorio: https://github.com/MrJa1me/museo-de-los-museos-app
- Tablero Trello: https://trello.com/b/E2TN3rMG

