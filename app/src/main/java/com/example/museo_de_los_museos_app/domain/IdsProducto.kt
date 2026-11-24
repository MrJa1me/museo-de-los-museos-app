package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.CategoriaProducto

/**
 * Formato de los id del catálogo: c01 (curso), p01 (publicación), m01k (material) y a01 (artículo).
 * Es la misma regla que ProductoService.siguienteId en ms-productos: en la app real el id lo asigna
 * el servidor; aquí se usa en el repositorio en memoria de las pruebas.
 */
object IdsProducto {

    fun prefijo(categoria: CategoriaProducto): String = when (categoria) {
        CategoriaProducto.CURSO -> "c"
        CategoriaProducto.PUBLICACION -> "p"
        CategoriaProducto.MATERIAL_CONSERVACION -> "m"
        CategoriaProducto.ARTICULO_MUSEOLOGICO -> "a"
    }

    fun sufijo(categoria: CategoriaProducto): String =
        if (categoria == CategoriaProducto.MATERIAL_CONSERVACION) "k" else ""

    /** c01…c04 → "c05"; m01k, m02k → "m03k". Ignora los id de otras categorías o con otro formato. */
    fun siguienteId(categoria: CategoriaProducto, existentes: List<String>): String {
        val formato = Regex("^${prefijo(categoria)}(\\d+)${sufijo(categoria)}$")
        val mayor = existentes.mapNotNull { formato.matchEntire(it)?.groupValues?.get(1)?.toIntOrNull() }.maxOrNull() ?: 0
        return prefijo(categoria) + "%02d".format(mayor + 1) + sufijo(categoria)
    }
}
