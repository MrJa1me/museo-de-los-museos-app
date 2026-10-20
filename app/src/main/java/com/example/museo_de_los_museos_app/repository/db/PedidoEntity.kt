package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.OrderItem


@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val subtotal: Int,
    val costoEnvio: Int,
    val total: Int,
    val estado: EstadoPedido,
    val destino: String,
    val modalidad: String,
    val fechaMillis: Long
)

/** cada producto de un pedido. */
@Entity(
    tableName = "pedido_items",
    primaryKeys = ["pedidoId", "productoId"],
    foreignKeys = [ForeignKey(
        entity = PedidoEntity::class,
        parentColumns = ["id"],
        childColumns = ["pedidoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("pedidoId")]
)
data class PedidoItemEntity(
    val pedidoId: String,
    val productoId: String,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Int
)

fun PedidoEntity.aModelo(items: List<PedidoItemEntity>) = Order(
    id = id, userId = userId,
    items = items.map { OrderItem(it.productoId, it.nombre, it.cantidad, it.precioUnitario) },
    subtotal = subtotal, costoEnvio = costoEnvio, total = total,
    estado = estado, destino = destino, modalidad = modalidad, fechaMillis = fechaMillis
)

fun Order.aEntidad() = PedidoEntity(id, userId, subtotal, costoEnvio, total, estado, destino, modalidad, fechaMillis)

fun Order.itemsEntidad() = items.map {
    PedidoItemEntity(id, it.productoId, it.nombre, it.cantidad, it.precioUnitario)
}
