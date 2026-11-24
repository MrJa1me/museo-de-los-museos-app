package com.example.museo_de_los_museos_app.viewmodel.historial

import com.example.museo_de_los_museos_app.fakes.PedidosConFallo
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.repository.InMemoryNotificationRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Flujos que dependen del microservicio de pedidos: cancelar, actualizar y errores del servidor. */
@OptIn(ExperimentalCoroutinesApi::class)
class HistorialRemotoTest {

    private val pedidos = PedidosConFallo()
    private val notificaciones = InMemoryNotificationRepository()
    private lateinit var viewModel: HistorialViewModel

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    private fun iniciarComo(rol: Rol, indice: Int = 0) {
        SessionManager.iniciarSesion(SeedData.usuarios.filter { it.rol == rol }[indice])
        viewModel = HistorialViewModel(pedidos, notificaciones, cargarUsuarios = { SeedData.usuarios })
    }

    @Test
    fun cliente_cancelaSuSolicitudPendiente() {
        iniciarComo(Rol.CLIENTE)
        viewModel.onSolicitarCancelacion("sol_101")
        assertEquals("sol_101", viewModel.uiState.value.pedidoPorCancelar?.id)

        viewModel.confirmarCancelacion()

        assertTrue(viewModel.uiState.value.pedidos.isEmpty())
        assertTrue(viewModel.uiState.value.pedidoCancelado)
        viewModel.onErrorMostrado()
        assertFalse(viewModel.uiState.value.pedidoCancelado)
    }

    @Test
    fun cliente_noPuedeCancelarLaSolicitudDeOtro() {
        iniciarComo(Rol.CLIENTE)
        viewModel.onSolicitarCancelacion("sol_102")
        assertNull(viewModel.uiState.value.pedidoPorCancelar)
    }

    @Test
    fun cerrarElDialogo_noCancela() {
        iniciarComo(Rol.CLIENTE)
        viewModel.onSolicitarCancelacion("sol_101")
        viewModel.onCerrarCancelacion()
        viewModel.confirmarCancelacion()
        assertEquals(1, viewModel.uiState.value.pedidos.size)
    }

    @Test
    fun solicitudEnviada_noSePuedeCancelar() {
        iniciarComo(Rol.ADMINISTRADOR)
        val enviado = viewModel.uiState.value.pedidos.first().copy(estado = EstadoPedido.ENVIADO)
        assertFalse(viewModel.uiState.value.puedeCancelar(enviado, "u01"))
    }

    @Test
    fun cancelar_rechazadoPorElServidor_muestraSuMensaje() {
        iniciarComo(Rol.ADMINISTRADOR)
        pedidos.falla = ErrorRemoto(TipoErrorRemoto.RECHAZADO, "No se puede cancelar un pedido ENVIADO")
        viewModel.onSolicitarCancelacion("sol_102")
        viewModel.confirmarCancelacion()
        assertEquals("No se puede cancelar un pedido ENVIADO", viewModel.uiState.value.errorRemoto?.mensajeServidor)
        assertEquals(2, viewModel.uiState.value.pedidos.size)
    }

    @Test
    fun avanzarEstado_sinServidor_noCambiaNiNotifica() {
        iniciarComo(Rol.ADMINISTRADOR)
        val avisosAntes = notificaciones.notificaciones.value.size
        pedidos.falla = ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)

        viewModel.avanzarEstado("sol_101")

        assertEquals(TipoErrorRemoto.SIN_CONEXION, viewModel.uiState.value.errorRemoto?.tipo)
        assertNull(viewModel.uiState.value.ultimoCambio)
        assertEquals(avisosAntes, notificaciones.notificaciones.value.size)
    }

    @Test
    fun actualizar_ok_y_conError() {
        iniciarComo(Rol.CLIENTE)
        viewModel.actualizar()
        assertFalse(viewModel.uiState.value.actualizando)
        assertNull(viewModel.uiState.value.errorRemoto)

        pedidos.falla = ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)
        viewModel.actualizar()
        assertEquals(TipoErrorRemoto.SIN_CONEXION, viewModel.uiState.value.errorRemoto?.tipo)
    }
}
