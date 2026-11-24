package com.example.museo_de_los_museos_app.viewmodel.formularioproducto

import com.example.museo_de_los_museos_app.fakes.ProductosConFallo
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** Alta y edición de productos cuando ms-productos rechaza o no responde. */
@OptIn(ExperimentalCoroutinesApi::class)
class FormularioProductoRemotoTest {

    private val productos = ProductosConFallo()
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

    private fun llenarPublicacion() {
        viewModel.cargar(null)
        viewModel.onCategoriaChange(CategoriaProducto.PUBLICACION)
        viewModel.onNombreChange("Manual de Archivos")
        viewModel.onPrecioChange("15000")
        viewModel.onStockChange("8")
        viewModel.onPesoChange("0,6")
    }

    @Test
    fun servidorRechaza_muestraSuMensaje_yNoVuelveAtras() {
        productos.falla = ErrorRemoto(TipoErrorRemoto.RECHAZADO, "El nombre debe tener entre 3 y 80 caracteres", 400)
        llenarPublicacion()

        viewModel.guardar()

        val estado = viewModel.uiState.value
        assertEquals("El nombre debe tener entre 3 y 80 caracteres", estado.errorRemoto?.mensajeServidor)
        assertFalse(estado.guardado)
        assertFalse(estado.guardando)
        assertEquals(SeedData.productos.size, productos.productos.value.size)

        viewModel.onErrorMostrado()
        assertNull(viewModel.uiState.value.errorRemoto)
    }

    @Test
    fun servidorVuelve_alReintentar_seGuardaConIdDelServidor() {
        productos.falla = ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)
        llenarPublicacion()
        viewModel.guardar()

        productos.falla = null
        viewModel.guardar()

        assertEquals("p03", productos.productos.value.single { it.nombre == "Manual de Archivos" }.id)
        assertEquals(0.6, productos.obtenerPorId("p03")?.pesoKg)
    }
}
