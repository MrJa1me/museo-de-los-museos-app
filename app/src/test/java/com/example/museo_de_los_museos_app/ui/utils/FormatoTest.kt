package com.example.museo_de_los_museos_app.ui.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatoTest {

    @Test
    fun formateaPrecioConPuntoDeMiles() {
        assertEquals("$0", formatearPrecio(0))
        assertEquals("$900", formatearPrecio(900))
        assertEquals("$7.000", formatearPrecio(7000))
        assertEquals("$45.000", formatearPrecio(45000))
        assertEquals("$1.250.000", formatearPrecio(1250000))
    }
}
