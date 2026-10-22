package com.example.museo_de_los_museos_app.viewmodel.historial

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order

data class HistorialUiState(
    val cargando: Boolean = true,
    val haySesion: Boolean = false,
    /** El Administrador ve las solicitudes de todos y puede cambiar su estado. */
    val esAdministrador: Boolean = false,
    /** Solicitudes visibles (ya filtradas por usuario y por estado). */
    val pedidos: List<Order> = emptyList(),
    /** null = todos los estados. */
    val filtroEstado: EstadoPedido? = null,
    /** userId → nombre, para que el Administrador vea de quién es cada solicitud. */
    val nombresClientes: Map<String, String> = emptyMap(),
    val ultimoCambio: CambioEstado? = null
)

/** Último cambio hecho por el Administrador (para el mensaje de confirmación animado). */
data class CambioEstado(val pedidoId: String, val nuevoEstado: EstadoPedido)
