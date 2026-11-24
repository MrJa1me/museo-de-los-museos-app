package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.MuseosApi
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.remote.aDto
import com.example.museo_de_los_museos_app.remote.dto.MuseoDto
import com.example.museo_de_los_museos_app.repository.db.MuseoDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Repositorio de museos con MockK:
 *  - la API Retrofit (MuseosApi) se simula para responder lo que cada prueba necesita (éxito, sin red, 409...)
 *  - el DAO de Room se simula con relaxed = true y luego se verifica qué se guardó con coVerify
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MuseumRepositoryRemotoTest {

    private val api = mockk<MuseosApi>()
    private val dao = mockk<MuseoDao>(relaxed = true)
    private val museo = SeedData.museos.first()

    private fun TestScope.repositorio(sincronizarAlIniciar: Boolean = false): MuseumRepositoryRemoto {
        every { dao.observarTodos() } returns flowOf(listOf(museo.aEntidad()))
        return MuseumRepositoryRemoto(api, dao, CoroutineScope(UnconfinedTestDispatcher(testScheduler)), sincronizarAlIniciar)
    }

    @Test
    fun museos_vienenDeRoom() = runTest {
        val repo = repositorio()
        assertEquals(listOf(museo), repo.museos.value)
        assertEquals(museo, repo.obtenerPorId(museo.id))
        assertEquals(EstadoConexion.SIN_VERIFICAR, repo.conexion.value)
    }

    @Test
    fun sincronizar_reemplazaLaCopiaLocal_yDescartaFilasSinId() = runTest {
        coEvery { api.listar() } returns listOf(museo.aDto(), MuseoDto(id = null))
        val repo = repositorio()

        assertTrue(repo.sincronizar().isSuccess)

        coVerify { dao.reemplazarTodos(listOf(museo.aEntidad())) }
        assertEquals(EstadoConexion.CONECTADO, repo.conexion.value)
    }

    @Test
    fun sincronizar_sinRed_noTocaRoom_yAvisaSinConexion() = runTest {
        coEvery { api.listar() } throws IOException("sin red")
        val repo = repositorio()

        val resultado = repo.sincronizar()

        assertEquals(TipoErrorRemoto.SIN_CONEXION, (resultado.exceptionOrNull() as ErrorRemoto).tipo)
        coVerify(exactly = 0) { dao.reemplazarTodos(any()) }
        assertEquals(EstadoConexion.SIN_CONEXION, repo.conexion.value)
    }

    @Test
    fun alIniciar_conTablaVacia_cargaSemilla_yLuegoSincroniza() = runTest {
        coEvery { dao.contar() } returns 0
        coEvery { api.listar() } returns emptyList()
        repositorio(sincronizarAlIniciar = true)

        coVerify { dao.insertarSiNoExisten(match { it.size == SeedData.museos.size }) }
        coVerify { dao.reemplazarTodos(emptyList()) }
    }

    @Test
    fun crear_enviaSinId_yGuardaLoQueRespondeElServidor() = runTest {
        val respuesta = museo.copy(id = "m11", publicado = false)
        coEvery { api.crear(any()) } returns respuesta.aDto()
        val repo = repositorio()

        val creado = repo.crear(museo).getOrThrow()

        assertEquals("m11", creado.id)
        coVerify { api.crear(match { it.id == null }) }
        coVerify { dao.guardar(respuesta.aEntidad()) }
    }

    @Test
    fun crear_respuestaSinId_esError() = runTest {
        coEvery { api.crear(any()) } returns MuseoDto(id = null)
        assertTrue(repositorio().crear(museo).isFailure)
        coVerify(exactly = 0) { dao.guardar(any()) }
    }

    @Test
    fun actualizar_usaPut_conElId() = runTest {
        coEvery { api.actualizar(museo.id, any()) } returns museo.aDto()
        assertTrue(repositorio().actualizar(museo).isSuccess)
        coVerify { api.actualizar(museo.id, match { it.id == museo.id }) }
    }

    @Test
    fun aprobar_rechazado_noGuarda_peroHayConexion() = runTest {
        val cuerpo = """{"mensaje":"No existe el museo m99"}""".toResponseBody("application/json".toMediaType())
        coEvery { api.aprobar("m99") } throws HttpException(Response.error<Any>(404, cuerpo))
        val repo = repositorio()

        val error = repo.aprobar("m99").exceptionOrNull() as ErrorRemoto

        assertEquals("No existe el museo m99", error.mensajeServidor)
        assertEquals(EstadoConexion.CONECTADO, repo.conexion.value)
        coVerify(exactly = 0) { dao.guardar(any()) }
    }

    @Test
    fun eliminar_ok_borraDeRoom() = runTest {
        coEvery { api.eliminar(museo.id) } returns Response.success(204, Unit)
        assertTrue(repositorio().eliminar(museo.id).isSuccess)
        coVerify { dao.eliminar(museo.id) }
    }

    @Test
    fun eliminar_404_igualBorraLaCopiaLocal() = runTest {
        coEvery { api.eliminar("m01") } returns Response.error(404, "".toResponseBody(null))
        assertTrue(repositorio().eliminar("m01").isSuccess)
        coVerify { dao.eliminar("m01") }
    }

    @Test
    fun eliminar_errorDelServidor_noBorra() = runTest {
        coEvery { api.eliminar("m01") } returns Response.error(500, "".toResponseBody(null))
        val resultado = repositorio().eliminar("m01")
        assertEquals(TipoErrorRemoto.SERVIDOR, (resultado.exceptionOrNull() as ErrorRemoto).tipo)
        coVerify(exactly = 0) { dao.eliminar(any()) }
    }
}
