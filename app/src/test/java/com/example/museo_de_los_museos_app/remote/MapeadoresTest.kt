package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.remote.dto.MuseoDto
import com.example.museo_de_los_museos_app.remote.dto.ObraDto
import com.example.museo_de_los_museos_app.remote.dto.PedidoDto
import com.example.museo_de_los_museos_app.remote.dto.PedidoItemDto
import com.example.museo_de_los_museos_app.repository.PEDIDOS_SEMILLA
import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** Conversión JSON (DTO) ↔ modelo: los datos incompletos del servidor no deben botar la app. */
class MapeadoresTest {

    @Test
    fun museo_idaYVuelta_conservaTodosLosDatos() {
        val museo = SeedData.museos.first()
        assertEquals(museo, museo.aDto().aModelo())
    }

    @Test
    fun museo_alCrearNoSeEnviaElId() {
        assertNull(SeedData.museos.first().aDto(incluirId = false).id)
    }

    @Test
    fun museo_sinId_seDescarta() {
        assertNull(MuseoDto(id = " ", nombre = "X").aModelo())
        assertNull(MuseoDto(nombre = "X").aModelo())
    }

    @Test
    fun museo_camposFaltantes_usanValoresSeguros() {
        val museo = MuseoDto(id = "m20", clasificacion = "desconocida").aModelo()!!
        assertEquals("", museo.nombre)
        assertEquals(0.0, museo.latitud, 0.0)
        assertEquals(ClasificacionMuseo.COMUNIDAD, museo.clasificacion)
        assertEquals(Museum.PROYECTO_POR_DEFECTO, museo.proyecto)
        assertFalse(museo.publicado)
    }

    @Test
    fun museo_clasificacionSinImportarMayusculas() {
        assertEquals(ClasificacionMuseo.ARTE, MuseoDto(id = "m1", clasificacion = "arte").aModelo()!!.clasificacion)
    }

    @Test
    fun pedido_idaYVuelta_conservaTodosLosDatos() {
        val pedido = PEDIDOS_SEMILLA.first()
        assertEquals(pedido, pedido.aDto().aModelo())
    }

    @Test
    fun pedido_sinIdOUsuario_seDescarta() {
        assertNull(PedidoDto(userId = "u03").aModelo())
        assertNull(PedidoDto(id = "sol_1").aModelo())
    }

    @Test
    fun pedido_incompleto_calculaTotalYDescartaItemsSinProducto() {
        val pedido = PedidoDto(
            id = "sol_9", userId = "u03", subtotal = 1000, costoEnvio = 500, estado = "enviado",
            items = listOf(PedidoItemDto(productoId = "p01"), PedidoItemDto(productoId = null))
        ).aModelo()!!
        assertEquals(1500, pedido.total)
        assertEquals(EstadoPedido.ENVIADO, pedido.estado)
        assertEquals(1, pedido.items.size)
        assertEquals(1, pedido.items.first().cantidad)
    }

    @Test
    fun obra_armaLaUrlDeImagenConElServidorIiif() {
        val obra = ObraDto(id = 7, title = "Machu Picchu", artista = " Georgia O'Keeffe ", imagenId = "abc").aModelo("https://iiif.test")!!
        assertEquals("https://iiif.test/abc/full/400,/0/default.jpg", obra.urlImagen)
        assertEquals("Georgia O'Keeffe", obra.artista)
    }

    @Test
    fun obra_sinServidorIiif_usaElPorDefecto_ySinImagenQuedaNull() {
        assertEquals("$IIIF_POR_DEFECTO/x/full/400,/0/default.jpg", ObraDto(id = 1, title = "A", imagenId = "x").aModelo(null)!!.urlImagen)
        assertNull(ObraDto(id = 1, title = "A").aModelo(null)!!.urlImagen)
    }

    @Test
    fun obra_sinIdOTitulo_seDescarta() {
        assertNull(ObraDto(title = "A").aModelo(null))
        assertNull(ObraDto(id = 1, title = "").aModelo(null))
    }
}
