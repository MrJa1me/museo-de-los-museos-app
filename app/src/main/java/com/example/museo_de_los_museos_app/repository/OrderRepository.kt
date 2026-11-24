package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.OrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


/**
 * Contrato de acceso a las solicitudes de compra. En la app real es [OrderRepositoryRemoto]
 * (microservicio ms-pedidos + copia en Room); en las pruebas, [InMemoryOrderRepository].
 */
interface OrderRepository {
    val pedidos: StateFlow<List<Order>>
    val conexion: StateFlow<EstadoConexion>
    fun obtenerPorUsuario(userId: String): List<Order>
    fun obtenerPendientes(): List<Order>

    suspend fun sincronizar(): Result<Unit>

    suspend fun crear(pedido: Order): Result<Order>

    /** El servidor solo acepta avanzar un paso: Pendiente → Confirmado → Enviado → Entregado. */
    suspend fun cambiarEstado(id: String, estado: EstadoPedido): Result<Order>

    /** Cancelar una solicitud (el servidor no permite cancelar una ya enviada). */
    suspend fun eliminar(id: String): Result<Unit>
}

/** Pedidos de ejemplo (datos ficticios). */
val PEDIDOS_SEMILLA = listOf(
    Order(
        id = "sol_101", userId = "u03",
        items = listOf(OrderItem("p01", "Guía de Museos Virtuales", 1, 12000)),
        subtotal = 12000, costoEnvio = 3500, total = 15500,
        estado = EstadoPedido.PENDIENTE, destino = "Región Metropolitana", modalidad = "Estándar",
    ),
    Order(
        id = "sol_102", userId = "u04",
        items = listOf(OrderItem("m01k", "Kit de guantes y papel libre de ácido", 1, 18000)),
        subtotal = 18000, costoEnvio = 3500, total = 21500,
        estado = EstadoPedido.PENDIENTE, destino = "Valparaíso", modalidad = "Estándar",
    ),
)


class InMemoryOrderRepository(inicial: List<Order> = PEDIDOS_SEMILLA) : OrderRepository {
    private val _pedidos = MutableStateFlow(inicial)
    override val pedidos: StateFlow<List<Order>> = _pedidos.asStateFlow()
    override val conexion: StateFlow<EstadoConexion> = MutableStateFlow(EstadoConexion.CONECTADO)
    override fun obtenerPorUsuario(userId: String) = _pedidos.value.filter { it.userId == userId }
    override fun obtenerPendientes() = _pedidos.value.filter { it.estado == EstadoPedido.PENDIENTE }
    override suspend fun sincronizar(): Result<Unit> = Result.success(Unit)

    override suspend fun crear(pedido: Order): Result<Order> {
        _pedidos.update { listOf(pedido) + it }
        return Result.success(pedido)
    }

    override suspend fun cambiarEstado(id: String, estado: EstadoPedido): Result<Order> {
        _pedidos.update { lista -> lista.map { if (it.id == id) it.copy(estado = estado) else it } }
        return _pedidos.value.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException(id))
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        _pedidos.update { lista -> lista.filterNot { it.id == id } }
        return Result.success(Unit)
    }
}
