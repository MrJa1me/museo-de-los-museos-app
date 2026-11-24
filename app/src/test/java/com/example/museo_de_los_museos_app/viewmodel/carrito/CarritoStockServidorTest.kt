package com.example.museo_de_los_museos_app.viewmodel.carrito

import com.example.museo_de_los_museos_app.fakes.ProductosConFallo
import com.example.museo_de_los_museos_app.model.ItemStock
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.repository.InMemoryCartRepository
import com.example.museo_de_los_museos_app.repository.InMemoryOrderRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
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

/**
 * Confirmar el carrito valida la disponibilidad con ms-productos antes de crear la solicitud en ms-pedidos
 * (pauta: "Debe validar la disponibilidad con el backend antes de confirmar una solicitud").
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CarritoStockServidorTest {

    private val base = InMemoryProductRepository()
    private val productos = ProductosConFallo(base)
    private val carrito = InMemoryCartRepository()
    private val pedidos = InMemoryOrderRepository(emptyList())
    private val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }
    private lateinit var viewModel: CarritoViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SessionManager.iniciarSesion(cliente)
        carrito.agregar(cliente.id, "m02k", 2) // stock 15
        viewModel = CarritoViewModel(productos, carrito, pedidos)
        viewModel.onNombreChange("Ana Pérez")
        viewModel.onDireccionChange("Av. Siempre Viva 742")
        viewModel.onCiudadChange("Concepción")
        viewModel.onTelefonoChange("+56 9 1234 5678")
        viewModel.solicitarConfirmacion()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun todoDisponible_creaPedido_yDescuentaStockEnElServidor() = runTest {
        viewModel.confirmarPedido()

        assertEquals(1, pedidos.pedidos.value.size)
        assertEquals(13, base.obtenerPorId("m02k")?.stock)
        assertTrue(carrito.observar(cliente.id).first().isEmpty())
    }

    @Test
    fun otroClienteComproAntes_noCreaPedido_yAvisaQueProductoNoAlcanza() = runTest {
        // En el servidor ya solo queda 1 unidad (el teléfono todavía cree que hay 15)
        base.descontarStock(listOf(ItemStock("m02k", 14)))

        viewModel.confirmarPedido()

        assertTrue(pedidos.pedidos.value.isEmpty())
        assertEquals("Caja de conservación archivística", viewModel.uiState.value.productoSinStock)
        assertEquals(1, productos.sincronizaciones) // trae el stock real para mostrarlo
        assertFalse(carrito.observar(cliente.id).first().isEmpty())
        assertFalse(viewModel.uiState.value.confirmando)
    }

    @Test
    fun msProductosApagado_noCreaPedido_niVaciaElCarrito() = runTest {
        productos.falla = ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)

        viewModel.confirmarPedido()

        assertEquals(TipoErrorRemoto.SIN_CONEXION, viewModel.uiState.value.errorRemoto?.tipo)
        assertTrue(pedidos.pedidos.value.isEmpty())
        assertNull(viewModel.uiState.value.pedidoConfirmado)
        assertFalse(carrito.observar(cliente.id).first().isEmpty())
    }
}
