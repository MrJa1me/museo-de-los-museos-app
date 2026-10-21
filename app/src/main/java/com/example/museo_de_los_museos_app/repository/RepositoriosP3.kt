package com.example.museo_de_los_museos_app.repository

import android.content.Context

/**
 * Repositorios de P3 con la misma forma que el resto:  Repositorios.favoritos  /  Repositorios.historialQr
 *
 * Igual que RepositorioUsuarios.kt, están como extensión en este archivo para no tocar Repositorios.kt.
 * Para usarlos en otra carpeta:  import com.example.museo_de_los_museos_app.repository.favoritos
 */
private object AlmacenP3 {
    lateinit var favoritos: FavoriteRepository
    lateinit var historialQr: QrHistoryRepository
}

val Repositorios.favoritos: FavoriteRepository
    get() = AlmacenP3.favoritos

val Repositorios.historialQr: QrHistoryRepository
    get() = AlmacenP3.historialQr

/** MainActivity lo llama una vez al iniciar, antes de mostrar cualquier pantalla. */
fun Repositorios.inicializarP3(context: Context) {
    AlmacenP3.favoritos = RoomFavoriteRepository(baseDatos.favoritoDao())
    AlmacenP3.historialQr = RoomQrHistoryRepository(baseDatos.historialQrDao())
}
