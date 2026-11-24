# Clase 8: Integración CRUD y análisis de fallos

**Objetivo:** demostrar en vivo insertar, actualizar y eliminar desde la app (paso 5c) y saber diagnosticar errores.

## CRUD del catálogo desde la app (rol Administrador: admin@museo.test / Admin123)

| Operación | En la app | Endpoint | Dónde está el código |
| --- | --- | --- | --- |
| **Leer** | Catálogo → botón Actualizar | `GET /api/productos` | `CatalogoViewModel.actualizar` → `ProductRepositoryRemoto.sincronizar` |
| **Crear** | Catálogo → «Agregar producto» | `POST /api/productos` → 201 | `FormularioProductoViewModel.guardar` → `crear` |
| **Actualizar** | Detalle → Editar | `PUT /api/productos/{id}` | `FormularioProductoViewModel.guardar` → `actualizar` |
| **Desactivar** (borrado lógico) | Detalle → Desactivar | `PATCH /api/productos/{id}/activo` | `DetalleProductoViewModel.alternarActivo` |
| **Stock** (Cliente) | Carrito → Confirmar | `POST .../disponibilidad` y `.../descontar-stock` | `CarritoViewModel.confirmarPedido` |

Los otros CRUD del equipo: fichas de museo con ms-museos (crear, editar, aprobar, eliminar) y solicitudes con ms-pedidos (crear, avanzar estado, cancelar).

## Reglas de diseño que debes saber justificar

- **Primero el servidor, después Room.** La app nunca muestra algo que el servidor rechazó.
- **Validar en los dos lados.** La app valida para ayudar al usuario (`Validadores`); el servidor vuelve a validar (`@Valid`) porque no se puede confiar en el cliente.
- **Errores recuperables.** Sin conexión: aviso «Sin conexión» + Reintentar y se muestra la copia local. Rechazo (400/409): se muestra el mensaje del servidor en un Snackbar.
- **Desactivar en vez de borrar**: los pedidos antiguos siguen nombrando el producto.

## Diagnóstico rápido

| Síntoma | Causa probable | Qué revisar |
| --- | --- | --- |
| Aviso «Sin conexión» | Microservicio apagado o IP incorrecta | Consola de Spring; `http://localhost:8083/api/productos`; en teléfono, `msHost` |
| 400 al guardar | Dato inválido | Mensaje del Snackbar (`campos` del JSON) y anotaciones del DTO |
| 409 al confirmar | Stock insuficiente o id repetido | `ProductoService.descontarStock` / `crear` |
| Datos antiguos | No se volvió a sincronizar | Botón Actualizar; `sincronizar()` |
| `CLEARTEXT not permitted` | HTTP bloqueado | `network_security_config.xml` |

## Práctica (30 min)

1. Haz el CRUD completo de un producto y después revísalo en la consola H2.
2. Como Cliente, agrega 2 cajas (m02k). En Swagger usa `descontar-stock` para dejar 1 unidad. Confirma el carrito: debe aparecer «No hay stock suficiente de "Caja de conservación archivística"».

## Preguntas del quiz de esta clase (sección 08)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**071. ¿Qué método HTTP se utiliza habitualmente para crear un recurso en una API REST?**  
✅ B) POST — POST envía datos que el servidor puede procesar para crear un recurso.

**072. ¿Qué método HTTP se utiliza habitualmente para reemplazar la representación de un recurso identificado?**  
✅ D) PUT — PUT expresa una actualización o reemplazo en la URI destino, según el contrato de la API.

**073. ¿Qué método HTTP se usa habitualmente para eliminar un recurso?**  
✅ B) DELETE — DELETE solicita eliminar el recurso identificado.

**074. ¿Qué operación HTTP debería usarse, en general, para consultar un recurso sin modificarlo?**  
✅ A) GET — GET es un método seguro en su semántica HTTP: se utiliza para recuperar información.

**075. Si una aplicación agrega un producto y el backend devuelve 201, ¿cuál es una buena acción de UI?**  
✅ A) Actualizar el estado o volver a consultar la lista y confirmar éxito — Después de una creación correcta, la UI debe reflejar el recurso persistido.

**076. ¿Qué se debe verificar si la aplicación muestra datos antiguos después de actualizar en el backend?**  
✅ B) Si se emite un nuevo estado y se refresca la fuente de datos o caché — El problema puede estar en caché, ausencia de nueva consulta o estado que no se actualiza.

**077. ¿Qué consecuencia suele tener enviar un JSON con un tipo incompatible con el DTO esperado?**  
✅ B) Puede fallar la deserialización y generarse un error de solicitud — El cuerpo debe ajustarse al contrato esperado por el backend.

**078. ¿Cuál es la forma adecuada de mostrar un error de conexión en Compose?**  
✅ B) Exponer un estado de error desde la lógica y presentar un mensaje con opción de reintento — La UI debe reflejar errores recuperables sin perder capacidad de interacción.

**079. ¿Por qué conviene validar datos tanto en la app como en el backend?**  
✅ B) La app mejora la experiencia, mientras el backend protege las reglas ante cualquier cliente — La validación del cliente no es confiable como única barrera de negocio.

**080. ¿Qué indica una violación de restricción UNIQUE al guardar un registro?**  
✅ A) Se intenta introducir una combinación de valores que debe ser única y ya existe — UNIQUE exige que las claves definidas no se dupliquen según la semántica de la base.

---

[← Clase 7](clase-07.md) · [Índice](README.md) · [Clase 9 →](clase-09.md)
