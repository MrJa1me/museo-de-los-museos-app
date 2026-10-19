package com.example.museo_de_los_museos_app

import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryMuseumRepository
import com.example.museo_de_los_museos_app.repository.InMemoryOrderRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.ui.screens.dashboard.DashboardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var museumRepository: InMemoryMuseumRepository
    private lateinit var productRepository: InMemoryProductRepository
    private lateinit var orderRepository: InMemoryOrderRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        museumRepository = InMemoryMuseumRepository()
        productRepository = InMemoryProductRepository()
        orderRepository = InMemoryOrderRepository()
        viewModel = DashboardViewModel(
            museumRepository = museumRepository,
            productRepository = productRepository,
            orderRepository = orderRepository,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dashboard_rolAdministrador_exponeMetricasCorrectas() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        val admin = SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR }
        SessionManager.iniciarSesion(admin)

        val state = viewModel.uiState.value

        assertEquals(Rol.ADMINISTRADOR, state.rol)
        assertEquals(admin, state.usuario)
        assertEquals(orderRepository.obtenerPendientes().size, state.solicitudesPendientesCount)
        assertFalse(state.cargando)
        collectJob.cancel()
    }

    @Test
    fun dashboard_rolInvestigador_exponeTotalMuseosCorrectos() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        val investigador = SeedData.usuarios.first { it.rol == Rol.INVESTIGADOR }
        SessionManager.iniciarSesion(investigador)

        val state = viewModel.uiState.value

        assertEquals(Rol.INVESTIGADOR, state.rol)
        assertEquals(SeedData.museos.size, state.totalMuseos)
        collectJob.cancel()
    }

    @Test
    fun dashboard_rolCliente_exponeContenidoDestacado() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }
        SessionManager.iniciarSesion(cliente)

        val state = viewModel.uiState.value

        assertEquals(Rol.CLIENTE, state.rol)
        assertFalse(state.museosDestacados.isEmpty())
        assertFalse(state.cursosDestacados.isEmpty())
        assertFalse(state.productosDestacados.isEmpty())
        collectJob.cancel()
    }

    @Test
    fun dashboard_sinSesion_exponeRolClientePorDefecto() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        SessionManager.cerrarSesion()

        val state = viewModel.uiState.value

        assertEquals(Rol.CLIENTE, state.rol)
        collectJob.cancel()
    }
}
