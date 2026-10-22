package com.example.museo_de_los_museos_app.viewmodel

import com.example.museo_de_los_museos_app.domain.ErrorValidacion
import com.example.museo_de_los_museos_app.domain.ModalidadEnvio
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryCartRepository
import com.example.museo_de_los_museos_app.repository.InMemoryOrderRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.viewmodel.carrito.CarritoViewModel
import com.example.museo_de_los_museos_app.viewmodel.carrito.TipoEnvio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarritoViewModelTest {

    private val productos = InMemoryProductRepository()
    private val carrito = InMemoryCartRepository()
    private val pedidos = InMemoryOrderRepository()
    private val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }
    private lateinit var viewModel: CarritoViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SessionManager.iniciarSesion(cliente)
        carrito.agregar(cliente.id, "m02k", 1) // 1,5 kg · $24.000
        viewModel = CarritoViewModel(productos, carrito, pedidos)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun llenarFormulario() {
        viewModel.onNombreChange("Ana Pérez")
        viewModel.onDireccionChange("Av. Siempre Viva 742")
        viewModel.onCiudadChange("Concepción")
        viewModel.onTelefonoChange("+56 9 1234 5678")
    }

    @Test
    fun calculaEnvio_segunRegionYModalidad() {
        // Región Metropolitana, Estándar: 3.500 + 1 kg adicional = 4.500
        assertEquals(4500, viewModel.uiState.value.resultado?.costoEnvio)

        viewModel.onRegionChange("Biobío")
        viewModel.onModalidadChange(ModalidadEnvio.EXPRESS)
        // 4.500 × 1,3 × 1,6 = 9.360
        assertEquals(9360, viewModel.uiState.value.resultado?.costoEnvio)
        assertEquals(24000 + 9360, viewModel.uiState.value.resultado?.total)
    }

    @Test
    fun internacional_sinPais_noCalculaHastaElegirlo() {
        viewModel.onTipoEnvioChange(TipoEnvio.INTERNACIONAL)
        assertNull(viewModel.uiState.value.resultado)

        viewModel.onPaisChange("España") // zona 2: 25.000 + 6.000
        assertEquals(31000, viewModel.uiState.value.resultado?.costoEnvio)
    }

    @Test
    fun confirmar_formularioIncompleto_muestraErroresPorCampo() {
        viewModel.solicitarConfirmacion()

        val estado = viewModel.uiState.value
        assertEquals(ErrorValidacion.CAMPO_VACIO, estado.errores.nombre)
        assertEquals(ErrorValidacion.CAMPO_VACIO, estado.errores.telefono)
        assertFalse(estado.mostrandoConfirmacion)
    }

    @Test
    fun confirmar_formularioValido_creaPedidoYVaciaCarrito() = runTest {
        llenarFormulario()
        viewModel.solicitarConfirmacion()
        assertTrue(viewModel.uiState.value.mostrandoConfirmacion)

        viewModel.confirmarPedido()

        val pedido = pedidos.obtenerPorUsuario(cliente.id).first { it.id == viewModel.uiState.value.pedidoConfirmado }
        assertEquals(28500, pedido.total)
        assertTrue(carrito.observar(cliente.id).first().isEmpty())
        assertEquals(14, productos.obtenerPorId("m02k")?.stock) // tenía 15
    }
}
