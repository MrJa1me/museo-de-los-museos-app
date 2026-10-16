# Museo de los Museos – App móvil

Proyecto de Desarrollo de Aplicaciones Móviles (DSY1105, Duoc UC · Evaluación Parcial 2).
App móvil Android que unifica el catálogo de cursos y productos museológicos (con solicitudes de compra y cálculo de envío simulado) y el Mapa de Museos Virtuales de Iberoamérica. Todos los datos son ficticios.

## Integrantes

| Parte | Integrante | Responsabilidad |
| --- | --- | --- |
| P1 | _Alan_ | Base técnica, sesión, registro, login y perfil |
| P2 | _Nicolas_ | Navegación, bienvenida, dashboard y notificaciones |
| P3 | _Benjamin_ | Museos, mapa, QR y favoritos |
| P4 | _Alanis_ | Catálogo, carrito, envío e historial |

## Funcionalidades (en desarrollo)

- Registro e inicio de sesión con tres roles: Administrador, Investigador y Cliente.
- Mapa de museos virtuales (OpenStreetMap), detalle, favoritos por usuario y lectura de códigos QR.
- Catálogo de cursos y productos, carrito, cálculo de envío simulado e historial de solicitudes.
- Notificaciones dentro de la app y perfil editable.

## Tecnologías

Kotlin · Jetpack Compose (Material 3) · MVVM con StateFlow · Navigation Compose · DataStore Preferences · osmdroid (mapa) · Google Code Scanner (QR).

## Cómo ejecutar

1. Abrir el proyecto en Android Studio y esperar la sincronización de Gradle (necesita Internet la primera vez).
2. Usar un emulador **Google Play** (API 37) o un teléfono físico.
3. Ejecutar el módulo `app`.

## Estructura del proyecto

```
app/src/main/java/com/example/museo_de_los_museos_app/
├── model/          entidades y enumeraciones
├── repository/     repositorios, datos semilla (SeedData) y SessionManager
├── navigation/     rutas (Routes) y NavHost
├── viewmodel/      ViewModels compartidos
└── ui/
    ├── components/ componentes reutilizables
    ├── screens/    una carpeta por pantalla (Screen + ViewModel + UiState)
    ├── theme/      tema Material 3
    └── utils/      utilidades de UI
```

## Usuarios de prueba (ficticios)

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | admin@museo.test | Admin123 |
| Investigador | investigador@museo.test | Invest123 |
| Cliente | cliente1@museo.test | Cliente123 |
| Cliente | cliente2@museo.test | Cliente123 |

## Flujo de trabajo del equipo

- `main` es la rama estable: solo P1 integra.
- Cada integrante trabaja en su rama `feature/pX-...` y hace commits pequeños con mensajes técnicos en español.
- El avance se registra en el tablero de Trello del proyecto.
