package com.example.museo_de_los_museos_app.viewmodel.catalogo

import com.example.museo_de_los_museos_app.fakes.ProductosConFallo
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.repository.EstadoConexion
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/** Catálogo conectado a ms-productos: botón Actualizar y aviso «Sin conexión». */
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogoRemotoTest {

    private val productos = ProductosConFallo()
    private lateinit var viewModel: CatalogoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.CLIENTE })
        viewModel = CatalogoViewModel(productos)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun actualizar_llamaAlServidor_yTerminaSinQuedarseCargando() {
        viewModel.actualizar()

        assertEquals(1, productos.sincronizaciones)
        assertFalse(viewModel.uiState.value.actualizando)
        assertEquals(EstadoConexion.CONECTADO, viewModel.uiState.value.conexion)
    }

    @Test
    fun servidorApagado_muestraAviso_peroSigueMostrandoLaCopiaLocal() {
        productos.falla = ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)

        viewModel.actualizar()

        assertEquals(EstadoConexion.SIN_CONEXION, viewModel.uiState.value.conexion)
        assertEquals(SeedData.productos.count { it.activo }, viewModel.uiState.value.productos.size)
    }
}
