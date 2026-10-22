package com.example.museo_de_los_museos_app.viewmodel.favoritos

import com.example.museo_de_los_museos_app.model.Museum

data class FavoritosUiState(
    val cargando: Boolean = true,
    val haySesion: Boolean = false,
    /** Museos favoritos del usuario activo, ordenados por nombre. */
    val museos: List<Museum> = emptyList()
)
