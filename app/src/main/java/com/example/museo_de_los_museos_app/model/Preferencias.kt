package com.example.museo_de_los_museos_app.model

/** Configuración de un usuario (pantalla Perfil). Se guarda en la tabla SQLite "preferencias". */
data class Preferencias(
    val userId: String,
    val notificaciones: Boolean = true,
    val textoGrande: Boolean = false,
    val idioma: String = "es",
    /** Opción propia de cada rol (su texto cambia según el rol, ver SeccionConfiguracion). */
    val opcionRol: Boolean = false
)
