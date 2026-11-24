package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.remote.dto.ProductoDto

/**
 * Conversión entre el JSON de ms-productos y los modelos de la app (mismo criterio que Mapeadores.kt):
 * la UI y Room nunca dependen de la forma del JSON.
 */

/** Devuelve null si falta el id o la categoría no se reconoce (no se puede mostrar ni guardar en Room). */
fun ProductoDto.aModelo(): Product? {
    val id = id?.takeIf { it.isNotBlank() } ?: return null
    val categoria = CategoriaProducto.entries.firstOrNull { it.name.equals(categoria, ignoreCase = true) } ?: return null
    return Product(
        id = id,
        nombre = nombre.orEmpty(),
        categoria = categoria,
        precio = precio ?: 0,
        stock = (stock ?: 0).coerceAtLeast(0),
        // Un curso nunca tiene peso aunque el servidor mande uno
        pesoKg = if (categoria == CategoriaProducto.CURSO) null else pesoKg,
        descripcion = descripcion.orEmpty(),
        activo = activo ?: true
    )
}

/** Al crear se manda id = null y el servidor asigna el siguiente de la categoría (c05, p03, m03k...). */
fun Product.aDto(incluirId: Boolean = true) = ProductoDto(
    id = if (incluirId) id else null,
    nombre = nombre,
    categoria = categoria.name,
    precio = precio,
    stock = stock,
    pesoKg = pesoKg,
    descripcion = descripcion,
    activo = activo
)
