package com.example.museo_de_los_museos_app.viewmodel.catalogo

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CatalogoViewModelTest {

    private val productos = InMemoryProductRepository()
    private lateinit var viewModel: CatalogoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = CatalogoViewModel(productos)
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    @Test
    fun muestraTodoElCatalogoActivo() {
        assertEquals(SeedData.productos.size, viewModel.uiState.value.productos.size)
    }

    @Test
    fun busqueda_porNombreODescripcion_sinImportarMayusculas() {
        viewModel.onBusquedaChange("  MUSEOLOGÍA ")
        assertEquals(listOf("c01"), viewModel.uiState.value.productos.map { it.id })
    }

    @Test
    fun categoria_seAplicaYSeQuitaAlRepetir() {
        viewModel.onCategoriaSeleccionada(CategoriaProducto.CURSO)
        assertTrue(viewModel.uiState.value.productos.all { it.categoria == CategoriaProducto.CURSO })
        viewModel.onCategoriaSeleccionada(CategoriaProducto.CURSO)
        assertNull(viewModel.uiState.value.categoriaSeleccionada)
    }

    @Test
    fun soloDisponibles_ocultaLosAgotados_yLimpiarFiltrosRestaura() {
        viewModel.onSoloDisponiblesChange(true)
        assertFalse(viewModel.uiState.value.productos.any { it.id == "a02" })
        viewModel.limpiarFiltros()
        assertEquals(SeedData.productos.size, viewModel.uiState.value.productos.size)
    }

    @Test
    fun productoDesactivado_soloLoVeElAdministrador() = runTest {
        productos.cambiarActivo(SeedData.productos.first().id, activo = false)
        assertEquals(SeedData.productos.size - 1, viewModel.uiState.value.productos.size)

        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR })
        assertTrue(viewModel.uiState.value.esAdministrador)
        assertEquals(SeedData.productos.size, viewModel.uiState.value.productos.size)
    }
}
