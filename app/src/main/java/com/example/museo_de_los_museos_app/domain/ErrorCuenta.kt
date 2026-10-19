package com.example.museo_de_los_museos_app.domain

/**
 * Errores de validación de las pantallas de cuenta (registro y login). Igual que ErrorValidacion de P4,
 * no tienen texto: la UI los traduce a strings.xml con ErrorCuenta.mensaje() (ui/utils/MensajesCuenta.kt).
 */
enum class ErrorCuenta {
    OBLIGATORIO,
    TEXTO_CORTO,
    CORREO_INVALIDO,
    CONTRASENA_CORTA,
    CONTRASENA_DEBIL,
    NO_COINCIDEN,
    CORREO_EN_USO,
    CREDENCIALES_INCORRECTAS
}
