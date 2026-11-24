package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.model.QrHistory
import com.example.museo_de_los_museos_app.repository.db.CarritoDao
import com.example.museo_de_los_museos_app.repository.db.CarritoEntity
import com.example.museo_de_los_museos_app.repository.db.FavoritoDao
import com.example.museo_de_los_museos_app.repository.db.HistorialQrDao
import com.example.museo_de_los_museos_app.repository.db.PreferenciasDao
import com.example.museo_de_los_museos_app.repository.db.ProductoDao
import com.example.museo_de_los_museos_app.repository.db.UsuarioDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Repositorios que solo usan Room: el DAO se simula con MockK y se verifica que se llame correctamente. */
@OptIn(ExperimentalCoroutinesApi::class)
class RepositoriosRoomTest {

    @Test
    fun usuarios_cargaLaSemillaUnaSolaVez_yBuscaPorCorreoSinEspacios() = runTest {
        val dao = mockk<UsuarioDao>(relaxed = true)
        val admin = SeedData.usuarios.first()
        coEvery { dao.obtenerTodos() } returns SeedData.usuarios.map { it.aEntidad() }
        coEvery { dao.buscarPorCorreo("admin@museo.test") } returns admin.aEntidad()
        val repo = RoomUserRepository(dao)

        assertEquals(SeedData.usuarios, repo.obtenerTodos())
        assertEquals(admin, repo.buscarPorCorreo("  admin@museo.test "))
        repo.registrar(admin.copy(id = "u99"))
        repo.actualizar(admin)

        coVerify(exactly = 1) { dao.insertarSiNoExisten(any()) }
        coVerify { dao.insertar(admin.copy(id = "u99").aEntidad()) }
        coVerify { dao.actualizar(admin.aEntidad()) }
    }

    @Test
    fun productos_cargaSemilla_yGuarda() = runTest {
        val dao = mockk<ProductoDao>(relaxed = true)
        val producto = SeedData.productos.first()
        every { dao.observarTodos() } returns flowOf(listOf(producto.aEntidad()))
        val repo = RoomProductRepository(dao, CoroutineScope(UnconfinedTestDispatcher(testScheduler)))

        assertEquals(producto, repo.obtenerPorId(producto.id))
        assertNull(repo.obtenerPorId("zzz"))
        repo.guardar(producto.copy(stock = 1))

        coVerify { dao.insertarSiNoExisten(match { it.size == SeedData.productos.size }) }
        coVerify { dao.guardar(producto.copy(stock = 1).aEntidad()) }
    }

    @Test
    fun carrito_sumaCantidades_yCantidadCeroElimina() = runTest {
        val dao = mockk<CarritoDao>(relaxed = true)
        every { dao.observar("u03") } returns flowOf(listOf(CarritoEntity("u03", "p01", 2)))
        val repo = RoomCartRepository(dao)

        assertEquals(2, repo.observar("u03").first().single().cantidad)
        repo.agregar("u03", "p01", 3)
        repo.agregar("u03", "a01", 1)
        repo.cambiarCantidad("u03", "p01", 0)
        repo.cambiarCantidad("u03", "a01", 4)
        repo.eliminar("u03", "a01")
        repo.vaciar("u03")

        coVerify { dao.guardar(CarritoEntity("u03", "p01", 5)) }
        coVerify { dao.guardar(CarritoEntity("u03", "a01", 1)) }
        coVerify { dao.guardar(CarritoEntity("u03", "a01", 4)) }
        coVerify { dao.eliminar("u03", "p01") }
        coVerify { dao.eliminar("u03", "a01") }
        coVerify { dao.vaciar("u03") }
    }

    @Test
    fun favoritos_entregaUnConjunto_yAlterna() = runTest {
        val dao = mockk<FavoritoDao>(relaxed = true)
        every { dao.observarIds("u03") } returns flowOf(listOf("m01", "m01", "m04"))
        val repo = RoomFavoriteRepository(dao)

        assertEquals(setOf("m01", "m04"), repo.observar("u03").first())
        repo.alternar("u03", "m02")
        coVerify { dao.alternar("u03", "m02") }
    }

    @Test
    fun preferencias_sinFila_devuelveValoresPorDefecto() = runTest {
        val dao = mockk<PreferenciasDao>(relaxed = true)
        coEvery { dao.obtener("u03") } returns null
        every { dao.observar("u03") } returns flowOf(null)
        val repo = RoomPreferenciasRepository(dao)

        assertEquals(Preferencias("u03"), repo.obtener("u03"))
        assertEquals(Preferencias("u03"), repo.observar("u03").first())
        val nuevas = Preferencias("u03", textoGrande = true)
        repo.guardar(nuevas)
        coVerify { dao.guardar(nuevas.aEntidad()) }
    }

    @Test
    fun historialQr_registraConElMaximo() = runTest {
        val dao = mockk<HistorialQrDao>(relaxed = true)
        every { dao.observar("u03") } returns flowOf(emptyList())
        val repo = RoomQrHistoryRepository(dao)
        val item = QrHistory("u03", "{\"tipo\":\"museo\",\"id\":\"m01\"}", true, "Museo", 1L)

        assertEquals(emptyList<QrHistory>(), repo.observar("u03").first())
        repo.registrar(item)
        coVerify { dao.registrar(item.aEntidad(), MAXIMO_HISTORIAL_QR) }
    }
}
