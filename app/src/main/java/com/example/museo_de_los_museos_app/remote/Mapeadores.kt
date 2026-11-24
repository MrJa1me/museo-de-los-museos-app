package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.ObraArte
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.OrderItem
import com.example.museo_de_los_museos_app.remote.dto.MuseoDto
import com.example.museo_de_los_museos_app.remote.dto.ObraDto
import com.example.museo_de_los_museos_app.remote.dto.PedidoDto
import com.example.museo_de_los_museos_app.remote.dto.PedidoItemDto

/**
 * Conversión entre el JSON de los servicios (DTO) y los modelos de la app.
 * Así la UI y Room nunca dependen de la forma del JSON: si el servidor cambia, solo se toca este archivo.
 */

/** Devuelve null si al museo le falta el id (no se puede guardar en Room sin clave). */
fun MuseoDto.aModelo(): Museum? {
    val id = id?.takeIf { it.isNotBlank() } ?: return null
    return Museum(
        id = id,
        nombre = nombre.orEmpty(),
        pais = pais.orEmpty(),
        ciudad = ciudad.orEmpty(),
        latitud = latitud ?: 0.0,
        longitud = longitud ?: 0.0,
        clasificacion = clasificacion.aEnum(ClasificacionMuseo.COMUNIDAD),
        descripcion = descripcion.orEmpty(),
        enlace = enlace.orEmpty(),
        proyecto = proyecto?.takeIf { it.isNotBlank() } ?: Museum.PROYECTO_POR_DEFECTO,
        imagen = imagen.orEmpty(),
        publicado = publicado ?: false
    )
}

/** Al crear se manda id = null y el servidor asigna el siguiente (m11, m12...). */
fun Museum.aDto(incluirId: Boolean = true) = MuseoDto(
    id = if (incluirId) id else null,
    nombre = nombre, pais = pais, ciudad = ciudad,
    latitud = latitud, longitud = longitud,
    clasificacion = clasificacion.name,
    descripcion = descripcion, enlace = enlace,
    proyecto = proyecto, imagen = imagen, publicado = publicado
)

fun PedidoDto.aModelo(): Order? {
    val id = id?.takeIf { it.isNotBlank() } ?: return null
    val userId = userId?.takeIf { it.isNotBlank() } ?: return null
    return Order(
        id = id,
        userId = userId,
        items = items.orEmpty().mapNotNull { it.aModelo() },
        subtotal = subtotal ?: 0,
        costoEnvio = costoEnvio ?: 0,
        total = total ?: ((subtotal ?: 0) + (costoEnvio ?: 0)),
        estado = estado.aEnum(EstadoPedido.PENDIENTE),
        destino = destino.orEmpty(),
        modalidad = modalidad.orEmpty(),
        fechaMillis = fechaMillis ?: 0L
    )
}

private fun PedidoItemDto.aModelo(): OrderItem? {
    val productoId = productoId?.takeIf { it.isNotBlank() } ?: return null
    return OrderItem(productoId, nombre.orEmpty(), cantidad ?: 1, precioUnitario ?: 0)
}

fun Order.aDto() = PedidoDto(
    id = id, userId = userId,
    items = items.map { PedidoItemDto(it.productoId, it.nombre, it.cantidad, it.precioUnitario) },
    subtotal = subtotal, costoEnvio = costoEnvio, total = total,
    estado = estado.name, destino = destino, modalidad = modalidad, fechaMillis = fechaMillis
)

/** Las obras sin id o sin título se descartan; la imagen se arma con el servidor IIIF del museo. */
fun ObraDto.aModelo(urlIiif: String?): ObraArte? {
    val id = id ?: return null
    val titulo = title?.takeIf { it.isNotBlank() } ?: return null
    return ObraArte(
        id = id,
        titulo = titulo,
        artista = artista.orEmpty().trim(),
        fecha = fecha.orEmpty(),
        origen = lugarOrigen.orEmpty(),
        tecnica = tecnica.orEmpty(),
        urlImagen = imagenId?.takeIf { it.isNotBlank() }?.let { urlImagenObra(urlIiif, it) }
    )
}

const val IIIF_POR_DEFECTO = "https://www.artic.edu/iiif/2"

/** Formato IIIF: <servidor>/<id>/full/<ancho>,/0/default.jpg  (400 px de ancho alcanza para la lista). */
fun urlImagenObra(urlIiif: String?, imagenId: String): String =
    "${urlIiif?.takeIf { it.isNotBlank() } ?: IIIF_POR_DEFECTO}/$imagenId/full/400,/0/default.jpg"

/** "ARTE" → ClasificacionMuseo.ARTE; si el servidor manda un valor desconocido se usa [porDefecto]. */
private inline fun <reified T : Enum<T>> String?.aEnum(porDefecto: T): T =
    enumValues<T>().firstOrNull { it.name.equals(this, ignoreCase = true) } ?: porDefecto
