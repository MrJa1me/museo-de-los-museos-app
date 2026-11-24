package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.ItemStock
import com.example.museo_de_los_museos_app.model.MotivoStock
import com.example.museo_de_los_museos_app.remote.dto.ProblemaStockDto
import com.example.museo_de_los_museos_app.remote.dto.ProductoDto
import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Conversión JSON ↔ modelo del catálogo: valores seguros cuando el servidor manda campos nulos. */
class MapeadoresProductoTest {

    @Test
    fun ida_y_vuelta_conservaTodosLosCampos() {
        SeedData.productos.forEach { producto ->
            assertEquals(producto, producto.aDto().aModelo())
        }
    }

    @Test
    fun aDto_sinId_paraQueElServidorLoAsigne() {
        assertNull(SeedData.productos.first().aDto(incluirId = false).id)
    }

    @Test
    fun camposNulos_seCompletanConValoresSeguros() {
        val producto = ProductoDto(id = "a09", categoria = "articulo_museologico").aModelo()!!
        assertEquals("", producto.nombre)
        assertEquals(0, producto.precio)
        assertEquals(0, producto.stock)
        assertTrue(producto.activo)
        assertEquals(CategoriaProducto.ARTICULO_MUSEOLOGICO, producto.categoria)
    }

    @Test
    fun sinId_oCategoriaDesconocida_seDescarta() {
        assertNull(ProductoDto(id = " ", categoria = "CURSO").aModelo())
        assertNull(ProductoDto(id = "z01", categoria = "JUGUETE").aModelo())
    }

    @Test
    fun curso_nuncaTienePeso_yStockNegativoQuedaEnCero() {
        val curso = ProductoDto(id = "c09", categoria = "CURSO", pesoKg = 2.0, stock = -3).aModelo()!!
        assertNull(curso.pesoKg)
        assertEquals(0, curso.stock)
    }

    @Test
    fun itemsDeStock_yProblemas() {
        val dto = listOf(ItemStock("p01", 2)).aDto()
        assertEquals("p01", dto.items.single().productoId)
        assertEquals(2, dto.items.single().cantidad)

        assertEquals(MotivoStock.INACTIVO, ProblemaStockDto("a01", motivo = "inactivo").aModelo().motivo)
        val desconocido = ProblemaStockDto().aModelo()
        assertEquals(MotivoStock.SIN_STOCK, desconocido.motivo)
        assertEquals("", desconocido.productoId)
    }
}
