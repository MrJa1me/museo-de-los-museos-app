package com.example.museo_de_los_museos_app.repository

import android.content.Context
import com.example.museo_de_los_museos_app.remote.ClienteHttp
import com.example.museo_de_los_museos_app.repository.db.AppDatabase


object Repositorios {
    var museos: MuseumRepository = InMemoryMuseumRepository()
        private set

    var productos: ProductRepository = InMemoryProductRepository()
        private set

    val notificaciones: NotificationRepository = InMemoryNotificationRepository()
    var pedidos: OrderRepository = InMemoryOrderRepository()
        private set

    /** API externa (Art Institute of Chicago): solo lectura, no pasa por Room. */
    val obrasArte: ObrasArteRepository by lazy { ObrasArteRepositoryRemoto(ClienteHttp.obrasArte) }

    lateinit var baseDatos: AppDatabase
        private set

    fun inicializarBaseDatos(context: Context) {
        baseDatos = AppDatabase.crear(context)
        productos = RoomProductRepository(baseDatos.productoDao())
        // Museos y pedidos: microservicios propios (Spring Boot) con copia local en Room
        museos = MuseumRepositoryRemoto(ClienteHttp.museos, baseDatos.museoDao())
        pedidos = OrderRepositoryRemoto(ClienteHttp.pedidos, baseDatos.pedidoDao())

    }


}
