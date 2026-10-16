package com.example.museo_de_los_museos_app.model

data class OrderItem(
    val productoId: String,
    val nombre: String,
    val cantidad: Int,
    val precioUnitario: Int
)

data class Order(
    val id: String,
    val userId: String,
    val items: List<OrderItem>,
    val subtotal: Int,
    val costoEnvio: Int,
    val total: Int,
    val estado: EstadoPedido = EstadoPedido.PENDIENTE,
    val destino: String = "",
    val modalidad: String = "",
    val fechaMillis: Long = System.currentTimeMillis()
)
