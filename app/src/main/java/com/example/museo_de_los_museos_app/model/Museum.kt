package com.example.museo_de_los_museos_app.model

data class Museum(
    val id: String,
    val nombre: String,
    val pais: String,
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val clasificacion: ClasificacionMuseo,
    val descripcion: String,
    val enlace: String,
    val proyecto: String = "Mapa Iberoamérica 2026"
)
