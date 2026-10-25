package com.example.museo_de_los_museos_app.viewmodel.notificaciones

import androidx.annotation.StringRes
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.navigation.Routes

enum class FiltroNotificacion { TODAS, NO_LEIDAS }

enum class OpcionDestinatario(@get:StringRes val labelRes: Int, val userIdTarget: String?) {
    TODOS_CLIENTES(R.string.notif_destinatario_todos, null),
    CLIENTE_UNO(R.string.notif_destinatario_cliente1, "u03"),
    CLIENTE_DOS(R.string.notif_destinatario_cliente2, "u04"),
}

/**
 * Estado de la UI para la pantalla de Notificaciones y Avisos (P2).
 * Incluye desacoplamiento total de validaciones y referencias a recursos.
 */
data class NotificacionesUiState(
    val lista: List<NotificationItem> = emptyList(),
    val filtroActual: FiltroNotificacion = FiltroNotificacion.TODAS,
    val noLeidasCount: Int = 0,
    val esAdmin: Boolean = false,
    val tieneSesionActiva: Boolean = false,
    val notificacionSeleccionada: NotificationItem? = null,
    val mostrarDialogoCrear: Boolean = false,
    val destinatarioSeleccionado: OpcionDestinatario = OpcionDestinatario.TODOS_CLIENTES,
    val nuevoTitulo: String = "",
    val nuevoMensaje: String = "",
    val nuevaRuta: String = "",
    @get:StringRes val errorTituloRes: Int? = null,
    @get:StringRes val errorMensajeRes: Int? = null,
    val cargando: Boolean = false,
)

enum class OpcionRutaAviso(@get:StringRes val labelRes: Int, val ruta: String?) {
    SIN_RUTA(R.string.notif_ruta_ninguna, null),
    INICIO(R.string.nav_inicio, Routes.Dashboard.route),
    MAPA(R.string.nav_mapa, Routes.Mapa.route),
    CATALOGO(R.string.nav_catalogo, Routes.Catalogo.route),
    FAVORITOS(R.string.notif_ruta_favoritos, Routes.Favoritos.route),
    CARRITO(R.string.notif_ruta_carrito, Routes.Carrito.route),
    HISTORIAL(R.string.notif_ruta_historial, Routes.Historial.route),
    ESCANER_QR(R.string.notif_ruta_qr, Routes.EscanerQr.route),
    PERFIL(R.string.nav_perfil, Routes.Perfil.route);

    companion object {
        fun desdeRuta(ruta: String?) = entries.firstOrNull { it.ruta == ruta?.ifBlank { null } } ?: SIN_RUTA
        fun esValida(ruta: String?) = ruta != null && entries.any { it.ruta == ruta }
    }
}
