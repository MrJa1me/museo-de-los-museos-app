package com.example.museo_de_los_museos_app

import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryNotificationRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.viewmodel.notificaciones.FiltroNotificacion
import com.example.museo_de_los_museos_app.viewmodel.notificaciones.NotificacionesViewModel
import com.example.museo_de_los_museos_app.viewmodel.notificaciones.OpcionDestinatario
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificacionesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: InMemoryNotificationRepository
    private lateinit var viewModel: NotificacionesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = InMemoryNotificationRepository()
        viewModel = NotificacionesViewModel(repository)
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.id == "u01" }) // Admin
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validacionAviso_tituloVacio_falla() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.onNuevoTituloChanged("   ")
        viewModel.onNuevoMensajeChanged("Mensaje de prueba")

        val resultado = viewModel.publicarAvisoDePrueba()

        assertFalse(resultado)
        assertNotNull(viewModel.uiState.value.errorTituloRes)
        collectJob.cancel()
    }

    @Test
    fun validacionAviso_mensajeVacio_falla() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.onNuevoTituloChanged("Título Válido")
        viewModel.onNuevoMensajeChanged("")

        val resultado = viewModel.publicarAvisoDePrueba()

        assertFalse(resultado)
        assertNotNull(viewModel.uiState.value.errorMensajeRes)
        collectJob.cancel()
    }

    @Test
    fun validacionAviso_ambosValidos_exito() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.onNuevoTituloChanged("Aviso General")
        viewModel.onNuevoMensajeChanged("Contenido del aviso para clientes")

        val resultado = viewModel.publicarAvisoDePrueba()

        assertTrue(resultado)
        collectJob.cancel()
    }

    @Test
    fun marcarComoLeida_cambiaSoloEseItem() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        val originalNoLeida = repository.obtenerPorUsuario("u03").first { !it.leida }
        viewModel.marcarComoLeida(originalNoLeida.id)

        val notifActualizada = repository.notificaciones.value.first { it.id == originalNoLeida.id }
        assertTrue(notifActualizada.leida)
        collectJob.cancel()
    }

    @Test
    fun filtroNoLeidas_devuelveSoloNoLeidas() = runTest {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.id == "u03" })
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.cambiarFiltro(FiltroNotificacion.NO_LEIDAS)

        val noLeidasRepositorio = repository.obtenerPorUsuario("u03").filter { !it.leida }
        assertEquals(noLeidasRepositorio.size, viewModel.uiState.value.lista.size)
        assertTrue(viewModel.uiState.value.lista.all { !it.leida })
        collectJob.cancel()
    }

    @Test
    fun cadaUsuario_veSoloSusAvisos() = runTest {
        val avisosCliente1 = repository.obtenerPorUsuario("u03")
        val avisosAdmin = repository.obtenerPorUsuario("u01")

        assertTrue(avisosCliente1.all { it.userId == "u03" })
        assertTrue(avisosAdmin.all { it.userId == "u01" })
    }

    @Test
    fun crearAvisoTodosClientes_generaUnAvisoPorCadaCliente() = runTest {
        val collectJob = backgroundScope.launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.onDestinatarioChanged(OpcionDestinatario.TODOS_CLIENTES)
        viewModel.onNuevoTituloChanged("Aviso Masivo")
        viewModel.onNuevoMensajeChanged("Mensaje para todos los clientes")

        val clientesCount = SeedData.usuarios.count { it.rol == Rol.CLIENTE }
        val sizeAntes = repository.notificaciones.value.size

        val resultado = viewModel.publicarAvisoDePrueba()

        assertTrue(resultado)
        val sizeDespues = repository.notificaciones.value.size
        assertEquals(sizeAntes + clientesCount, sizeDespues)
        collectJob.cancel()
    }
}
