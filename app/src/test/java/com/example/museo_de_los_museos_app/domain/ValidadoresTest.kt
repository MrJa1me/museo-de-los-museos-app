package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresTest {

    private fun producto(id: String) = SeedData.productos.first { it.id == id }

    @Test
    fun cantidad() {
        val p01 = producto("p01") // stock 25
        assertNull(Validadores.validarCantidad("2", p01))
        assertEquals(ErrorValidacion.CAMPO_VACIO, Validadores.validarCantidad(" ", p01))
        assertEquals(ErrorValidacion.NUMERO_INVALIDO, Validadores.validarCantidad("dos", p01))
        assertEquals(ErrorValidacion.CANTIDAD_MINIMA, Validadores.validarCantidad("0", p01))
        assertEquals(ErrorValidacion.SUPERA_STOCK, Validadores.validarCantidad("26", p01))
        assertEquals(ErrorValidacion.SUPERA_STOCK, Validadores.validarCantidad("5", p01, enCarrito = 21))
    }

    @Test
    fun cantidad_productoAgotado() {
        // a02 está agotado a propósito en el seed
        assertEquals(ErrorValidacion.PRODUCTO_AGOTADO, Validadores.validarCantidad("1", producto("a02")))
    }

    @Test
    fun destino() {
        assertNull(Validadores.validarNombreDestinatario("María José Pérez"))
        assertEquals(ErrorValidacion.TEXTO_MUY_CORTO, Validadores.validarNombreDestinatario("Al"))
        assertEquals(ErrorValidacion.SOLO_LETRAS, Validadores.validarNombreDestinatario("Ana123"))
        assertEquals(ErrorValidacion.TEXTO_MUY_CORTO, Validadores.validarDireccion("Av 1"))
        assertNull(Validadores.validarDireccion("Av. Siempre Viva 742"))
        assertNull(Validadores.validarCiudad("São Paulo"))
        assertNull(Validadores.validarTelefono("+56 9 1234 5678"))
        assertEquals(ErrorValidacion.TELEFONO_INVALIDO, Validadores.validarTelefono("12-34"))
        assertNull(Validadores.validarRegion("Biobío"))
        assertEquals(ErrorValidacion.REGION_INVALIDA, Validadores.validarRegion("Mendoza"))
        assertNull(Validadores.validarPais("Portugal"))
        assertEquals(ErrorValidacion.PAIS_NO_DISPONIBLE, Validadores.validarPais("Japón"))
    }

    @Test
    fun formularioProducto() {
        assertNull(Validadores.validarPrecio("12000"))
        assertEquals(ErrorValidacion.PRECIO_INVALIDO, Validadores.validarPrecio("0"))
        assertEquals(ErrorValidacion.NUMERO_INVALIDO, Validadores.validarPrecio("12.000,5"))
        assertNull(Validadores.validarStock("0"))
        assertEquals(ErrorValidacion.STOCK_INVALIDO, Validadores.validarStock("-1"))
        assertNull(Validadores.validarPeso("0,5", CategoriaProducto.PUBLICACION))
        assertNull(Validadores.validarPeso("", CategoriaProducto.CURSO))
        assertEquals(ErrorValidacion.CAMPO_VACIO, Validadores.validarPeso("", CategoriaProducto.PUBLICACION))
        assertEquals(ErrorValidacion.PESO_INVALIDO, Validadores.validarPeso("0", CategoriaProducto.PUBLICACION))
        assertEquals(ErrorValidacion.PESO_MAXIMO, Validadores.validarPeso("31", CategoriaProducto.PUBLICACION))
        assertEquals(ErrorValidacion.NUMERO_INVALIDO, Validadores.validarPeso("NaN", CategoriaProducto.PUBLICACION))
    }
}
