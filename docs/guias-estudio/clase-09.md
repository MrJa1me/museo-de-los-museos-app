# Clase 9: Pruebas unitarias, JUnit, MockK y cobertura

**Objetivo:** ejecutar pruebas en vivo y explicar qué se prueba, qué se simula y cómo (paso 6). Material: Guía 15 y diapositiva de pruebas unitarias.

## Lo esencial

- **Prueba unitaria**: revisa una unidad (un ViewModel, un repositorio, una función) con sus dependencias controladas. Rápida y sin emulador. Va en `app/src/test/`.
- **Prueba instrumentada**: corre en el emulador (`app/src/androidTest/`), por ejemplo pruebas de UI con `ComposeTestRule`.
- `@Test`, `@Before`, `@After`; `assertEquals(esperado, real)`, `assertTrue`, `assertNull`.
- **MockK**: `mockk<ProductosApi>()` crea un simulado; `coEvery { api.listar() } returns ...` define qué responde una función `suspend`; `coVerify(exactly = 0) { dao.reemplazarTodos(any()) }` comprueba que algo **no** se llamó. `relaxed = true` responde valores vacíos sin configurarlos.
- **Fake vs mock**: un *fake* es una implementación simple que funciona de verdad en memoria (`InMemoryProductRepository`, `ProductosConFallo`); un *mock* se programa respuesta por respuesta y permite verificar llamadas.
- `runTest` y `UnconfinedTestDispatcher` para probar corrutinas sin esperas reales; `Dispatchers.setMain(...)` para que `viewModelScope` funcione en el PC.
- **Cobertura**: porcentaje de líneas ejecutadas por las pruebas (JaCoCo). La pauta pide 80 % de la lógica. No garantiza que no haya errores.

## Pruebas del catálogo (lo que tú agregaste)

| Prueba | Qué verifica | Qué se simula |
| --- | --- | --- |
| `repository/ProductRepositoryRemotoTest` | Sincronizar, crear sin id, 400 con el campo, PATCH, disponibilidad, 409 al descontar | **MockK**: `ProductosApi` y `ProductoDao` |
| `remote/MapeadoresProductoTest` | JSON con nulos, categoría desconocida, curso sin peso | Nada |
| `domain/IdsProductoTest`, `domain/DisponibilidadStockTest` | Reglas puras (ids, stock) | Nada |
| `viewmodel/carrito/CarritoStockServidorTest` | Otro cliente compró antes → no se crea el pedido; servidor apagado → carrito intacto | Fake `ProductosConFallo` |
| `viewmodel/catalogo/CatalogoRemotoTest` | Botón Actualizar y aviso «Sin conexión» | Fake `ProductosConFallo` |
| `viewmodel/formularioproducto/FormularioProductoRemotoTest` | Servidor rechaza → mensaje y no vuelve atrás | Fake `ProductosConFallo` |
| `DetalleProductoRemotoTest` | Desactivar sin servidor no cambia nada | Fake `ProductosConFallo` |
| `ms-productos`: `ProductoServiceTest`, `ProductoControllerTest`, `MsProductosApplicationTests` | Reglas, rutas/códigos HTTP, contexto completo | **Mockito** (repositorio JPA) y **MockMvc** |

## Prueba para explicar en la defensa

`ProductRepositoryRemotoTest.sincronizar_sinRed_noTocaRoom_yAvisaSinConexion`

```kotlin
coEvery { api.listar() } throws IOException("servidor apagado")   // 1. simulo el servidor apagado
val resultado = repo.sincronizar()                                 // 2. ejecuto
assertEquals(TipoErrorRemoto.SIN_CONEXION, (resultado.exceptionOrNull() as ErrorRemoto).tipo)
coVerify(exactly = 0) { dao.reemplazarTodos(any()) }               // 3. Room no se borró
assertEquals(EstadoConexion.SIN_CONEXION, repo.conexion.value)     // 4. la UI mostrará el aviso
```

> "Simulo la API con MockK para no depender del servidor ni de internet: la prueba es rápida y siempre da el mismo resultado. Verifico que si el servidor está apagado no se borra la copia local y que el estado de conexión cambia para mostrar el aviso."

## Cómo ejecutarlas

```bash
./gradlew testDebugUnitTest          # todas las pruebas de la app
./gradlew coberturaLogica            # pruebas + reporte JaCoCo (app/build/reports/jacoco/coberturaLogica/html/index.html)
cd microservicios/ms-productos && ./mvnw test
```

En Android Studio: clic derecho en `app/src/test` → **Run Tests**, o en el triángulo verde junto a una prueba.

## Práctica (30 min)

1. Escribe en `DisponibilidadStockTest` una prueba que pida 26 unidades de `p01` (stock 25) y verifique `MotivoStock.SIN_STOCK`.
2. Rompe a propósito `IdsProducto.sufijo` (que devuelva `""` siempre) y mira qué pruebas fallan. Luego arréglalo.

## Preguntas del quiz de esta clase (sección 09)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**081. ¿Qué caracteriza una prueba unitaria?**  
✅ A) Valida una unidad pequeña de comportamiento de forma aislada o controlada — Una prueba unitaria comprueba el comportamiento de una unidad con dependencias controladas.

**082. ¿Qué función tiene `@Test` de JUnit?**  
✅ D) Identificar un método o función como prueba ejecutable — JUnit descubre y ejecuta los tests marcados con `@Test`.

**083. ¿Qué comprueba `assertEquals(5, resultado)`?**  
✅ C) Que `resultado` sea igual a 5 según la comparación usada — La aserción compara el resultado real con el valor esperado.

**084. ¿Para qué se usa `mockk<ProductoRepository>()`?**  
✅ C) Crear un doble simulado de esa dependencia para pruebas — MockK permite controlar las respuestas y verificar interacciones de dependencias.

**085. ¿Qué indica `coEvery { api.obtener() } returns datos` en MockK?**  
✅ A) Configura la respuesta de una función suspendida simulada — `coEvery` prepara comportamientos para llamadas suspendidas en mocks.

**086. ¿Qué verifica `coVerify(exactly = 1) { api.obtener() }`?**  
✅ D) Que la llamada suspendida simulada se haya realizado exactamente una vez — `coVerify` comprueba interacciones con un mock de función suspendida.

**087. ¿Qué ventaja tiene `runTest` de `kotlinx.coroutines.test`?**  
✅ A) Proporciona un entorno controlado para probar corrutinas suspendidas — `runTest` administra un ámbito y planificador de prueba para código con corrutinas.

**088. ¿Qué debe probar un ViewModel que carga productos desde un Repository simulado?**  
✅ B) Que emita el estado esperado ante éxito y error — Se verifica la lógica de estado de presentación separada de la red.

**089. ¿Qué significa alcanzar 80% de cobertura de líneas medida por una herramienta?**  
✅ A) Que se ejecutó al menos el 80% de las líneas instrumentadas según esa métrica — Cobertura cuantifica ejecución de código; no garantiza ausencia de defectos.

**090. ¿Qué diferencia práctica hay entre un mock y una implementación fake?**  
✅ B) El mock suele configurarse/verificarse por llamadas; el fake implementa comportamiento simplificado — Ambos sustituyen dependencias, pero difieren en cómo representan comportamiento e interacciones.

---

[← Clase 8](clase-08.md) · [Índice](README.md) · [Clase 10 →](clase-10.md)
