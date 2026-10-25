package com.example.museo_de_los_museos_app.repository

import android.content.Context

/**
 * Repositorios de P3 con la misma forma que el resto:  Repositorios.favoritos  /  Repositorios.historialQr
 *
 * Igual que RepositorioUsuarios.kt, están como extensión en este archivo para no tocar Repositorios.kt.
 * Para usarlos en otra carpeta:  import com.example.museo_de_los_museos_app.repository.favoritos
 */
private object AlmacenFavoritosQr {
    lateinit var favoritos: FavoriteRepository
    lateinit var historialQr: QrHistoryRepository
}

val Repositorios.favoritos: FavoriteRepository
    get() = AlmacenFavoritosQr.favoritos

val Repositorios.historialQr: QrHistoryRepository
    get() = AlmacenFavoritosQr.historialQr

/** MainActivity lo llama una vez al iniciar, antes de mostrar cualquier pantalla. */
fun Repositorios.inicializarFavoritosQr(context: Context) {
    AlmacenFavoritosQr.favoritos = RoomFavoriteRepository(baseDatos.favoritoDao())
    AlmacenFavoritosQr.historialQr = RoomQrHistoryRepository(baseDatos.historialQrDao())
}
