# Clase 7: Microservicios, Eureka y API Gateway

**Objetivo:** explicar por qué el backend son **tres microservicios** y responder las preguntas de teoría (Eureka y Gateway aparecen en el quiz aunque no los usamos).

## Lo esencial

- **Monolito**: todo en una aplicación que se despliega junta. **Microservicios**: varias aplicaciones pequeñas, cada una con su responsabilidad y su base de datos, que se despliegan por separado.
- **Una base de datos por microservicio**: cada uno es dueño de sus datos. ms-pedidos no lee la tabla de productos; si necesita algo, se lo pide por HTTP.
- **Eureka Server**: registro donde los servicios se anotan y se buscan por nombre (descubrimiento).
- **API Gateway**: puerta de entrada única que enruta (`Path=/api/productos/**` → ms-productos) y puede aplicar seguridad. `lb://PRODUCTOS-SERVICE` resuelve el servicio por nombre con balanceo.
- **Timeout**: tiempo máximo de espera. En la app es de 6 segundos (`ClienteHttp`): si el microservicio no responde, se usa la copia de Room.
- **503** al pasar por un Gateway: el servicio destino no está disponible; revisar que esté encendido, registrado y que la ruta exista.

## Nuestro backend

| Microservicio | Puerto | Base de datos | Responsabilidad |
| --- | --- | --- | --- |
| ms-museos | 8081 | `data/museos` | Fichas de museos y aprobación |
| ms-pedidos | 8082 | `data/pedidos` | Solicitudes de compra y sus estados |
| **ms-productos** | **8083** | `data/productos` | **Catálogo, activación y stock** |

**¿Por qué no usamos Eureka ni Gateway?** Son tres servicios con direcciones fijas en `BuildConfig`, así que un registro no aporta en esta etapa. Si el proyecto creciera (más instancias, despliegue en la nube), se agregaría un Gateway delante de los tres y la app solo conocería una URL.

## Cómo se coordinan al confirmar el carrito

1. **ms-productos** `POST /api/productos/disponibilidad`: ¿hay stock real?
2. **ms-pedidos** `POST /api/pedidos`: guarda la solicitud (recalcula el total).
3. **ms-productos** `POST /api/productos/descontar-stock`: descuenta (todo o nada).

Si el paso 1 falla (servidor apagado o sin stock), el carrito no se toca. Lo coordina `CarritoViewModel.confirmarPedido()`.

## Práctica (15 min)

1. Explica en voz alta qué pasaría si ms-pedidos leyera directamente la tabla de productos (acoplamiento: un cambio en la tabla rompe dos servicios).
2. Apaga solo ms-productos y confirma un carrito: ¿qué mensaje aparece y por qué no se creó la solicitud?

## Preguntas del quiz de esta clase (sección 07)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**061. ¿Cuál es el propósito principal de Eureka Server en una arquitectura Spring Cloud?**  
✅ C) Registrar y descubrir instancias de servicios — Eureka ofrece un registro de servicios consultable por clientes de descubrimiento.

**062. ¿Qué hace un cliente Eureka configurado correctamente?**  
✅ A) Puede registrar su instancia y descubrir otros servicios a través del registro — El cliente interactúa con el registro para anuncios y descubrimiento.

**063. ¿Qué función cumple un API Gateway?**  
✅ C) Proveer una entrada común que enrute solicitudes hacia servicios internos — El gateway concentra rutas y puede aplicar controles transversales.

**064. ¿Qué ventaja tiene enrutar a `lb://PRODUCTOS-SERVICE` con integración adecuada de Spring Cloud?**  
✅ D) Permite resolver instancias por nombre lógico mediante balanceo — El esquema `lb://` permite resolver el identificador de servicio usando un balanceador configurado.

**065. ¿Qué problema produce una URL de backend codificada con `localhost` en un teléfono físico?**  
✅ B) `localhost` apunta al propio teléfono, no al PC servidor — El teléfono necesita una dirección alcanzable del servidor, como su IP LAN o dominio.

**066. ¿Qué diferencia hay entre monolito y microservicios?**  
✅ A) Los microservicios separan capacidades en servicios desplegables independientemente; el monolito concentra el despliegue — La separación de despliegue cambia límites y costos operativos de la arquitectura.

**067. ¿Qué representa un timeout al llamar un microservicio remoto?**  
✅ B) La solicitud excedió un límite de espera sin completar a tiempo — Los timeouts protegen a clientes frente a esperas indefinidas por respuestas remotas.

**068. ¿Por qué se recomienda no compartir indiscriminadamente una única base de datos entre microservicios?**  
✅ C) Aumenta el acoplamiento y dificulta la autonomía de cambios — La propiedad de datos por servicio favorece límites claros y evolución independiente.

**069. ¿Para qué sirve una ruta de Gateway que usa un predicado `Path=/api/productos/**`?**  
✅ D) Seleccionar peticiones cuya ruta coincide con el patrón configurado — El predicado de ruta filtra las solicitudes que ese enrutamiento atenderá.

**070. ¿Qué se debe comprobar primero cuando la app recibe 503 al pasar por un Gateway?**  
✅ A) Que el servicio destino esté disponible y sea descubrible o alcanzable según la ruta — 503 suele indicar indisponibilidad; se diagnostican servicio, registro, rutas y conectividad.

---

[← Clase 6](clase-06.md) · [Índice](README.md) · [Clase 8 →](clase-08.md)
