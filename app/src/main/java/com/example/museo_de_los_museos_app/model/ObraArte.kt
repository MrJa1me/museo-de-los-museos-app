package com.example.museo_de_los_museos_app.model

/** Obra de arte que viene de la API externa (solo lectura, no se guarda en la app). */
data class ObraArte(
    val id: Long,
    val titulo: String,
    val artista: String,
    val fecha: String,
    val origen: String,
    val tecnica: String,
    /** null si la obra no tiene imagen pública. */
    val urlImagen: String?
)
