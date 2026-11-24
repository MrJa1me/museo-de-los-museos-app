package com.example.museo_de_los_museos_app.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto

@StringRes
fun TipoErrorRemoto.mensaje(): Int = when (this) {
    TipoErrorRemoto.SIN_CONEXION -> R.string.remoto_sin_conexion
    TipoErrorRemoto.RECHAZADO -> R.string.remoto_rechazado
    TipoErrorRemoto.SERVIDOR -> R.string.remoto_servidor
    TipoErrorRemoto.DESCONOCIDO -> R.string.remoto_desconocido
}

/** Si el microservicio explicó el motivo (ej. "No se puede cancelar un pedido ENVIADO") se muestra tal cual. */
@Composable
fun ErrorRemoto.texto(): String =
    mensajeServidor?.takeIf { tipo == TipoErrorRemoto.RECHAZADO } ?: stringResource(tipo.mensaje())
