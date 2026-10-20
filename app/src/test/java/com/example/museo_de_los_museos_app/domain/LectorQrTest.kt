package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LectorQrTest {

    private fun resolver(texto: String?) = LectorQr.resolver(
        texto,
        buscarMuseo = { id -> SeedData.museos.firstOrNull { it.id == id } },
        buscarProducto = { id -> SeedData.productos.firstOrNull { it.id == id } }
    )

    @Test
    fun jsonDeMuseo_seInterpreta() {
        assertEquals(ContenidoQr.Museo("m01"), LectorQr.interpretar("{\"tipo\":\"museo\",\"id\":\"m01\"}"))
    }

    @Test
    fun jsonConMayusculasYEspacios_seNormaliza() {
        assertEquals(ContenidoQr.Producto("p01"), LectorQr.interpretar("  {\"tipo\":\"Producto\",\"id\":\" P01 \"}  "))
    }

    @Test
    fun textoVacioONulo_esVacio() {
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.VACIO), LectorQr.interpretar("   "))
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.VACIO), LectorQr.interpretar(null))
    }

    @Test
    fun jsonMalFormado_esFormatoDesconocido() {
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.FORMATO_DESCONOCIDO), LectorQr.interpretar("{\"tipo\":"))
    }

    @Test
    fun jsonConTipoDesconocidoOSinId_seRechaza() {
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.TIPO_DESCONOCIDO), LectorQr.interpretar("{\"tipo\":\"usuario\",\"id\":\"u01\"}"))
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.SIN_ID), LectorQr.interpretar("{\"tipo\":\"museo\"}"))
    }

    @Test
    fun soloSeAceptanEnlacesHttpYHttps() {
        assertEquals(ContenidoQr.Enlace("https://ejemplo.museo.test/m01"), LectorQr.interpretar("https://ejemplo.museo.test/m01"))
        assertEquals(ContenidoQr.Enlace("HTTP://ejemplo.museo.test"), LectorQr.interpretar("HTTP://ejemplo.museo.test"))
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.ENLACE_NO_PERMITIDO), LectorQr.interpretar("javascript:alert(1)"))
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.ENLACE_NO_PERMITIDO), LectorQr.interpretar("ftp://archivos.test"))
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.FORMATO_DESCONOCIDO), LectorQr.interpretar("https://"))
    }

    @Test
    fun textoCualquiera_esFormatoDesconocido() {
        assertEquals(ContenidoQr.Invalido(MotivoQrInvalido.FORMATO_DESCONOCIDO), LectorQr.interpretar("hola mundo"))
    }

    @Test
    fun resolver_buscaMuseoYProductoEnLosDatosLocales() {
        val museo = resolver(LectorQr.EJEMPLO_MUSEO)
        assertTrue(museo is ResultadoQr.MuseoEncontrado)
        assertEquals("m04", (museo as ResultadoQr.MuseoEncontrado).museo.id)

        val producto = resolver("{\"tipo\":\"producto\",\"id\":\"p01\"}")
        assertEquals("p01", (producto as ResultadoQr.ProductoEncontrado).producto.id)
    }

    @Test
    fun resolver_idInexistente_esNoEncontrado() {
        assertEquals(ResultadoQr.Error(MotivoQrInvalido.NO_ENCONTRADO), resolver("{\"tipo\":\"museo\",\"id\":\"m99\"}"))
    }
}
