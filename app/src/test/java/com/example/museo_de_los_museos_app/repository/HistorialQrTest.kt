package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.QrHistory
import org.junit.Assert.assertEquals
import org.junit.Test

class HistorialQrTest {

    private fun escaneo(contenido: String, fecha: Long) =
        QrHistory(userId = "u03", contenido = contenido, valido = true, descripcion = "", fechaMillis = fecha)

    @Test
    fun mismoCodigo_noSeDuplicaYSubeAlInicio() {
        val historial = listOf(escaneo("A", 2), escaneo("B", 1))
        val nuevo = historial.agregarSinDuplicados(escaneo("B", 3))
        assertEquals(listOf("B", "A"), nuevo.map { it.contenido })
        assertEquals(3L, nuevo.first().fechaMillis)
    }

    @Test
    fun historial_seLimitaAlMaximo() {
        val historial = (1..5).map { escaneo("c$it", it.toLong()) }
        assertEquals(3, historial.agregarSinDuplicados(escaneo("nuevo", 9), maximo = 3).size)
    }

    @Test
    fun json_idaYVuelta() {
        val historial = listOf(escaneo("{\"tipo\":\"museo\",\"id\":\"m01\"}", 10), escaneo("https://ejemplo.museo.test", 5))
        assertEquals(historial, historialDesdeJson(historial.aJsonHistorial()))
    }

    @Test
    fun jsonDanado_devuelveListaVacia() {
        assertEquals(emptyList<QrHistory>(), historialDesdeJson("no es json"))
        assertEquals(emptyList<QrHistory>(), historialDesdeJson(null))
    }
}
