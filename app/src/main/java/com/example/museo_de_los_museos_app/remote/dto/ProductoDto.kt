package com.example.museo_de_los_museos_app.remote.dto

/**
 * JSON de ms-productos. Igual que MuseoDto, todo es anulable porque Gson no respeta los valores
 * por defecto de Kotlin; MapeadoresProducto.kt completa lo que falte con un valor seguro.
 */
data class ProductoDto(
    val id: String? = null,
    val nombre: String? = null,
    val categoria: String? = null,
    val precio: Int? = null,
    val stock: Int? = null,
    val pesoKg: Double? = null,
    val descripcion: String? = null,
    val activo: Boolean? = null
)

/** Cuerpo de PATCH /api/productos/{id}/activo. */
data class CambioActivoDto(val activo: Boolean)

/** Cuerpo de POST /api/productos/disponibilidad y POST /api/productos/descontar-stock. */
data class StockDto(val items: List<ItemStockDto>)

data class ItemStockDto(val productoId: String, val cantidad: Int)

/** Respuesta de POST /api/productos/disponibilidad. */
data class DisponibilidadDto(
    val disponible: Boolean? = null,
    val problemas: List<ProblemaStockDto>? = null
)

data class ProblemaStockDto(
    val productoId: String? = null,
    val nombre: String? = null,
    val solicitado: Int? = null,
    val disponible: Int? = null,
    val motivo: String? = null
)
