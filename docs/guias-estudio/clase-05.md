# Clase 5: Spring Boot: controller, service y arquitectura backend

**Objetivo:** explicar las capas de un microservicio (pasos 2 y 3) usando **ms-productos** como ejemplo.

## Lo esencial

```
controller  →  service  →  repository (Spring Data JPA)  →  base de datos (H2 / MySQL)
     │             │
  dto (@Valid)   exception (GlobalExceptionHandler → JSON 400/404/409)
```

- `@RestController` + `@RequestMapping("/api/productos")`: recibe HTTP y responde JSON. **No** tiene reglas de negocio.
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping`, `@DeleteMapping`.
- `@PathVariable` toma `{id}` de la ruta; `@RequestParam` toma `?categoria=`; `@RequestBody` toma el JSON del cuerpo.
- `@Valid` revisa las anotaciones del DTO (`@NotBlank`, `@Min`, `@Size`...) y si falla responde **400** con el campo.
- `@Service`: las reglas (asignar id, peso obligatorio salvo cursos, stock suficiente).
- **Inyección por constructor**: `public ProductoService(ProductoRepository repositorio)`.
- `ResponseEntity.status(HttpStatus.CREATED).body(...)` para responder **201**.
- `@RestControllerAdvice` (`GlobalExceptionHandler`) convierte las excepciones en JSON con el código correcto.

## ms-productos (puerto 8083)

| Paquete | Clase | Qué hace |
| --- | --- | --- |
| `controller` | `ProductoController` | 8 endpoints (ver tabla) |
| `service` | `ProductoService` | Reglas del catálogo y del stock |
| `repository` | `ProductoRepository` | `JpaRepository<Producto, String>` + consultas por nombre de método |
| `model` | `Producto`, `Categoria` | `@Entity` tabla `productos` |
| `dto` | `ProductoRequest`, `CambioActivoRequest`, `StockRequest`, `ItemStockRequest`, `DisponibilidadResponse`, `ProblemaStock` | Forma del JSON + validaciones |
| `exception` | `GlobalExceptionHandler`, `RecursoNoEncontradoException` (404), `ConflictoException` (409), `DatoInvalidoException` (400) | Errores en JSON |
| `config` | `DatosIniciales` | Carga los 10 productos de prueba la primera vez |

| Método | Ruta | Regla |
| --- | --- | --- |
| GET | `/api/productos?categoria=&activo=` | Filtros opcionales |
| GET | `/api/productos/{id}` | 404 si no existe |
| POST | `/api/productos` | 201; el servidor asigna el id (c05, p03, m03k…); peso obligatorio salvo cursos |
| PUT | `/api/productos/{id}` | No cambia si está activo |
| PATCH | `/api/productos/{id}/activo` | Activar / desactivar (no borra) |
| DELETE | `/api/productos/{id}` | 204 (la app usa desactivar) |
| POST | `/api/productos/disponibilidad` | Solo revisa: `SIN_STOCK`, `INACTIVO`, `NO_EXISTE` |
| POST | `/api/productos/descontar-stock` | Todo o nada (`@Transactional`); 409 si no alcanza |

## Cómo lo explico en la defensa

> "El controller solo recibe y responde. Por ejemplo `crear` recibe `@Valid @RequestBody ProductoRequest`; si el precio es 0, `@Min(1)` hace que responda 400 con `campos.precio`. La regla de que un producto físico necesita peso depende de la categoría, por eso está en el service y lanza `DatoInvalidoException`, que el `GlobalExceptionHandler` transforma en 400."

## Práctica (30 min)

1. Levanta ms-productos (`cd microservicios/ms-productos` y `./mvnw spring-boot:run`) y abre `http://localhost:8083/swagger-ui.html`.
2. Desde Swagger crea un material sin `pesoKg` y mira el 400. Luego créalo con peso y mira el id `m03k`.
3. Ejercicio de modificación en vivo: agrega `@GetMapping("/buscar")` que reciba `@RequestParam String texto` y devuelva `repositorio.findByNombreContainingIgnoreCaseOrderByIdAsc(texto)`.

## Preguntas del quiz de esta clase (sección 05)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**041. ¿Qué anotación se utiliza para declarar un controlador REST en Spring Boot?**  
✅ A) `@RestController` — `@RestController` combina semántica de controlador y serialización del cuerpo de respuesta.

**042. ¿Qué realiza `@GetMapping("/productos")` en un controlador?**  
✅ A) Asocia solicitudes GET a `/productos` con el método — `@GetMapping` mapea una ruta HTTP GET a un método del controlador.

**043. ¿Cuál es la responsabilidad típica de la capa Service?**  
✅ D) Implementar reglas y operaciones de negocio — Service coordina lógica de negocio y operaciones sobre repositorios u otros servicios.

**044. ¿Por qué el Controller no debería concentrar consultas y reglas complejas de negocio?**  
✅ A) Porque dificulta mantenimiento, reutilización y pruebas — Separar Controller y Service mantiene las responsabilidades acotadas.

**045. ¿Qué hace `@RequestBody ProductoDto dto` en un método POST de Spring?**  
✅ C) Deserializa el cuerpo de la solicitud al objeto del parámetro — `@RequestBody` vincula el contenido enviado por el cliente al parámetro Java.

**046. ¿Qué permite `@PathVariable Long id` en `@GetMapping("/{id}")`?**  
✅ C) Leer el identificador incluido en la ruta — `@PathVariable` captura parámetros de la ruta HTTP.

**047. ¿Qué devuelve conceptualmente `ResponseEntity.status(201).body(objeto)`?**  
✅ A) Una respuesta con estado 201 y cuerpo `objeto` — `ResponseEntity` permite controlar explícitamente código HTTP, cabeceras y cuerpo.

**048. ¿Qué realiza `@Service` en una clase detectada por component scanning?**  
✅ A) Registra la clase como bean administrado por Spring — `@Service` especializa `@Component` para componentes de lógica de negocio.

**049. ¿Qué aporta la inyección por constructor de `ProductoRepository` en un Service?**  
✅ D) Declara explícitamente la dependencia y favorece pruebas y diseño desacoplado — Spring puede resolver la dependencia al crear el bean.

**050. ¿Cuál es la ventaja de usar DTO en el contrato de una API?**  
✅ D) Definir los datos que se intercambian sin exponer directamente toda la entidad persistente — Los DTO separan el modelo externo de los detalles internos de persistencia y negocio.

---

[← Clase 4](clase-04.md) · [Índice](README.md) · [Clase 6 →](clase-06.md)
