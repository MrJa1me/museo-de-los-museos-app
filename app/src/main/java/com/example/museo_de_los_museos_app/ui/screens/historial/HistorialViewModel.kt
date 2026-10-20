package com.example.museo_de_los_museos_app.ui.screens.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.repository.NotificationRepository
import com.example.museo_de_los_museos_app.repository.OrderRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.usuarios
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Historial y gestión de solicitudes (P4).
 * - Cliente / Investigador: ve solo sus solicitudes (filtradas por el userId de SessionManager).
 * - Administrador: ve todas y avanza su estado Pendiente → Confirmado → Enviado → Entregado;
 *   cada cambio genera un aviso en Notificaciones (P2) para el dueño de la solicitud.
 */
class HistorialViewModel(
    private val pedidos: OrderRepository = Repositorios.pedidos,
    private val notificaciones: NotificationRepository = Repositorios.notificaciones,
    private val cargarUsuarios: suspend () -> List<User> = { Repositorios.usuarios.obtenerTodos() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState())
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val filtro = MutableStateFlow<EstadoPedido?>(null)

    init {
        viewModelScope.launch {
            combine(SessionManager.usuarioActual, pedidos.pedidos, filtro) { usuario, lista, estado ->
                Triple(usuario, lista, estado)
            }.collect { (usuario, lista, estado) ->
                val esAdmin = usuario?.rol == Rol.ADMINISTRADOR
                _uiState.update {
                    it.copy(
                        cargando = false,
                        haySesion = usuario != null,
                        esAdministrador = esAdmin,
                        filtroEstado = estado,
                        pedidos = if (usuario == null) emptyList() else filtrar(lista, usuario.id, esAdmin, estado)
                    )
                }
            }
        }
        viewModelScope.launch {
            val nombres = runCatching { cargarUsuarios() }.getOrDefault(emptyList()).associate { it.id to it.nombre }
            _uiState.update { it.copy(nombresClientes = nombres) }
        }
    }

    /** Al tocar el estado que ya está seleccionado, se quita el filtro. */
    fun onFiltroChange(estado: EstadoPedido?) {
        filtro.update { actual -> if (actual == estado) null else estado }
    }

    /** Solo Administrador: pasa la solicitud al siguiente estado y avisa al cliente. */
    fun avanzarEstado(pedidoId: String) {
        if (!_uiState.value.esAdministrador) return
        val pedido = pedidos.pedidos.value.firstOrNull { it.id == pedidoId } ?: return
        val nuevo = pedido.estado.siguiente() ?: return

        pedidos.cambiarEstado(pedidoId, nuevo)
        notificaciones.agregarAviso(
            NotificationItem(
                id = "estado_${pedidoId}_${System.currentTimeMillis()}",
                userId = pedido.userId,
                titulo = "Tu solicitud cambió a ${nuevo.etiqueta}",
                mensaje = "Tu solicitud #${codigoCorto(pedidoId)} cambió al estado ${nuevo.etiqueta.uppercase()}.",
                ruta = Routes.Historial.route
            )
        )
        _uiState.update { it.copy(ultimoCambio = CambioEstado(pedidoId, nuevo)) }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(ultimoCambio = null) }
    }

    companion object {
        /** Función pura para que sea fácil de probar. */
        fun filtrar(pedidos: List<Order>, userId: String, esAdmin: Boolean, estado: EstadoPedido?): List<Order> =
            pedidos
                .filter { esAdmin || it.userId == userId }
                .filter { estado == null || it.estado == estado }
                .sortedByDescending { it.fechaMillis }

        /** "sol_1729450000123" → "450000123" (más fácil de leer en pantalla). */
        fun codigoCorto(id: String): String = id.removePrefix("sol_").takeLast(9)
    }
}
