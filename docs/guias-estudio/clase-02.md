# Clase 2: Jetpack Compose: frontend y navegación

**Objetivo:** explicar cómo se dibujan las pantallas, cómo reciben el estado del ViewModel y cómo se navega entre ellas.

## Lo esencial

- Una pantalla es una función `@Composable` que **solo dibuja** el estado que recibe y avisa los eventos (clics, texto escrito).
- El estado del ViewModel se lee con `val estado by viewModel.uiState.collectAsStateWithLifecycle()`: cada vez que cambia, Compose vuelve a dibujar (recomposición).
- `remember { ... }` guarda algo mientras la pantalla está visible (por ejemplo el `SnackbarHostState`); `rememberSaveable` además sobrevive a girar el teléfono.
- `LaunchedEffect(clave) { ... }` ejecuta código (por ejemplo mostrar un Snackbar) cuando cambia la clave.
- `Scaffold` arma la estructura: `topBar`, `snackbarHost`, `floatingActionButton` y contenido.
- `LazyColumn` / `LazyVerticalGrid` para listas largas: solo dibujan lo visible.
- **Navegación:** `Routes.kt` define las rutas (`detalle_producto/{productoId}`) y `AppNavHost.kt` asocia cada ruta con su pantalla. `navController.navigate(Routes.DetalleProducto.crear("p01"))`.

## Dónde mirarlo en el proyecto

| Pantalla | Archivo | Qué observar |
| --- | --- | --- |
| Catálogo | `app/src/main/java/com/example/museo_de_los_museos_app/ui/screens/catalogo/CatalogoScreen.kt` | Botón **Actualizar** (ícono `Refresh`) y `AvisoConexion` cuando ms-productos no responde |
| Formulario de producto | `app/src/main/java/com/example/museo_de_los_museos_app/ui/screens/formularioproducto/FormularioProductoScreen.kt` | `LaunchedEffect(estado.guardado)` vuelve atrás; Snackbar con el error del servidor |
| Detalle de producto | `app/src/main/java/com/example/museo_de_los_museos_app/ui/screens/detalleproducto/DetalleProductoScreen.kt` | Botón activar/desactivar del Administrador y Snackbar de error |
| Carrito | `app/src/main/java/com/example/museo_de_los_museos_app/ui/screens/carrito/CarritoScreen.kt` | Formulario validado por campo y aviso «No hay stock suficiente» |
| Rutas | `app/src/main/java/com/example/museo_de_los_museos_app/navigation/Routes.kt`, `AppNavHost.kt` | Rutas con argumentos (`{productoId}`) y opcionales (`?productoId=`) |

## Cómo lo explico en la defensa

> "La pantalla no tiene lógica: recibe `estado` y llama funciones del ViewModel como `viewModel::actualizar`. Si el servidor rechaza un producto, el ViewModel pone `errorRemoto` en el estado y la pantalla lo muestra con un Snackbar dentro de un `LaunchedEffect`."

## Práctica (20 min)

1. Sigue el clic de **Actualizar** del catálogo: `CatalogoScreen` → `CatalogoViewModel.actualizar()` → `ProductRepository.sincronizar()`.
2. Navega desde el catálogo a un producto y luego al formulario de edición: ¿qué argumento viaja en la ruta?

## Preguntas del quiz de esta clase (sección 02)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**011. ¿Qué marca una función de interfaz declarativa de Jetpack Compose?**  
✅ C) La anotación `@Composable` — `@Composable` identifica funciones que describen contenido de interfaz en Compose.

**012. ¿Qué efecto tiene `var contador by remember { mutableIntStateOf(0) }` dentro de un composable?**  
✅ D) Conserva un estado local durante recomposiciones y observa cambios para actualizar UI — `remember` conserva estado entre recomposiciones mientras la instancia siga en composición; el estado observable dispara actualizaciones.

**013. ¿Por qué se usa `rememberSaveable` en un formulario simple?**  
✅ B) Para restaurar estado compatible frente a recreaciones de actividad y procesos cuando es posible — `rememberSaveable` utiliza mecanismos de estado guardado para tipos admitidos.

**014. ¿Qué hace `Modifier.padding(16.dp)`?**  
✅ C) Agrega espacio interior alrededor del contenido del elemento — `padding` aplica un espaciado en el diseño del componente.

**015. ¿Qué parámetro controla el texto visible de `TextField` en Compose?**  
✅ B) `value` — `value` recibe el estado del texto; `onValueChange` permite actualizarlo.

**016. ¿Qué debe hacer `onValueChange = { nuevo -> texto = nuevo }` en un `TextField` controlado?**  
✅ B) Actualizar el estado que se entrega nuevamente en `value` — El campo muestra el estado; al actualizarlo, Compose refleja el nuevo valor.

**017. ¿Qué componente organiza hijos verticalmente en Compose?**  
✅ D) `Column` — `Column` dispone sus elementos uno debajo de otro.

**018. ¿Qué componente conviene para una lista larga desplazable de registros?**  
✅ C) `LazyColumn` — `LazyColumn` compone bajo demanda los elementos visibles, reduciendo trabajo innecesario.

**019. ¿Qué hace `navController.navigate("detalle/7")` en Navigation Compose si existe esa ruta?**  
✅ C) Solicita navegar al destino cuya ruta corresponde a `detalle/7` — `NavController` gestiona destinos y navegación; la ruta puede incluir argumentos.

**020. ¿Por qué conviene que un composable de pantalla reciba el estado y callbacks desde un ViewModel?**  
✅ A) Para separar presentación de lógica y facilitar pruebas — La pantalla representa estado y comunica acciones; el ViewModel gestiona la lógica de presentación.

---

[← Clase 1](clase-01.md) · [Índice](README.md) · [Clase 3 →](clase-03.md)
