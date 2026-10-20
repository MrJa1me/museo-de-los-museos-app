package com.example.museo_de_los_museos_app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorFichaMuseoTest {

    @Test
    fun nombre_obligatorioYLargoMinimo() {
        assertEquals(ErrorFicha.OBLIGATORIO, ValidadorFichaMuseo.nombre("  "))
        assertEquals(ErrorFicha.TEXTO_CORTO, ValidadorFichaMuseo.nombre("Mus"))
        assertNull(ValidadorFichaMuseo.nombre("Museo Virtual de los Andes"))
    }

    @Test
    fun lugar_soloLetras() {
        assertNull(ValidadorFichaMuseo.lugar("São Paulo"))
        assertNull(ValidadorFichaMuseo.lugar("Ciudad de México"))
        assertEquals(ErrorFicha.SOLO_LETRAS, ValidadorFichaMuseo.lugar("Lima 123"))
    }

    @Test
    fun coordenadas_aceptanComaYValidanRango() {
        assertNull(ValidadorFichaMuseo.latitud("-33,4489"))
        assertNull(ValidadorFichaMuseo.longitud("-70.6693"))
        assertEquals(ErrorFicha.NUMERO_INVALIDO, ValidadorFichaMuseo.latitud("norte"))
        assertEquals(ErrorFicha.LATITUD_FUERA_DE_RANGO, ValidadorFichaMuseo.latitud("91"))
        assertEquals(ErrorFicha.LONGITUD_FUERA_DE_RANGO, ValidadorFichaMuseo.longitud("-180.5"))
        assertEquals(ErrorFicha.NUMERO_INVALIDO, ValidadorFichaMuseo.longitud("NaN"))
    }

    @Test
    fun descripcion_largoMinimo() {
        assertEquals(ErrorFicha.DESCRIPCION_CORTA, ValidadorFichaMuseo.descripcion("Muy corta"))
        assertNull(ValidadorFichaMuseo.descripcion("Recorrido digital por las tradiciones."))
    }

    @Test
    fun enlace_soloHttpOHttps() {
        assertNull(ValidadorFichaMuseo.enlace("https://ejemplo.museo.test/m11"))
        assertEquals(ErrorFicha.ENLACE_INVALIDO, ValidadorFichaMuseo.enlace("ejemplo.museo.test"))
        assertEquals(ErrorFicha.ENLACE_INVALIDO, ValidadorFichaMuseo.enlace("ftp://ejemplo.museo.test"))
    }

    @Test
    fun siguienteId_continuaLaNumeracion() {
        assertEquals("m11", ValidadorFichaMuseo.siguienteId(listOf("m01", "m10", "m02")))
        assertEquals("m01", ValidadorFichaMuseo.siguienteId(emptyList()))
    }
}
