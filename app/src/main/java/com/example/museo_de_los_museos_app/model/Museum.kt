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
    val proyecto: String = "Mapa Iberoamérica 2026",
    /** Nombre del archivo en res/drawable, sin extensión (ej. "museo_oro"). Vacío = imagen genérica. */
    val imagen: String = "",
    /** Las fichas nuevas quedan pendientes hasta que el Administrador las aprueba. */
    val publicado: Boolean = false
)

/**
 * Un Cliente (o invitado, [rol] null) solo ve museos publicados.
 * El Administrador y el Investigador ven también los pendientes para poder revisarlos.
 */
fun Museum.visiblePara(rol: Rol?): Boolean =
    publicado || rol == Rol.ADMINISTRADOR || rol == Rol.INVESTIGADOR
