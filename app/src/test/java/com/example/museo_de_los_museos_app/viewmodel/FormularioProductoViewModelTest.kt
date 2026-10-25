package com.example.museo_de_los_museos_app.viewmodel

import com.example.museo_de_los_museos_app.domain.ErrorValidacion
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.viewmodel.formularioproducto.FormularioProductoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FormularioProductoViewModelTest {

    private val productos = InMemoryProductRepository()
    private lateinit var viewModel: FormularioProductoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR })
        viewModel = FormularioProductoViewModel(productos)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun guardar_formularioVacio_muestraErroresYNoGuarda() {
        viewModel.cargar(null)
        viewModel.onCategoriaChange(CategoriaProducto.PUBLICACION)
        viewModel.guardar()

        val errores = viewModel.uiState.value.errores
        assertEquals(ErrorValidacion.CAMPO_VACIO, errores.nombre)
        assertEquals(ErrorValidacion.CAMPO_VACIO, errores.precio)
        assertEquals(ErrorValidacion.CAMPO_VACIO, errores.stock)
        assertEquals(ErrorValidacion.CAMPO_VACIO, errores.peso)
        assertEquals(10, productos.productos.value.size)
    }

    @Test
    fun guardar_precioCero_esInvalido() {
        viewModel.cargar(null)
        viewModel.onNombreChange("Taller de Archivos")
        viewModel.onPrecioChange("0")
        viewModel.onStockChange("0") // stock 0 sí es válido
        viewModel.guardar()

        assertEquals(ErrorValidacion.PRECIO_INVALIDO, viewModel.uiState.value.errores.precio)
        assertNull(viewModel.uiState.value.errores.stock)
    }

    @Test
    fun alta_curso_creaProductoActivoSinPeso() {
        viewModel.cargar(null)
        viewModel.onNombreChange("Taller de Archivos")
        viewModel.onPrecioChange("35000")
        viewModel.onStockChange("12")
        viewModel.guardar()

        val nuevo = productos.productos.value.single { it.nombre == "Taller de Archivos" }
        assertTrue(nuevo.id.startsWith("c"))
        assertNull(nuevo.pesoKg)
        assertTrue(nuevo.activo)
        assertTrue(viewModel.uiState.value.guardado)
    }

    @Test
    fun edicion_cargaDatosYConservaIdYEstado() {
        productos.guardar(productos.obtenerPorId("p01")!!.copy(activo = false))
        viewModel.cargar("p01")
        assertEquals("Guía de Museos Virtuales (libro)", viewModel.uiState.value.nombre)
        assertEquals("0,5", viewModel.uiState.value.peso)

        viewModel.onPrecioChange("13000")
        viewModel.guardar()

        val editado = productos.obtenerPorId("p01")!!
        assertEquals(13000, editado.precio)
        assertFalse(editado.activo)
        assertEquals(10, productos.productos.value.size)
    }
}
