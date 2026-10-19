package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadoraEnvioTest {

    private fun linea(id: String, cantidad: Int) =
        LineaCarrito(SeedData.productos.first { it.id == id }, cantidad)

    private val santiago = Destino.Nacional(RegionesChile.METROPOLITANA)
    private val valparaiso = Destino.Nacional("Valparaíso")

    @Test
    fun soloCursos_noTieneEnvio() {
        val r = CalculadoraEnvio.calcular(listOf(linea("c01", 1)), santiago, ModalidadEnvio.EXPRESS)
        assertFalse(r.requiereEnvio)
        assertEquals(0, r.costoEnvio)
        assertEquals(45000, r.total)
    }

    @Test
    fun nacional_hastaUnKilo_cobraTarifaBase() {
        // p01 pesa 0,5 kg → 3.500
        val r = CalculadoraEnvio.calcular(listOf(linea("p01", 1)), santiago, ModalidadEnvio.ESTANDAR)
        assertEquals(3500, r.costoEnvio)
        assertEquals(12000 + 3500, r.total)
    }

    @Test
    fun nacional_kiloAdicionalSeRedondeaHaciaArriba() {
        // p02 × 3 = 1,2 kg → 1 kg adicional → 3.500 + 1.000
        val r = CalculadoraEnvio.calcular(listOf(linea("p02", 3)), santiago, ModalidadEnvio.ESTANDAR)
        assertEquals(1.2, r.pesoTotalKg, 0.0001)
        assertEquals(4500, r.costoEnvio)
    }

    @Test
    fun nacional_otraRegion_multiplicaPorUnoComaTres() {
        // m02k = 1,5 kg → 4.500 × 1,3 = 5.850
        val r = CalculadoraEnvio.calcular(listOf(linea("m02k", 1)), valparaiso, ModalidadEnvio.ESTANDAR)
        assertEquals(5850, r.costoEnvio)
    }

    @Test
    fun express_multiplicaPorUnoComaSeis() {
        // 4.500 × 1,3 × 1,6 = 9.360
        val r = CalculadoraEnvio.calcular(listOf(linea("m02k", 1)), valparaiso, ModalidadEnvio.EXPRESS)
        assertEquals(9360, r.costoEnvio)
    }

    @Test
    fun nacional_subtotalDesde60000_envioGratis() {
        // c04 (60.000) + a01 (8.000) = 68.000 → envío gratis aunque sea Express
        val r = CalculadoraEnvio.calcular(listOf(linea("c04", 1), linea("a01", 1)), valparaiso, ModalidadEnvio.EXPRESS)
        assertTrue(r.envioGratis)
        assertEquals(0, r.costoEnvio)
        assertEquals(68000, r.total)
    }

    @Test
    fun internacional_noTieneEnvioGratis() {
        // 68.000 a Argentina (zona 1), 0,2 kg → 15.000
        val r = CalculadoraEnvio.calcular(
            listOf(linea("c04", 1), linea("a01", 1)),
            Destino.Internacional(ZonaInternacional.SUDAMERICA),
            ModalidadEnvio.ESTANDAR
        )
        assertFalse(r.envioGratis)
        assertEquals(15000, r.costoEnvio)
    }

    @Test
    fun internacional_zona2_conKilosAdicionales() {
        // m02k × 2 = 3 kg → 2 kg adicionales → 25.000 + 2 × 6.000 = 37.000
        val r = CalculadoraEnvio.calcular(
            listOf(linea("m02k", 2)),
            Destino.Internacional(ZonaInternacional.ZONA_2),
            ModalidadEnvio.ESTANDAR
        )
        assertEquals(37000, r.costoEnvio)
        assertEquals(48000 + 37000, r.total)
    }

    @Test
    fun kgAdicionales_bordes() {
        assertEquals(0, CalculadoraEnvio.kgAdicionales(1000))
        assertEquals(1, CalculadoraEnvio.kgAdicionales(1001))
        assertEquals(1, CalculadoraEnvio.kgAdicionales(2000))
        assertEquals(2, CalculadoraEnvio.kgAdicionales(2001))
    }

    @Test
    fun zonaDesdePais() {
        assertEquals(ZonaInternacional.SUDAMERICA, ZonaInternacional.desdePais("Perú"))
        assertEquals(ZonaInternacional.ZONA_2, ZonaInternacional.desdePais("españa"))
        assertEquals(null, ZonaInternacional.desdePais("Chile"))
    }
}
