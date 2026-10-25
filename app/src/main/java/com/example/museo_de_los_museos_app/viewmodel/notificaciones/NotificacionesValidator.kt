package com.example.museo_de_los_museos_app.viewmodel.notificaciones

import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User
import com.example.museo_de_los_museos_app.repository.SeedData

data class ResultadoValidacionAviso(
    val esValido: Boolean,
    val errorTituloRes: Int? = null,
    val errorMensajeRes: Int? = null,
)

/**
 * Lógica pura de validación y filtrado de notificaciones para pruebas unitarias sin dependencias de Android.
 */
object NotificacionesValidator {

    fun validarFormularioAviso(titulo: String, mensaje: String): ResultadoValidacionAviso {
        val errTitulo = if (titulo.isBlank()) R.string.notif_error_titulo_vacio else null
        val errMensaje = if (mensaje.isBlank()) R.string.notif_error_mensaje_vacio else null
        val valido = errTitulo == null && errMensaje == null

        return ResultadoValidacionAviso(
            esValido = valido,
            errorTituloRes = errTitulo,
            errorMensajeRes = errMensaje,
        )
    }

    fun filtrarNotificaciones(
        notificaciones: List<NotificationItem>,
        userId: String?,
        filtro: FiltroNotificacion,
    ): List<NotificationItem> {
        if (userId == null) return emptyList()
        val notifUsuario = notificaciones.filter { it.userId == userId }
        return when (filtro) {
            FiltroNotificacion.TODAS -> notifUsuario
            FiltroNotificacion.NO_LEIDAS -> notifUsuario.filter { !it.leida }
        }
    }
}
