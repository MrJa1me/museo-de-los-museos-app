package com.example.museo_de_los_museos_app.viewmodel.escanerqr

import com.example.museo_de_los_museos_app.domain.LectorQr
import com.example.museo_de_los_museos_app.domain.MotivoQrInvalido
import com.example.museo_de_los_museos_app.domain.ResultadoQr
import com.example.museo_de_los_museos_app.fakes.FakeQrHistoryRepository
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryMuseumRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.viewmodel.resultadoqr.ResultadoQrViewModel
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
class EscanerQrViewModelTest {

    private val historial = FakeQrHistoryRepository()
    private val pendiente = SeedData.museos.first().copy(id = "m11", publicado = false)
    private lateinit var viewModel: EscanerQrViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = EscanerQrViewModel(InMemoryMuseumRepository(SeedData.museos + pendiente), InMemoryProductRepository(), historial)
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    @Test
    fun codigoDeMuseo_vaAlResultado_yQuedaEnElHistorialDelUsuario() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.CLIENTE })
        viewModel.onCodigoLeido(LectorQr.EJEMPLO_MUSEO)

        val estado = viewModel.uiState.value
        assertTrue(estado.navegarAResultado)
        assertEquals(1, estado.historial.size)
        assertEquals("Museo Virtual de la Memoria Sur", estado.historial.first().descripcion)
        assertTrue(UltimoEscaneo.escaneo.value?.resultado is ResultadoQr.MuseoEncontrado)

        viewModel.onNavegacionRealizada()
        assertFalse(viewModel.uiState.value.navegarAResultado)
    }

    @Test
    fun cliente_noEncuentraFichasPendientes() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.CLIENTE })
        viewModel.onCodigoLeido("{\"tipo\":\"museo\",\"id\":\"m11\"}")
        assertEquals(ResultadoQr.Error(MotivoQrInvalido.NO_ENCONTRADO), UltimoEscaneo.escaneo.value?.resultado)
        assertFalse(viewModel.uiState.value.historial.first().valido)
    }

    @Test
    fun productoYEnlace_seDescribenEnElHistorial() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.CLIENTE })
        viewModel.onCodigoLeido("{\"tipo\":\"producto\",\"id\":\"p01\"}")
        viewModel.onCodigoLeido("https://ejemplo.museo.test")
        assertEquals(listOf("https://ejemplo.museo.test", "Guía de Museos Virtuales (libro)"), viewModel.uiState.value.historial.map { it.descripcion })
    }

    @Test
    fun sinSesion_noGuardaHistorial() {
        viewModel.onCodigoLeido(LectorQr.EJEMPLO_MUSEO)
        assertFalse(viewModel.uiState.value.haySesion)
        assertTrue(historial.historial.value.isEmpty())
    }

    @Test
    fun ingresoManual_vacioMarcaError_yEjemploLoCorrige() {
        viewModel.onRevisarManual()
        assertEquals(MotivoQrInvalido.VACIO, viewModel.uiState.value.errorManual)

        viewModel.onUsarEjemplo()
        assertNull(viewModel.uiState.value.errorManual)
        viewModel.onRevisarManual()
        assertTrue(viewModel.uiState.value.navegarAResultado)

        viewModel.onTextoManualChange("hola")
        assertEquals("hola", viewModel.uiState.value.textoManual)
    }

    @Test
    fun cancelarOFallarElLector_muestraMensaje() {
        viewModel.onEscaneoCancelado()
        assertEquals(MensajeEscaner.CANCELADO, viewModel.uiState.value.mensaje)
        viewModel.onEscaneoFallido()
        assertEquals(MensajeEscaner.FALLO, viewModel.uiState.value.mensaje)
        viewModel.onMensajeMostrado()
        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun tocarUnEscaneoDelHistorial_loVuelveAAbrir_yElResultadoLoMuestra() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.CLIENTE })
        viewModel.onCodigoLeido("texto cualquiera")
        viewModel.onNavegacionRealizada()
        viewModel.onHistorialSeleccionado(viewModel.uiState.value.historial.first())
        assertTrue(viewModel.uiState.value.navegarAResultado)

        val resultado = ResultadoQrViewModel().uiState.value
        assertEquals("texto cualquiera", resultado.contenido)
        assertFalse(resultado.esExito)
    }
}
