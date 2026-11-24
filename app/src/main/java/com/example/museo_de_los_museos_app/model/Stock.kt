package com.example.museo_de_los_museos_app.model

/** Una línea del carrito que se envía a ms-productos para revisar o descontar stock. */
data class ItemStock(val productoId: String, val cantidad: Int)

/** Por qué una línea del carrito no se puede comprar (mismos valores que MotivoProblema en ms-productos). */
enum class MotivoStock { NO_EXISTE, INACTIVO, SIN_STOCK }

/** Respuesta del servidor para una línea que no se puede comprar, con su stock real. */
data class ProblemaStock(
    val productoId: String,
    val nombre: String,
    val solicitado: Int,
    val disponible: Int,
    val motivo: MotivoStock
)
