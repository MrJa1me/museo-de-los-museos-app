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

    lateinit var baseDatos: AppDatabase
        private set

    fun inicializarBaseDatos(context: Context) {
        baseDatos = AppDatabase.crear(context)
        productos = RoomProductRepository(baseDatos.productoDao())
        museos = RoomMuseumRepository(baseDatos.museoDao())

    }


}
