# Clase 6: Base de datos, JPA y persistencia

**Objetivo:** mostrar dónde se guardan los datos (paso 5) y explicar las anotaciones JPA.

## Lo esencial

- `@Entity` + `@Table(name = "productos")`: la clase es una tabla.
- `@Id`: clave primaria. En nuestro caso es un `String` (`c01`, `p01`...) que asigna el **service**, no la base de datos (por eso no usamos `@GeneratedValue`).
- `@Column(nullable = false, length = 80)`: restricciones de la columna. `@Enumerated(EnumType.STRING)` guarda la categoría como texto.
- `JpaRepository<Producto, String>` ya trae `save`, `findById` (devuelve `Optional`), `findAll`, `existsById`, `deleteById`, `count`.
- **Consultas por nombre de método**: `findByCategoriaAndActivoOrderByIdAsc(categoria, activo)` → Spring genera el SQL.
- `@Transactional`: todo o nada. En `descontarStock`, si una línea no alcanza no se descuenta ninguna.
- `spring.jpa.hibernate.ddl-auto=update`: crea/actualiza las tablas solas.
- **H2 en archivo** (`./data/productos`) por defecto; **MySQL** con el perfil `mysql` sin cambiar código.
- **Room** en la app es la copia local (SQLite): `ProductoDao.reemplazarTodos` (`@Transaction`) deja la tabla igual al servidor.

## Ver la base de datos

- Consola H2: `http://localhost:8083/h2-console` con JDBC URL `jdbc:h2:file:./data/productos`, usuario `sa`, sin contraseña.
- `SELECT * FROM PRODUCTOS WHERE PRECIO > 10000;`
- Con MySQL (XAMPP/Laragon): `./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql` → base `museo_productos`.

## Flujo de una escritura (para seguirlo con el profesor)

1. `FormularioProductoScreen` → Guardar → `viewModel.guardar()`.
2. El ViewModel valida con `Validadores`; con errores no llama al servidor.
3. `ProductRepositoryRemoto.crear()` → `api.crear(producto.aDto(incluirId = false))` → `POST /api/productos`.
4. Servidor: `ProductoController` → `@Valid ProductoRequest` → `ProductoService.crear` (id + activo) → `save()` en H2.
5. La respuesta vuelve y se guarda en Room con `dao.guardar(...)`.
6. El catálogo observa Room (Flow), así que el producto aparece solo.

## Práctica (20 min)

1. Crea un producto desde la app y búscalo en la consola H2.
2. Apaga ms-productos e intenta guardar: aparece el Snackbar y no se guarda nada.

## Preguntas del quiz de esta clase (sección 06)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**051. ¿Qué indica `@Entity` en una clase Java usada con JPA?**  
✅ D) Que representa una entidad gestionada por JPA — JPA administra instancias de la entidad y su asociación con datos persistidos.

**052. ¿Qué identifica `@Id` dentro de una entidad JPA?**  
✅ C) La propiedad que representa la clave primaria — `@Id` marca el identificador persistente de la entidad.

**053. ¿Qué hace `@GeneratedValue(strategy = GenerationType.IDENTITY)` habitualmente?**  
✅ B) Delega la generación del identificador al mecanismo identity de la base de datos — `IDENTITY` suele utilizar una columna autogenerada por la base de datos.

**054. ¿Qué aporta `JpaRepository<Producto, Long>`?**  
✅ D) Operaciones estándar de persistencia sobre Producto con ID Long — Spring Data JPA provee operaciones como `save`, `findById`, `findAll` y `deleteById`.

**055. ¿Qué devuelve `repository.findById(7L)` en Spring Data JPA?**  
✅ C) Un `Optional<Producto>` — `findById` expresa la posible ausencia del registro mediante `Optional`.

**056. ¿Qué efecto tiene `repository.save(producto)` en Spring Data JPA?**  
✅ D) Persiste una entidad nueva o actualiza su estado según su condición de entidad — `save` delega en mecanismos JPA de persistencia o merge según la entidad.

**057. ¿Qué representa `@OneToMany` en JPA?**  
✅ A) Una relación de una entidad con una colección de entidades relacionadas — La anotación modela asociaciones de cardinalidad uno a muchos.

**058. ¿Qué significa que una operación sea transaccional con `@Transactional`?**  
✅ B) Sus operaciones participan en una transacción gestionada por Spring — Una transacción delimita consistencia de cambios y reglas de commit/rollback.

**059. ¿Por qué una clave foránea en SQL resulta útil?**  
✅ B) Mantiene integridad referencial entre registros relacionados — Las restricciones FK impiden referencias inconsistentes conforme a su definición.

**060. ¿Qué consulta SQL obtiene todos los productos cuyo precio supera 1000?**  
✅ A) `SELECT * FROM productos WHERE precio > 1000;` — `WHERE` filtra filas de acuerdo con la condición `precio > 1000`.

---

[← Clase 5](clase-05.md) · [Índice](README.md) · [Clase 7 →](clase-07.md)
