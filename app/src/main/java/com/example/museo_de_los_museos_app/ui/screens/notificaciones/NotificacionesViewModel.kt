package com.example.museo_de_los_museos_app.ui.screens.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.NotificationRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * ViewModel para la gestión de Notificaciones y Avisos (P2).
 * Incluye validación de formulario centralizada y filtrado por usuario.
 */
class NotificacionesViewModel(
    private val notificationRepository: NotificationRepository = Repositorios.notificaciones,
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroNotificacion.TODAS)
    private val _dialogoState = MutableStateFlow(
        DialogoCrearState(
            mostrar = false,
            titulo = "",
            mensaje = "",
            ruta = "",
            errorTitulo = null,
            errorMensaje = null,
        )
    )

    private data class DialogoCrearState(
        val mostrar: Boolean,
        val titulo: String,
        val mensaje: String,
        val ruta: String,
        val errorTitulo: String?,
        val errorMensaje: String?,
    )

    val uiState: StateFlow<NotificacionesUiState> = combine(
        SessionManager.usuarioActual,
        notificationRepository.notificaciones,
        _filtro,
        _dialogoState,
    ) { usuario, todasNotif, filtro, dialogo ->
        val userId = usuario?.id ?: "u03"
        val esAdmin = usuario?.rol == Rol.ADMINISTRADOR

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
            mostrarDialogoCrear = dialogo.mostrar,
            nuevoTitulo = dialogo.titulo,
            nuevoMensaje = dialogo.mensaje,
            nuevaRuta = dialogo.ruta,
            errorTitulo = dialogo.errorTitulo,
            errorMensaje = dialogo.errorMensaje,
            cargando = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificacionesUiState(cargando = true),
    )

    fun cambiarFiltro(filtro: FiltroNotificacion) {
        _filtro.value = filtro
    }

    fun marcarComoLeida(id: String) {
        notificationRepository.marcarComoLeida(id)
    }

    fun setMostrarDialogoCrear(mostrar: Boolean) {
        _dialogoState.update {
            it.copy(
                mostrar = mostrar,
                errorTitulo = null,
                errorMensaje = null,
            )
        }
    }

    fun onNuevoTituloChanged(titulo: String) {
        _dialogoState.update {
            it.copy(
                titulo = titulo,
                errorTitulo = if (titulo.isBlank()) "El título no puede estar vacío" else null,
            )
        }
    }

    fun onNuevoMensajeChanged(mensaje: String) {
        _dialogoState.update {
            it.copy(
                mensaje = mensaje,
                errorMensaje = if (mensaje.isBlank()) "El mensaje no puede estar vacío" else null,
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
            "El título no puede estar vacío"
        } else null

        val errMensaje = if (current.mensaje.isBlank()) {
            valido = false
            "El mensaje no puede estar vacío"
        } else null

        if (!valido) {
            _dialogoState.update {
                it.copy(errorTitulo = errTitulo, errorMensaje = errMensaje)
            }
            return false
        }

        val usuario = SessionManager.usuarioActual.value
        val targetUserId = usuario?.id ?: "u03"

        val nuevoAviso = NotificationItem(
            id = "n_${System.currentTimeMillis()}",
            userId = targetUserId,
            titulo = current.titulo.trim(),
            mensaje = current.mensaje.trim(),
            leida = false,
            fechaMillis = System.currentTimeMillis(),
            ruta = current.ruta.trim().ifEmpty { null },
        )

        notificationRepository.agregarAviso(nuevoAviso)
        _dialogoState.value = DialogoCrearState(
            mostrar = false,
            titulo = "",
            mensaje = "",
            ruta = "",
            errorTitulo = null,
            errorMensaje = null,
        )
        return true
    }
}
