package com.example.museo_de_los_museos_app.model

data class QrHistory(
    val userId: String,
    val contenido: String,
    val valido: Boolean,
    val descripcion: String = "",
    val fechaMillis: Long = System.currentTimeMillis()
)
