package com.example.museo_de_los_museos_app.ui.screens.notificaciones

import com.example.museo_de_los_museos_app.model.NotificationItem

enum class FiltroNotificacion { TODAS, NO_LEIDAS }

/**
 * Estado de la UI para la pantalla de Notificaciones y Avisos (P2).
 * Incluye desacoplamiento total de validaciones y formularios.
 */
data class NotificacionesUiState(
    val lista: List<NotificationItem> = emptyList(),
    val filtroActual: FiltroNotificacion = FiltroNotificacion.TODAS,
    val noLeidasCount: Int = 0,
    val esAdmin: Boolean = false,
    val mostrarDialogoCrear: Boolean = false,
    val nuevoTitulo: String = "",
    val nuevoMensaje: String = "",
    val nuevaRuta: String = "",
    val errorTitulo: String? = null,
    val errorMensaje: String? = null,
    val cargando: Boolean = false,
)
