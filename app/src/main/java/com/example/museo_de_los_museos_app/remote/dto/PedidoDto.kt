package com.example.museo_de_los_museos_app.remote.dto

/** JSON de ms-pedidos. Igual que MuseoDto, todo anulable y los mapeadores completan lo que falte. */
data class PedidoDto(
    val id: String? = null,
    val userId: String? = null,
    val items: List<PedidoItemDto>? = null,
    val subtotal: Int? = null,
    val costoEnvio: Int? = null,
    val total: Int? = null,
    val estado: String? = null,
    val destino: String? = null,
    val modalidad: String? = null,
    val fechaMillis: Long? = null
)

data class PedidoItemDto(
    val productoId: String? = null,
    val nombre: String? = null,
    val cantidad: Int? = null,
    val precioUnitario: Int? = null
)

/** Cuerpo de PATCH /api/pedidos/{id}/estado. */
data class CambioEstadoDto(val estado: String)
