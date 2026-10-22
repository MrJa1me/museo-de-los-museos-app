package com.example.museo_de_los_museos_app.ui.screens

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryNotificationRepository
import com.example.museo_de_los_museos_app.repository.InMemoryOrderRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.viewmodel.historial.HistorialViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {

    private val pedidos = InMemoryOrderRepository() // trae sol_101 (u03) y sol_102 (u04), ambos Pendiente
    private val notificaciones = InMemoryNotificationRepository()
    private lateinit var viewModel: HistorialViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun iniciarComo(rol: Rol, indice: Int = 0) {
        SessionManager.iniciarSesion(SeedData.usuarios.filter { it.rol == rol }[indice])
        viewModel = HistorialViewModel(pedidos, notificaciones, cargarUsuarios = { SeedData.usuarios })
    }

    @Test
    fun cliente_soloVeSusSolicitudes() {
        iniciarComo(Rol.CLIENTE) // Cliente Uno = u03
        assertEquals(listOf("sol_101"), viewModel.uiState.value.pedidos.map { it.id })
    }

    @Test
    fun administrador_veTodasYAvanzaEstadoConAviso() {
        iniciarComo(Rol.ADMINISTRADOR)
        assertEquals(2, viewModel.uiState.value.pedidos.size)

        viewModel.avanzarEstado("sol_101")

        val actualizado = viewModel.uiState.value.pedidos.first { it.id == "sol_101" }
        assertEquals(EstadoPedido.CONFIRMADO, actualizado.estado)
        assertTrue(notificaciones.obtenerPorUsuario("u03").any { it.id.startsWith("estado_sol_101") })
    }

    @Test
    fun cliente_noPuedeCambiarEstados() {
        iniciarComo(Rol.CLIENTE)
        viewModel.avanzarEstado("sol_101")
        assertEquals(EstadoPedido.PENDIENTE, pedidos.pedidos.value.first { it.id == "sol_101" }.estado)
    }

    @Test
    fun filtroPorEstado() {
        iniciarComo(Rol.ADMINISTRADOR)
        viewModel.avanzarEstado("sol_102")
        viewModel.onFiltroChange(EstadoPedido.CONFIRMADO)
        assertEquals(listOf("sol_102"), viewModel.uiState.value.pedidos.map { it.id })
    }
}
