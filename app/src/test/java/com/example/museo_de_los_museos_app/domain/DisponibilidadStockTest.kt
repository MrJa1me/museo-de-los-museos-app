package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.ItemStock
import com.example.museo_de_los_museos_app.model.MotivoStock
import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisponibilidadStockTest {

    private val productos = SeedData.productos.map { if (it.id == "a01") it.copy(activo = false) else it }

    @Test
    fun conStock_noHayProblemas() {
        assertTrue(DisponibilidadStock.revisar(listOf(ItemStock("p01", 25)), productos).isEmpty())
    }

    @Test
    fun detectaSinStockInactivoYNoExiste() {
        val problemas = DisponibilidadStock.revisar(
            listOf(ItemStock("a02", 1), ItemStock("a01", 1), ItemStock("x99", 1)), productos
        )
        assertEquals(listOf(MotivoStock.SIN_STOCK, MotivoStock.INACTIVO, MotivoStock.NO_EXISTE), problemas.map { it.motivo })
        assertEquals("Taza Museo de los Museos", problemas.first().nombre)
    }

    @Test
    fun sumaLineasRepetidas() {
        // p01 tiene 25: 20 + 10 = 30 no alcanza
        val problema = DisponibilidadStock.revisar(listOf(ItemStock("p01", 20), ItemStock("p01", 10)), productos).single()
        assertEquals(30, problema.solicitado)
        assertEquals(25, problema.disponible)
    }
}
