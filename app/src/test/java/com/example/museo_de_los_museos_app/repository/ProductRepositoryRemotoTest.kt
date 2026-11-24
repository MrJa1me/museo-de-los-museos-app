package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.ProductosApi
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.remote.aDto
import com.example.museo_de_los_museos_app.remote.dto.ProductoDto
import com.example.museo_de_los_museos_app.repository.db.ProductoDao
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Repositorio del catálogo (ms-productos) con MockK:
 *  - ProductosApi (Retrofit) se simula para responder éxito, sin red o 400 según la prueba;
 *  - ProductoDao (Room) se simula con relaxed = true y se verifica con coVerify qué se guardó.
 * Así se prueba la lógica sin servidor, sin emulador y sin internet.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryRemotoTest {

    private val api = mockk<ProductosApi>()
    private val dao = mockk<ProductoDao>(relaxed = true)
    private val producto = SeedData.productos.first { it.id == "p01" }

    private fun TestScope.repositorio(sincronizarAlIniciar: Boolean = false): ProductRepositoryRemoto {
        every { dao.observarTodos() } returns flowOf(listOf(producto.aEntidad()))
        return ProductRepositoryRemoto(api, dao, CoroutineScope(UnconfinedTestDispatcher(testScheduler)), sincronizarAlIniciar)
    }

    private fun errorHttp(codigo: Int, json: String) =
        HttpException(Response.error<Any>(codigo, json.toResponseBody("application/json".toMediaType())))

    @Test
    fun productos_vienenDeRoom() = runTest {
        val repo = repositorio()
        assertEquals(listOf(producto), repo.productos.value)
        assertEquals(producto, repo.obtenerPorId("p01"))
        assertNull(repo.obtenerPorId("zzz"))
        assertEquals(EstadoConexion.SIN_VERIFICAR, repo.conexion.value)
    }

    @Test
    fun sincronizar_reemplazaLaCopiaLocal_yDescartaFilasInvalidas() = runTest {
        coEvery { api.listar() } returns listOf(producto.aDto(), ProductoDto(id = null), ProductoDto(id = "x1", categoria = "JUGUETE"))
        val repo = repositorio()

        assertTrue(repo.sincronizar().isSuccess)

        coVerify { dao.reemplazarTodos(listOf(producto.aEntidad())) }
        assertEquals(EstadoConexion.CONECTADO, repo.conexion.value)
    }

    @Test
    fun sincronizar_sinRed_noTocaRoom_yAvisaSinConexion() = runTest {
        coEvery { api.listar() } throws IOException("servidor apagado")
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

        coVerify { dao.insertarSiNoExisten(match { it.size == SeedData.productos.size }) }
        coVerify { dao.reemplazarTodos(emptyList()) }
    }

    @Test
    fun crear_enviaSinId_yGuardaLoQueRespondeElServidor() = runTest {
        val respuesta = producto.copy(id = "p03")
        coEvery { api.crear(any()) } returns respuesta.aDto()
        val repo = repositorio()

        val creado = repo.crear(producto.copy(id = "")).getOrThrow()

        assertEquals("p03", creado.id)
        coVerify { api.crear(match { it.id == null && it.categoria == "PUBLICACION" }) }
        coVerify { dao.guardar(respuesta.aEntidad()) }
    }

    @Test
    fun crear_respuestaSinId_esError_yNoGuarda() = runTest {
        coEvery { api.crear(any()) } returns ProductoDto(id = null)
        assertTrue(repositorio().crear(producto).isFailure)
        coVerify(exactly = 0) { dao.guardar(any()) }
    }

    @Test
    fun actualizar_rechazado400_muestraElCampo_yNoGuarda() = runTest {
        coEvery { api.actualizar("p01", any()) } throws
            errorHttp(400, """{"mensaje":"Datos inválidos","campos":{"precio":"El precio debe ser mayor que 0"}}""")
        val repo = repositorio()

        val error = repo.actualizar(producto.copy(precio = 0)).exceptionOrNull() as ErrorRemoto

        assertEquals(TipoErrorRemoto.RECHAZADO, error.tipo)
        assertEquals("El precio debe ser mayor que 0", error.mensajeServidor)
        assertEquals(EstadoConexion.CONECTADO, repo.conexion.value) // el servidor sí respondió
        coVerify(exactly = 0) { dao.guardar(any()) }
    }

    @Test
    fun cambiarActivo_usaPatch_yGuardaLaRespuesta() = runTest {
        val desactivado = producto.copy(activo = false)
        coEvery { api.cambiarActivo("p01", any()) } returns desactivado.aDto()

        val resultado = repositorio().cambiarActivo("p01", activo = false)

        assertEquals(false, resultado.getOrThrow().activo)
        coVerify { api.cambiarActivo("p01", match { !it.activo }) }
        coVerify { dao.guardar(desactivado.aEntidad()) }
    }
}
