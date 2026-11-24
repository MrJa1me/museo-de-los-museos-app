package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.ObrasArteApi
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.remote.dto.ConfigDto
import com.example.museo_de_los_museos_app.remote.dto.ObraDto
import com.example.museo_de_los_museos_app.remote.dto.ObrasRespuestaDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

/** API externa simulada con MockK: no se llama a internet durante las pruebas. */
class ObrasArteRepositoryTest {

    private val api = mockk<ObrasArteApi>()
    private val repositorio = ObrasArteRepositoryRemoto(api)

    @Test
    fun buscar_convierteLasObras_yDescartaLasIncompletas() = runTest {
        coEvery { api.buscar("Peru", any(), any(), any()) } returns ObrasRespuestaDto(
            data = listOf(ObraDto(id = 1, title = "Machu Picchu", imagenId = "abc"), ObraDto(id = null, title = "Sin id")),
            config = ConfigDto("https://iiif.test")
        )

        val obras = repositorio.buscar("  Peru ").getOrThrow()

        assertEquals(1, obras.size)
        assertEquals("https://iiif.test/abc/full/400,/0/default.jpg", obras.first().urlImagen)
        coVerify { api.buscar("Peru", 20, ObrasArteApi.CAMPOS, ObrasArteApi.AGENTE) }
    }

    @Test
    fun buscar_respuestaVacia_devuelveListaVacia() = runTest {
        coEvery { api.buscar(any(), any(), any(), any()) } returns ObrasRespuestaDto()
        assertEquals(emptyList<Any>(), repositorio.buscar("x").getOrThrow())
    }

    @Test
    fun buscar_sinInternet_esErrorSinConexion() = runTest {
        coEvery { api.buscar(any(), any(), any(), any()) } throws IOException()
        assertEquals(TipoErrorRemoto.SIN_CONEXION, (repositorio.buscar("x").exceptionOrNull() as ErrorRemoto).tipo)
    }
}
