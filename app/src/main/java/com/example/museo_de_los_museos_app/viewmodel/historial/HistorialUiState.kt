package com.example.museo_de_los_museos_app.viewmodel.historial

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.repository.EstadoConexion

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
    val ultimoCambio: CambioEstado? = null,
    /** Si los datos vienen del microservicio o de la copia local (Room). */
    val conexion: EstadoConexion = EstadoConexion.SIN_VERIFICAR,
    val actualizando: Boolean = false,
    /** Solicitud que se pidió cancelar (diálogo de confirmación abierto). */
    val pedidoPorCancelar: Order? = null,
    val pedidoCancelado: Boolean = false,
    val errorRemoto: ErrorRemoto? = null
) {
    /** El dueño puede cancelar su solicitud y el Administrador cualquiera, solo antes de que sea enviada. */
    fun puedeCancelar(pedido: Order, userId: String?): Boolean =
        pedido.estado in ESTADOS_CANCELABLES && (esAdministrador || pedido.userId == userId)

    companion object {
        val ESTADOS_CANCELABLES = setOf(EstadoPedido.PENDIENTE, EstadoPedido.CONFIRMADO)
    }
}

/** Último cambio hecho por el Administrador (para el mensaje de confirmación animado). */
data class CambioEstado(val pedidoId: String, val nuevoEstado: EstadoPedido)
