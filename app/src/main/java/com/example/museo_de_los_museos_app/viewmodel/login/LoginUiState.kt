package com.example.museo_de_los_museos_app.viewmodel.login

import com.example.museo_de_los_museos_app.domain.ErrorCuenta
import com.example.museo_de_los_museos_app.model.Rol

data class LoginErrores(
    val correo: ErrorCuenta? = null,
    val contrasena: ErrorCuenta? = null
) {
    val hayErrores: Boolean
        get() = correo != null || contrasena != null
}

data class LoginUiState(
    val rol: Rol = Rol.CLIENTE,
    val correo: String = "",
    val contrasena: String = "",
    val errores: LoginErrores = LoginErrores(),
    /** Error que no es de un campo (credenciales incorrectas); se muestra en un banner. */
    val errorGeneral: ErrorCuenta? = null,
    val intentoEnvio: Boolean = false,
    val cargando: Boolean = false,
    val loginExitoso: Boolean = false
)
