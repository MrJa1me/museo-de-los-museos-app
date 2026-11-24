package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.PedidosApi
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.remote.aDto
import com.example.museo_de_los_museos_app.remote.dto.CambioEstadoDto
import com.example.museo_de_los_museos_app.remote.dto.PedidoDto
import com.example.museo_de_los_museos_app.repository.db.PedidoDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.itemsEntidad
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

/** Repositorio de pedidos: misma idea que MuseumRepositoryRemotoTest (Retrofit y DAO simulados con MockK). */
@OptIn(ExperimentalCoroutinesApi::class)
class OrderRepositoryRemotoTest {

    private val api = mockk<PedidosApi>()
    private val dao = mockk<PedidoDao>(relaxed = true)
    private val pedido = PEDIDOS_SEMILLA.first()

    private fun TestScope.repositorio(sincronizarAlIniciar: Boolean = false): OrderRepositoryRemoto {
        every { dao.observarPedidos() } returns flowOf(PEDIDOS_SEMILLA.map { it.aEntidad() })
        every { dao.observarItems() } returns flowOf(PEDIDOS_SEMILLA.flatMap { it.itemsEntidad() })
        return OrderRepositoryRemoto(api, dao, CoroutineScope(UnconfinedTestDispatcher(testScheduler)), sincronizarAlIniciar)
    }

    @Test
    fun pedidos_unenCadaPedidoConSusItems_yFiltran() = runTest {
        val repo = repositorio()
        assertEquals(PEDIDOS_SEMILLA, repo.pedidos.value)
        assertEquals(listOf(pedido), repo.obtenerPorUsuario(pedido.userId))
        assertEquals(PEDIDOS_SEMILLA.size, repo.obtenerPendientes().size)
    }

    @Test
    fun sincronizar_reemplazaPedidosEItems() = runTest {
        coEvery { api.listar() } returns listOf(pedido.aDto(), PedidoDto(id = "sin_usuario"))
        val repo = repositorio()

        assertTrue(repo.sincronizar().isSuccess)

        coVerify { dao.reemplazarTodos(listOf(pedido.aEntidad()), pedido.itemsEntidad()) }
        assertEquals(EstadoConexion.CONECTADO, repo.conexion.value)
    }

    @Test
    fun sincronizar_sinRed_avisa() = runTest {
        coEvery { api.listar() } throws IOException()
        val repo = repositorio()
        assertTrue(repo.sincronizar().isFailure)
        assertEquals(EstadoConexion.SIN_CONEXION, repo.conexion.value)
    }

    @Test
    fun alIniciar_conTablaVacia_cargaSemilla() = runTest {
        coEvery { dao.contar() } returns 0
        coEvery { api.listar() } throws IOException()
        repositorio(sincronizarAlIniciar = true)
        coVerify { dao.insertarConItems(match { it.size == PEDIDOS_SEMILLA.size }, any()) }
    }

    @Test
    fun crear_guardaLaRespuestaConSusItems() = runTest {
        coEvery { api.crear(any()) } returns pedido.aDto()
        assertEquals(pedido, repositorio().crear(pedido).getOrThrow())
        coVerify { dao.guardarConItems(pedido.aEntidad(), pedido.itemsEntidad()) }
    }

    @Test
    fun cambiarEstado_enviaElNombreDelEstado() = runTest {
        val confirmado = pedido.copy(estado = EstadoPedido.CONFIRMADO)
        coEvery { api.cambiarEstado(pedido.id, CambioEstadoDto("CONFIRMADO")) } returns confirmado.aDto()
        assertEquals(EstadoPedido.CONFIRMADO, repositorio().cambiarEstado(pedido.id, EstadoPedido.CONFIRMADO).getOrThrow().estado)
    }

    @Test
    fun cambiarEstado_saltandoPasos_elServidorRechaza() = runTest {
        val cuerpo = """{"mensaje":"No se puede pasar de PENDIENTE a ENTREGADO"}""".toResponseBody("application/json".toMediaType())
        coEvery { api.cambiarEstado(any(), any()) } throws HttpException(Response.error<Any>(409, cuerpo))
        val error = repositorio().cambiarEstado(pedido.id, EstadoPedido.ENTREGADO).exceptionOrNull() as ErrorRemoto
        assertEquals(TipoErrorRemoto.RECHAZADO, error.tipo)
        assertEquals("No se puede pasar de PENDIENTE a ENTREGADO", error.mensajeServidor)
    }

    @Test
    fun crear_respuestaInvalida_esError() = runTest {
        coEvery { api.crear(any()) } returns PedidoDto()
        assertTrue(repositorio().crear(pedido).isFailure)
    }

    @Test
    fun eliminar_okY404_borranLocal_y409No() = runTest {
        coEvery { api.eliminar("a") } returns Response.success(204, Unit)
        coEvery { api.eliminar("b") } returns Response.error(404, "".toResponseBody(null))
        coEvery { api.eliminar("c") } returns Response.error(409, "".toResponseBody(null))
        val repo = repositorio()

        assertTrue(repo.eliminar("a").isSuccess)
        assertTrue(repo.eliminar("b").isSuccess)
        assertTrue(repo.eliminar("c").isFailure)

        coVerify { dao.eliminar("a") }
        coVerify { dao.eliminar("b") }
        coVerify(exactly = 0) { dao.eliminar("c") }
    }
}
