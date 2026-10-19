package com.example.museo_de_los_museos_app.domain

/**
 * Validadores de P1 para registro, login y perfil. Viven fuera de los Composables (IE 2.2.1) y no dependen
 * de Android, así que se prueban con pruebas unitarias. Cada función devuelve null si el valor es válido.
 * (Los validadores de compra, envío y producto de P4 están en [Validadores].)
 */
object ValidadoresCuenta {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    const val LARGO_MINIMO_NOMBRE = 2
    const val LARGO_MINIMO_CONTRASENA = 8

    fun nombre(valor: String): ErrorCuenta? = when {
        valor.isBlank() -> ErrorCuenta.OBLIGATORIO
        valor.trim().length < LARGO_MINIMO_NOMBRE -> ErrorCuenta.TEXTO_CORTO
        else -> null
    }

    fun correo(valor: String): ErrorCuenta? = when {
        valor.isBlank() -> ErrorCuenta.OBLIGATORIO
        !REGEX_CORREO.matches(valor.trim()) -> ErrorCuenta.CORREO_INVALIDO
        else -> null
    }

    fun contrasena(valor: String): ErrorCuenta? = when {
        valor.isEmpty() -> ErrorCuenta.OBLIGATORIO
        valor.length < LARGO_MINIMO_CONTRASENA -> ErrorCuenta.CONTRASENA_CORTA
        valor.none { it.isLetter() } || valor.none { it.isDigit() } -> ErrorCuenta.CONTRASENA_DEBIL
        else -> null
    }

    fun confirmacionContrasena(contrasena: String, confirmacion: String): ErrorCuenta? = when {
        confirmacion.isEmpty() -> ErrorCuenta.OBLIGATORIO
        confirmacion != contrasena -> ErrorCuenta.NO_COINCIDEN
        else -> null
    }
}
