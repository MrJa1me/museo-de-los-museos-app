# Clase 1: Kotlin: lenguaje y concurrencia

**Objetivo:** leer y explicar el Kotlin del proyecto sin miedo: tipos nulos, `data class`, colecciones, `when` y funciones `suspend`.

## Lo esencial

| Concepto | Qué significa | Ejemplo en nuestro proyecto |
| --- | --- | --- |
| `val` / `var` | `val` no se puede reasignar; `var` sí | `val productos: StateFlow<List<Product>>` en `ProductRepository` |
| Tipos anulables `String?` | La variable puede valer `null` | `ProductoDto(val id: String? = null, ...)` |
| Llamada segura `?.` | Si es `null`, no llama y devuelve `null` | `id?.takeIf { it.isNotBlank() }` en `MapeadoresProducto.kt` |
| Elvis `?:` | Valor por defecto si lo de la izquierda es `null` | `precio = precio ?: 0` |
| `data class` | Genera `equals`, `toString`, `copy`... | `Product`, `ItemStock`, `ProblemaStock` |
| `copy()` | Crea una copia cambiando algunos campos | `producto.copy(activo = false)` |
| Colecciones | `filter`, `map`, `mapNotNull`, `groupBy`, `sumOf` | `DisponibilidadStock.revisar(...)` usa `groupBy` y `sumOf` |
| `when` | `switch` mejorado que devuelve un valor | `IdsProducto.prefijo(categoria)` |
| `enum class` | Lista cerrada de valores | `CategoriaProducto`, `MotivoStock`, `EstadoPedido` |
| `suspend` | Función que espera (red, base de datos) sin bloquear la pantalla | `suspend fun crear(producto: Product): Result<Product>` |
| `Result<T>` | Éxito o error sin lanzar excepciones hacia la pantalla | `.onSuccess { }` / `.onFailure { }` en `FormularioProductoViewModel` |

## Dónde mirarlo en el proyecto

- `app/src/main/java/com/example/museo_de_los_museos_app/model/Product.kt`: `data class` con propiedades calculadas (`requiereEnvio`, `disponible`).
- `app/src/main/java/com/example/museo_de_los_museos_app/domain/IdsProducto.kt`: `when`, `Regex`, `mapNotNull`, `maxOrNull() ?: 0`.
- `app/src/main/java/com/example/museo_de_los_museos_app/domain/DisponibilidadStock.kt`: `groupBy`, `sumOf`, `when` sin argumento.
- `app/src/main/java/com/example/museo_de_los_museos_app/remote/MapeadoresProducto.kt`: `?.`, `?:`, `orEmpty()`, funciones de extensión (`fun ProductoDto.aModelo()`).

## Cómo lo explico en la defensa

> "Los DTO tienen todos sus campos anulables porque Gson no respeta los valores por defecto de Kotlin. El mapeador usa `?:` y `orEmpty()` para poner un valor seguro, y si falta el id devuelve `null` y la fila se descarta con `mapNotNull`."

## Práctica (15 min)

1. Abre `IdsProducto.kt` y explica en voz alta qué devuelve `siguienteId(MATERIAL_CONSERVACION, listOf("m01k", "m02k"))` (respuesta: `m03k`).
2. En `DisponibilidadStock.kt`, ¿qué pasa si el mismo producto viene en dos líneas del carrito? (se suman con `groupBy` + `sumOf`).

## Preguntas del quiz de esta clase (sección 01)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**001. ¿Qué imprime Kotlin con `val x = 4; println("Valor: ${x * 2}")`?**  
✅ C) Valor: 8 — La plantilla `${...}` evalúa la expresión `4 * 2` y la inserta en el String.

**002. ¿Qué propiedad tiene `val` en Kotlin?**  
✅ B) La referencia no puede reasignarse después de inicializarse — `val` impide reasignar la referencia; un objeto mutable puede modificar su contenido.

**003. ¿Qué devuelve `nombre?.length` si `nombre` es `null`?**  
✅ C) `null` — El operador de llamada segura `?.` no invoca `length` cuando el receptor es nulo.

**004. ¿Qué expresión entrega `"Sin nombre"` cuando `nombre` es `null`?**  
✅ A) `nombre ?: "Sin nombre"` — El operador Elvis `?:` utiliza el valor de la derecha si la expresión izquierda es nula.

**005. ¿Qué representa `data class Usuario(val id: Long, val nombre: String)`?**  
✅ C) Un modelo que genera `equals`, `hashCode`, `toString` y `copy`, entre otros — Una `data class` facilita representar datos y genera métodos útiles sobre las propiedades del constructor primario.

**006. ¿Qué devuelve `listOf(1, 2, 3).filter { it > 1 }`?**  
✅ C) Una lista con 2 y 3 — `filter` conserva los elementos que cumplen el predicado.

**007. ¿Cuál es la diferencia correcta entre `List` y `MutableList` en Kotlin?**  
✅ D) `MutableList` permite operaciones de modificación de la colección — La interfaz `List` ofrece acceso de solo lectura; `MutableList` agrega operaciones de modificación.

**008. ¿Qué hace una función `suspend` en Kotlin?**  
✅ A) Puede suspender su ejecución sin bloquear necesariamente el hilo mientras espera — Las funciones `suspend` trabajan con corrutinas y pueden ceder la ejecución durante operaciones asíncronas.

**009. ¿Qué ocurre con `val edad = "20".toIntOrNull()`?**  
✅ B) `edad` vale 20 como `Int?` — `toIntOrNull()` convierte una cadena válida a entero y devuelve un tipo anulable.

**010. ¿Qué resultado produce `when (estado) { 200 -> "OK"; 404 -> "No encontrado"; else -> "Otro" }` cuando `estado = 404`?**  
✅ C) No encontrado — `when` elige la rama cuyo valor coincide con `estado`.

---

[Índice](README.md) · [Clase 2 →](clase-02.md)
