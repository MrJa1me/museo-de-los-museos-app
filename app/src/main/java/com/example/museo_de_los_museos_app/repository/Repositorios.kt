package com.example.museo_de_los_museos_app.repository

/**
 * Punto único para obtener los repositorios (sin Hilt, como en el curso).
 * Uso en un ViewModel:  Repositorios.museos.museos  /  Repositorios.productos.obtenerPorId("c01")
 */
object Repositorios {
    val museos: MuseumRepository = InMemoryMuseumRepository()
    val productos: ProductRepository = InMemoryProductRepository()
}
