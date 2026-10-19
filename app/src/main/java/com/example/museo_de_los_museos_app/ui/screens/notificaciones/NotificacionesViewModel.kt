package com.example.museo_de_los_museos_app.ui.screens.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.NotificationRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * ViewModel para la gestión de Notificaciones y Avisos (P2).
 * Sin fallback "u03", soporta selector de destinatario y validaciones centralizadas.
 */
class NotificacionesViewModel(
    private val notificationRepository: NotificationRepository = Repositorios.notificaciones,
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroNotificacion.TODAS)
    private val _notificacionSeleccionada = MutableStateFlow<NotificationItem?>(null)
    private val _dialogoState = MutableStateFlow(DialogoCrearState())

    private data class DialogoCrearState(
        val mostrar: Boolean = false,
        val destinatario: OpcionDestinatario = OpcionDestinatario.TODOS_CLIENTES,
        val titulo: String = "",
        val mensaje: String = "",
        val ruta: String = "",
        val errorTituloRes: Int? = null,
        val errorMensajeRes: Int? = null,
    )

    val uiState: StateFlow<NotificacionesUiState> = combine(
        SessionManager.usuarioActual,
        notificationRepository.notificaciones,
        _filtro,
        _notificacionSeleccionada,
        _dialogoState,
    ) { usuario, todasNotif, filtro, seleccionada, dialogo ->
        if (usuario == null) {
            NotificacionesUiState(
                tieneSesionActiva = false,
                cargando = false,
            )
        } else {
            val userId = usuario.id
            val esAdmin = usuario.rol == Rol.ADMINISTRADOR

            val notifUsuario = todasNotif.filter { it.userId == userId }
            val noLeidas = notifUsuario.count { !it.leida }

            val listaFiltrada = when (filtro) {
                FiltroNotificacion.TODAS -> notifUsuario
                FiltroNotificacion.NO_LEIDAS -> notifUsuario.filter { !it.leida }
            }

            NotificacionesUiState(
                lista = listaFiltrada,
                filtroActual = filtro,
                noLeidasCount = noLeidas,
                esAdmin = esAdmin,
                tieneSesionActiva = true,
                notificacionSeleccionada = seleccionada ?: listaFiltrada.firstOrNull(),
                mostrarDialogoCrear = dialogo.mostrar,
                destinatarioSeleccionado = dialogo.destinatario,
                nuevoTitulo = dialogo.titulo,
                nuevoMensaje = dialogo.mensaje,
                nuevaRuta = dialogo.ruta,
                errorTituloRes = dialogo.errorTituloRes,
                errorMensajeRes = dialogo.errorMensajeRes,
                cargando = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificacionesUiState(cargando = true),
    )

    fun cambiarFiltro(filtro: FiltroNotificacion) {
        _filtro.value = filtro
    }

    fun seleccionarNotificacion(item: NotificationItem?) {
        _notificacionSeleccionada.value = item
    }

    fun marcarComoLeida(id: String) {
        notificationRepository.marcarComoLeida(id)
    }

    fun setMostrarDialogoCrear(mostrar: Boolean) {
        _dialogoState.update {
            it.copy(
                mostrar = mostrar,
                errorTituloRes = null,
                errorMensajeRes = null,
            )
        }
    }

    fun onDestinatarioChanged(opcion: OpcionDestinatario) {
        _dialogoState.update { it.copy(destinatario = opcion) }
    }

    fun onNuevoTituloChanged(titulo: String) {
        _dialogoState.update {
            it.copy(
                titulo = titulo,
                errorTituloRes = if (titulo.isBlank()) R.string.notif_error_titulo_vacio else null,
            )
        }
    }

    fun onNuevoMensajeChanged(mensaje: String) {
        _dialogoState.update {
            it.copy(
                mensaje = mensaje,
                errorMensajeRes = if (mensaje.isBlank()) R.string.notif_error_mensaje_vacio else null,
            )
        }
    }

    fun onNuevaRutaChanged(ruta: String) {
        _dialogoState.update { it.copy(ruta = ruta) }
    }

    fun publicarAvisoDePrueba(): Boolean {
        val current = _dialogoState.value
        var valido = true

        val errTitulo = if (current.titulo.isBlank()) {
            valido = false
            R.string.notif_error_titulo_vacio
        } else null

        val errMensaje = if (current.mensaje.isBlank()) {
            valido = false
            R.string.notif_error_mensaje_vacio
        } else null

        if (!valido) {
            _dialogoState.update {
                it.copy(errorTituloRes = errTitulo, errorMensajeRes = errMensaje)
            }
            return false
        }

        val targetUserIds = when (current.destinatario) {
            OpcionDestinatario.TODOS_CLIENTES -> SeedData.usuarios
                .filter { it.rol == Rol.CLIENTE }
                .map { it.id }

            OpcionDestinatario.CLIENTE_UNO -> listOf("u03")
            OpcionDestinatario.CLIENTE_DOS -> listOf("u04")
        }

        targetUserIds.forEach { targetId ->
            val nuevoAviso = NotificationItem(
                id = "n_${System.currentTimeMillis()}_$targetId",
                userId = targetId,
                titulo = current.titulo.trim(),
                mensaje = current.mensaje.trim(),
                leida = false,
                fechaMillis = System.currentTimeMillis(),
                ruta = current.ruta.trim().ifEmpty { null },
            )
            notificationRepository.agregarAviso(nuevoAviso)
        }

        _dialogoState.value = DialogoCrearState()
        return true
    }
}
