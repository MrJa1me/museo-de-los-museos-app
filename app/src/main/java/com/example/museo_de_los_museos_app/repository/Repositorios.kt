package com.example.museo_de_los_museos_app.repository

import android.content.Context
import com.example.museo_de_los_museos_app.repository.db.AppDatabase


object Repositorios {
    var museos: MuseumRepository = InMemoryMuseumRepository()
        private set

    var productos: ProductRepository = InMemoryProductRepository()
        private set

    val notificaciones: NotificationRepository = InMemoryNotificationRepository()
    val pedidos: OrderRepository = InMemoryOrderRepository()

    fun inicializarBaseDatos(context: Context) {
        val bd = AppDatabase.crear(context)
        productos = RoomProductRepository(bd.productoDao())
        museos = RoomMuseumRepository(bd.museoDao())
    }


}
