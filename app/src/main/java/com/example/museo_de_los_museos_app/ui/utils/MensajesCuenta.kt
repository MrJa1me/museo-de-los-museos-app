package com.example.museo_de_los_museos_app.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.domain.ErrorCuenta

/** Texto (strings_cuenta.xml) de cada error de cuenta. Mismo estilo que MensajesValidacion.kt de P4. */
@StringRes
fun ErrorCuenta.mensaje(): Int = when (this) {
    ErrorCuenta.OBLIGATORIO -> R.string.cuenta_error_obligatorio
    ErrorCuenta.TEXTO_CORTO -> R.string.cuenta_error_texto_corto
    ErrorCuenta.CORREO_INVALIDO -> R.string.cuenta_error_correo_invalido
    ErrorCuenta.CONTRASENA_CORTA -> R.string.cuenta_error_contrasena_corta
    ErrorCuenta.CONTRASENA_DEBIL -> R.string.cuenta_error_contrasena_debil
    ErrorCuenta.NO_COINCIDEN -> R.string.cuenta_error_no_coinciden
    ErrorCuenta.CORREO_EN_USO -> R.string.cuenta_error_correo_en_uso
    ErrorCuenta.CREDENCIALES_INCORRECTAS -> R.string.cuenta_error_credenciales_incorrectas
}

/** Uso en la UI:  mensajeError = estado.errores.correo.texto()  (null si no hay error). */
@Composable
fun ErrorCuenta?.texto(): String? = this?.let { stringResource(it.mensaje()) }
