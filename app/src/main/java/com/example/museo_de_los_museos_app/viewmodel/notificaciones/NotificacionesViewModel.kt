package com.example.museo_de_los_museos_app.viewmodel.notificaciones

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
import kotlinx.coroutines.flow.onEach

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
        SessionManager.usuarioActual.onEach { u -> u?.let(notificationRepository::entregarAvisosDeRol) },
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
            val esAdmin = usuario.rol == Rol.ADMINISTRADOR
            val listaFiltrada = NotificacionesValidator.filtrarNotificaciones(
                notificaciones = todasNotif,
                userId = usuario.id,
                filtro = filtro,
            )
            val noLeidas = todasNotif.count { it.userId == usuario.id && !it.leida }

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
            val validacion = NotificacionesValidator.validarFormularioAviso(titulo, _dialogoState.value.mensaje)
            it.copy(
                titulo = titulo,
                errorTituloRes = validacion.errorTituloRes,
            )
        }
    }

    fun onNuevoMensajeChanged(mensaje: String) {
        _dialogoState.update {
            val validacion = NotificacionesValidator.validarFormularioAviso(_dialogoState.value.titulo, mensaje)
            it.copy(
                mensaje = mensaje,
                errorMensajeRes = validacion.errorMensajeRes,
            )
        }
    }

    fun onNuevaRutaChanged(ruta: String) {
        _dialogoState.update { it.copy(ruta = ruta) }
    }

    fun publicarAvisoDePrueba(): Boolean {
        val current = _dialogoState.value
        val validacion = NotificacionesValidator.validarFormularioAviso(current.titulo, current.mensaje)

        if (!validacion.esValido) {
            _dialogoState.update {
                it.copy(
                    errorTituloRes = validacion.errorTituloRes,
                    errorMensajeRes = validacion.errorMensajeRes,
                )
            }
            return false
        }

        val ahora = System.currentTimeMillis()
        val ruta = current.ruta.trim().takeIf(OpcionRutaAviso::esValida)
        val base = NotificationItem(
            id = "n_$ahora",
            userId = "",
            titulo = current.titulo.trim(),
            mensaje = current.mensaje.trim(),
            leida = false,
            fechaMillis = ahora,
            ruta = ruta,
        )

        val idUnico = current.destinatario.userIdTarget
        if (idUnico == null) {
            notificationRepository.publicarParaRol(Rol.CLIENTE, base)
            SessionManager.usuarioActual.value?.let(notificationRepository::entregarAvisosDeRol)
        } else {
            notificationRepository.agregarAviso(base.copy(id = "n_${ahora}_$idUnico", userId = idUnico))
        }

        _dialogoState.value = DialogoCrearState()
        return true
    }
}
