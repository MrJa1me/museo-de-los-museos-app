package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.repository.db.PedidoDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo
import com.example.museo_de_los_museos_app.repository.db.itemsEntidad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class RoomOrderRepository(private val dao: PedidoDao) : OrderRepository {

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        alcance.launch {
            dao.insertarConItems(
                PEDIDOS_SEMILLA.map { it.aEntidad() },
                PEDIDOS_SEMILLA.flatMap { it.itemsEntidad() }
            )
        }
    }

    override val pedidos: StateFlow<List<Order>> =
        combine(dao.observarPedidos(), dao.observarItems()) { pedidos, items ->
            val itemsPorPedido = items.groupBy { it.pedidoId }
            pedidos.map { it.aModelo(itemsPorPedido[it.id].orEmpty()) }
        }.stateIn(alcance, SharingStarted.Eagerly, emptyList())

    override fun obtenerPorUsuario(userId: String) = pedidos.value.filter { it.userId == userId }

    override fun obtenerPendientes() = pedidos.value.filter { it.estado == EstadoPedido.PENDIENTE }

    override fun crear(pedido: Order) {
        alcance.launch { dao.insertarConItems(listOf(pedido.aEntidad()), pedido.itemsEntidad()) }
    }

    override fun cambiarEstado(id: String, estado: EstadoPedido) {
        alcance.launch { dao.cambiarEstado(id, estado) }
    }
}
