package com.example.museo_de_los_museos_app.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.domain.ErrorFicha
import com.example.museo_de_los_museos_app.domain.MotivoQrInvalido

/** Texto (strings_museos.xml) de cada error de la ficha de museo. Mismo estilo que MensajesValidacion.kt. */
@StringRes
fun ErrorFicha.mensaje(): Int = when (this) {
    ErrorFicha.OBLIGATORIO -> R.string.ficha_error_obligatorio
    ErrorFicha.TEXTO_CORTO -> R.string.ficha_error_texto_corto
    ErrorFicha.SOLO_LETRAS -> R.string.ficha_error_solo_letras
    ErrorFicha.NUMERO_INVALIDO -> R.string.ficha_error_numero_invalido
    ErrorFicha.LATITUD_FUERA_DE_RANGO -> R.string.ficha_error_latitud
    ErrorFicha.LONGITUD_FUERA_DE_RANGO -> R.string.ficha_error_longitud
    ErrorFicha.DESCRIPCION_CORTA -> R.string.ficha_error_descripcion_corta
    ErrorFicha.ENLACE_INVALIDO -> R.string.ficha_error_enlace
}

/** Uso en la UI:  mensajeError = estado.errores.nombre.texto()  (null si no hay error). */
@Composable
fun ErrorFicha?.texto(): String? = this?.let { stringResource(it.mensaje()) }

@StringRes
fun MotivoQrInvalido.mensaje(): Int = when (this) {
    MotivoQrInvalido.VACIO -> R.string.qr_error_vacio
    MotivoQrInvalido.FORMATO_DESCONOCIDO -> R.string.qr_error_formato
    MotivoQrInvalido.TIPO_DESCONOCIDO -> R.string.qr_error_tipo
    MotivoQrInvalido.SIN_ID -> R.string.qr_error_sin_id
    MotivoQrInvalido.ENLACE_NO_PERMITIDO -> R.string.qr_error_enlace
    MotivoQrInvalido.NO_ENCONTRADO -> R.string.qr_error_no_encontrado
}

@Composable
fun MotivoQrInvalido?.texto(): String? = this?.let { stringResource(it.mensaje()) }
