package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Test

/** Misma regla de ids que ProductoService.siguienteId en ms-productos. */
class IdsProductoTest {

    private val ids = SeedData.productos.map { it.id }

    @Test
    fun siguienteId_porCategoria() {
        assertEquals("c05", IdsProducto.siguienteId(CategoriaProducto.CURSO, ids))
        assertEquals("p03", IdsProducto.siguienteId(CategoriaProducto.PUBLICACION, ids))
        assertEquals("m03k", IdsProducto.siguienteId(CategoriaProducto.MATERIAL_CONSERVACION, ids))
        assertEquals("a03", IdsProducto.siguienteId(CategoriaProducto.ARTICULO_MUSEOLOGICO, ids))
    }

    @Test
    fun sinProductos_empiezaEnUno_eIgnoraOtrosFormatos() {
        assertEquals("c01", IdsProducto.siguienteId(CategoriaProducto.CURSO, emptyList()))
        assertEquals("c01", IdsProducto.siguienteId(CategoriaProducto.CURSO, listOf("c1700000000000x", "curso")))
    }
}
