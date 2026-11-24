package com.example.museo_de_los_museos_app.remote.dto

/**
 * Forma exacta del JSON de ms-museos. Los campos son anulables a propósito: Gson no respeta los
 * valores por defecto de Kotlin, así que los mapeadores (Mapeadores.kt) ponen un valor seguro si falta alguno.
 */
data class MuseoDto(
    val id: String? = null,
    val nombre: String? = null,
    val pais: String? = null,
    val ciudad: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val clasificacion: String? = null,
    val descripcion: String? = null,
    val enlace: String? = null,
    val proyecto: String? = null,
    val imagen: String? = null,
    val publicado: Boolean? = null
)
