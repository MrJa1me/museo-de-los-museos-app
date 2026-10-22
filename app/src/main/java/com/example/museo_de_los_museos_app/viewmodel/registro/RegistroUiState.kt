package com.example.museo_de_los_museos_app.viewmodel.registro

import com.example.museo_de_los_museos_app.domain.ErrorCuenta

data class RegistroErrores(
    val nombre: ErrorCuenta? = null,
    val correo: ErrorCuenta? = null,
    val contrasena: ErrorCuenta? = null,
    val confirmacion: ErrorCuenta? = null
) {
    val hayErrores: Boolean
        get() = nombre != null || correo != null || contrasena != null || confirmacion != null
}

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val confirmacion: String = "",
    /** Preferencia inicial: se guarda en la tabla preferencias al crear la cuenta. */
    val recibirNotificaciones: Boolean = true,
    val errores: RegistroErrores = RegistroErrores(),
    /** Después del primer intento de envío, los campos se validan mientras el usuario escribe. */
    val intentoEnvio: Boolean = false,
    val cargando: Boolean = false,
    val registroExitoso: Boolean = false
)
