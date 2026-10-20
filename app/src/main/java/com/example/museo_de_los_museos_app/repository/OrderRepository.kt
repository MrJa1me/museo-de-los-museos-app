package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.OrderItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// TODO(P4): Reemplazar por la interfaz e implementación oficial de P4 en la integración.
interface OrderRepository {
    val pedidos: StateFlow<List<Order>>
    fun obtenerPorUsuario(userId: String): List<Order>
    fun obtenerPendientes(): List<Order>
}

// TODO(P4): Implementación temporal de pedidos para métricas de Dashboard hasta la integración con P4.
class InMemoryOrderRepository : OrderRepository {
    private val _pedidos = MutableStateFlow(
        listOf(
            Order(
                id = "sol_101",
                userId = "u03",
                items = listOf(OrderItem("p01", "Guía de Museos Virtuales", 1, 12000)),
                subtotal = 12000,
                costoEnvio = 3500,
                total = 15500,
                estado = EstadoPedido.PENDIENTE,
                destino = "Región Metropolitana",
                modalidad = "Estándar",
            ),
            Order(
                id = "sol_102",
                userId = "u04",
                items = listOf(OrderItem("m01k", "Kit de guantes y papel libre de ácido", 1, 18000)),
                subtotal = 18000,
                costoEnvio = 3500,
                total = 21500,
                estado = EstadoPedido.PENDIENTE,
                destino = "Valparaíso",
                modalidad = "Estándar",
            ),
        )
    )

    override val pedidos: StateFlow<List<Order>> = _pedidos.asStateFlow()

    override fun obtenerPorUsuario(userId: String): List<Order> {
        return _pedidos.value.filter { it.userId == userId }
    }

    override fun obtenerPendientes(): List<Order> {
        return _pedidos.value.filter { it.estado == EstadoPedido.PENDIENTE }
    }
}
