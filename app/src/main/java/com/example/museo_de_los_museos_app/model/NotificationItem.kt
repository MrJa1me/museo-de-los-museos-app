package com.example.museo_de_los_museos_app.model

data class NotificationItem(
    val id: String,
    val userId: String,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean = false,
    val fechaMillis: Long = System.currentTimeMillis(),
    /** Ruta de navegación a la que lleva el aviso (ver Routes), o null si no lleva a ninguna. */
    val ruta: String? = null
)
