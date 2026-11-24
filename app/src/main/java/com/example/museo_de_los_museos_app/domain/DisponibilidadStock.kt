package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.ItemStock
import com.example.museo_de_los_museos_app.model.MotivoStock
import com.example.museo_de_los_museos_app.model.ProblemaStock
import com.example.museo_de_los_museos_app.model.Product

/**
 * Misma revisión que hace ms-productos en POST /api/productos/disponibilidad, pero sobre una lista local.
 * La usa el repositorio en memoria (pruebas); la app real le pregunta al servidor porque el stock
 * guardado en el teléfono puede estar desactualizado.
 */
object DisponibilidadStock {

    fun revisar(items: List<ItemStock>, productos: List<Product>): List<ProblemaStock> {
        // Si el mismo producto viene dos veces, se suman las cantidades
        val porProducto = items.groupBy { it.productoId }.mapValues { (_, lineas) -> lineas.sumOf { it.cantidad } }
        return porProducto.mapNotNull { (id, solicitado) ->
            val producto = productos.firstOrNull { it.id == id }
            when {
                producto == null -> ProblemaStock(id, "", solicitado, 0, MotivoStock.NO_EXISTE)
                !producto.activo -> ProblemaStock(id, producto.nombre, solicitado, producto.stock, MotivoStock.INACTIVO)
                producto.stock < solicitado -> ProblemaStock(id, producto.nombre, solicitado, producto.stock, MotivoStock.SIN_STOCK)
                else -> null
            }
        }
    }
}
