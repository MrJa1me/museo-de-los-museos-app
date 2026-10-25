package com.example.museo_de_los_museos_app.viewmodel

import com.example.museo_de_los_museos_app.domain.ErrorValidacion
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryCartRepository
import com.example.museo_de_los_museos_app.repository.InMemoryNotificationRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.viewmodel.detalleproducto.DetalleProductoViewModel
import com.example.museo_de_los_museos_app.viewmodel.detalleproducto.MensajeDetalleProducto
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleProductoViewModelTest {

    private val carrito = InMemoryCartRepository()
    private val notificaciones = InMemoryNotificationRepository()
    private val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }
    private lateinit var viewModel: DetalleProductoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SessionManager.iniciarSesion(cliente)
        viewModel = DetalleProductoViewModel(InMemoryProductRepository(), carrito, notificaciones)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun agregarAlCarrito_cantidadValida_guardaEnElCarritoDelUsuario() = runTest {
        viewModel.cargar("p01")
        viewModel.onCantidadChange("2")
        viewModel.agregarAlCarrito()

        val items = carrito.observar(cliente.id).first()
        assertEquals(2, items.single { it.productoId == "p01" }.cantidad)
        assertEquals(MensajeDetalleProducto.AGREGADO_AL_CARRITO, viewModel.uiState.value.mensaje)
    }

    @Test
    fun agregarAlCarrito_superaStock_muestraErrorYNoGuarda() = runTest {
        viewModel.cargar("p01") // stock 25
        viewModel.onCantidadChange("30")
        viewModel.agregarAlCarrito()

        assertEquals(ErrorValidacion.SUPERA_STOCK, viewModel.uiState.value.errorCantidad)
        assertTrue(carrito.observar(cliente.id).first().isEmpty())
    }

    @Test
    fun solicitarInformacion_curso_creaAvisoParaElUsuario() {
        viewModel.cargar("c01")
        viewModel.solicitarInformacion()

        assertEquals(1, notificaciones.obtenerPorUsuario(cliente.id).count { it.id.startsWith("info_c01") })
    }

    @Test
    fun administrador_desactivaYActivaProducto() {
        val productos = InMemoryProductRepository()
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR })
        val vmAdmin = DetalleProductoViewModel(productos, carrito, notificaciones)
        vmAdmin.cargar("a01")

        vmAdmin.alternarActivo()
        assertFalse(productos.obtenerPorId("a01")!!.activo)
        assertEquals(MensajeDetalleProducto.PRODUCTO_DESACTIVADO, vmAdmin.uiState.value.mensaje)

        vmAdmin.alternarActivo()
        assertTrue(productos.obtenerPorId("a01")!!.activo)
    }

    @Test
    fun cliente_noPuedeDesactivar() {
        val productos = InMemoryProductRepository()
        val vmCliente = DetalleProductoViewModel(productos, carrito, notificaciones)
        vmCliente.cargar("a01")
        vmCliente.alternarActivo()
        assertTrue(productos.obtenerPorId("a01")!!.activo)
    }
}
