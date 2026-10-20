package com.example.museo_de_los_museos_app.repository

/**
 * Repositorios de P4 con la misma forma que el resto:  Repositorios.carrito
 *
 * Igual que RepositoriosP3.kt, están como extensión en este archivo para no tocar Repositorios.kt.
 * Se crean la primera vez que se usan (después de que MainActivity inicializa la base de datos).
 * Para usarlos en otra carpeta:  import com.example.museo_de_los_museos_app.repository.carrito
 */
private object AlmacenP4 {
    val carrito: CartRepository by lazy { RoomCartRepository(Repositorios.baseDatos.carritoDao()) }
}

val Repositorios.carrito: CartRepository
    get() = AlmacenP4.carrito
