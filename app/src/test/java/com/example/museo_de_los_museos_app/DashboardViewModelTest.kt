package com.example.museo_de_los_museos_app

import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryMuseumRepository
import com.example.museo_de_los_museos_app.repository.InMemoryOrderRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.ui.screens.dashboard.DashboardViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class DashboardViewModelTest {

    private lateinit var museumRepository: InMemoryMuseumRepository
    private lateinit var productRepository: InMemoryProductRepository
    private lateinit var orderRepository: InMemoryOrderRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        museumRepository = InMemoryMuseumRepository()
        productRepository = InMemoryProductRepository()
        orderRepository = InMemoryOrderRepository()
        viewModel = DashboardViewModel(
            museumRepository = museumRepository,
            productRepository = productRepository,
            orderRepository = orderRepository,
        )
    }

    @Test
    fun dashboard_rolAdministrador_exponeMetricasCorrectas() {
        val admin = SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR }
        SessionManager.iniciarSesion(admin)

        val state = viewModel.uiState.value

        assertEquals(Rol.ADMINISTRADOR, state.rol)
        assertEquals(admin, state.usuario)
        assertEquals(orderRepository.obtenerPendientes().size, state.solicitudesPendientesCount)
        assertFalse(state.cargando)
    }

    @Test
    fun dashboard_rolInvestigador_exponeTotalMuseosCorrectos() {
        val investigador = SeedData.usuarios.first { it.rol == Rol.INVESTIGADOR }
        SessionManager.iniciarSesion(investigador)

        val state = viewModel.uiState.value

        assertEquals(Rol.INVESTIGADOR, state.rol)
        assertEquals(SeedData.museos.size, state.totalMuseos)
    }

    @Test
    fun dashboard_rolCliente_exponeContenidoDestacado() {
        val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }
        SessionManager.iniciarSesion(cliente)

        val state = viewModel.uiState.value

        assertEquals(Rol.CLIENTE, state.rol)
        assertFalse(state.museosDestacados.isEmpty())
        assertFalse(state.cursosDestacados.isEmpty())
        assertFalse(state.productosDestacados.isEmpty())
    }

    @Test
    fun dashboard_sinSesion_exponeRolClientePorDefecto() {
        SessionManager.cerrarSesion()

        val state = viewModel.uiState.value

        assertEquals(Rol.CLIENTE, state.rol)
    }
}
