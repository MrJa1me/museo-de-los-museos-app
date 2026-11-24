# Clase 3: Arquitectura MVVM, Model y Repository

**Objetivo:** justificar la arquitectura MVVM de la app y el patrón Repository (pasos 2 y 5 de la defensa).

## Lo esencial

```
View (Compose) ──eventos──▶ ViewModel ──▶ Repository ──▶ Retrofit ──▶ Microservicio (fuente de verdad)
      ▲                        │ StateFlow<UiState>  │
      └──────── estado ────────┘                     └──▶ Room (copia local, funciona sin conexión)
```

- **Model**: los datos (`Product`, `Order`, `Museum`).
- **View**: dibuja el estado y envía eventos. No sabe de dónde vienen los datos.
- **ViewModel**: guarda el estado de la pantalla (`MutableStateFlow` privado, `StateFlow` público) y la lógica de presentación. Sobrevive al giro de pantalla. Lanza corrutinas con `viewModelScope.launch`.
- **Repository**: decide de dónde vienen los datos (microservicio, Room o memoria). Las pantallas dependen de la **interfaz** (`ProductRepository`), no de la implementación.
- **Inyección por constructor**: `CatalogoViewModel(repositorio: ProductRepository = Repositorios.productos)`. En la app se usa el remoto; en las pruebas se pasa un fake (`InMemoryProductRepository` o `ProductosConFallo`).

## El catálogo con MVVM, paso a paso

| Capa | Archivo | Responsabilidad |
| --- | --- | --- |
| Model | `app/src/main/java/com/example/museo_de_los_museos_app/model/Product.kt`, `model/Stock.kt` | Producto, línea de stock y problema de stock |
| Repository (contrato) | `app/src/main/java/com/example/museo_de_los_museos_app/repository/ProductRepository.kt` | `sincronizar`, `crear`, `actualizar`, `cambiarActivo`, `verificarDisponibilidad`, `descontarStock` |
| Repository (real) | `app/src/main/java/com/example/museo_de_los_museos_app/repository/ProductRepositoryRemoto.kt` | ms-productos + copia en Room |
| Repository (pruebas) | `InMemoryProductRepository` (mismo archivo del contrato) | Lista en memoria, sin red |
| ViewModel | `viewmodel/catalogo`, `viewmodel/formularioproducto`, `viewmodel/detalleproducto`, `viewmodel/carrito` | Estado + lógica de cada pantalla |
| View | `ui/screens/...` | Dibujar |

## Cómo lo explico en la defensa

> "Usamos MVVM para separar la pantalla de la lógica. Así el ViewModel se prueba sin emulador y la pantalla se puede cambiar sin tocar la lógica. El repositorio esconde si los datos vienen del microservicio o de Room: el ViewModel solo llama `productos.crear(...)` y recibe un `Result`."

## Práctica (20 min)

1. Dibuja en papel el flujo de **desactivar un producto**: `DetalleProductoScreen` → `alternarActivo()` → `cambiarActivo()` → `PATCH /api/productos/{id}/activo` → Room → catálogo actualizado.
2. Explica por qué `_uiState` es privado y `uiState` público.

## Preguntas del quiz de esta clase (sección 03)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**021. En MVVM, ¿qué componente suele contener el estado y la lógica de presentación de una pantalla?**  
✅ B) ViewModel — El ViewModel prepara y expone el estado que la vista observa.

**022. En una aplicación Compose con MVVM, ¿qué función corresponde principalmente a la View?**  
✅ D) Dibujar la interfaz y remitir acciones del usuario — La vista se ocupa de presentar el estado y recoger interacciones.

**023. ¿Qué representa una clase `Producto(id, nombre, precio)` en la capa Model?**  
✅ A) Los datos del dominio o transferencia de información — Un modelo expresa información que utiliza la aplicación, sin ser necesariamente una vista.

**024. ¿Cuál es la función principal del patrón Repository?**  
✅ C) Abstraer las fuentes de datos y operaciones de acceso — El Repository centraliza cómo se obtienen o modifican datos desde API, caché o BD.

**025. ¿Por qué un ViewModel no debería depender directamente de un `Activity` como referencia duradera?**  
✅ A) Puede causar acoplamiento y fugas de memoria durante recreaciones — Un ViewModel puede sobrevivir recreaciones; retener vistas o actividades puede dejar referencias obsoletas.

**026. ¿Qué ventaja aporta exponer `StateFlow<UiState>` desde un ViewModel?**  
✅ B) La UI puede observar un estado consistente y reaccionar a cambios — `StateFlow` mantiene un valor actual observable que Compose puede recolectar.

**027. ¿Qué patrón representa `private val _estado = MutableStateFlow(...); val estado = _estado.asStateFlow()`?**  
✅ D) Estado modificable internamente y de solo lectura para consumidores — La mutabilidad se encapsula en el ViewModel y otros componentes observan `StateFlow`.

**028. ¿Qué problema busca resolver un `UiState` con `loading`, `data` y `error`?**  
✅ D) Representar claramente los estados de carga, éxito y error — Una pantalla asíncrona necesita reflejar solicitudes en curso, resultados y fallos.

**029. ¿Por qué es útil la inyección de dependencias de un Repository en el ViewModel?**  
✅ B) Permite sustituir dependencias por dobles de prueba y reducir acoplamiento — Al recibir su dependencia por constructor, el ViewModel puede probarse con un repositorio falso o mock.

**030. ¿Qué función tiene `viewModelScope.launch { ... }`?**  
✅ A) Iniciar una corrutina asociada al ciclo de vida del ViewModel — Las corrutinas de `viewModelScope` se cancelan cuando el ViewModel se limpia.

---

[← Clase 2](clase-02.md) · [Índice](README.md) · [Clase 4 →](clase-04.md)
