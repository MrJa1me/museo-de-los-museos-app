package com.example.museo_de_los_museos_app.ui.screens.resultadoqr

import com.example.museo_de_los_museos_app.domain.ResultadoQr

data class ResultadoQrUiState(
    /** Texto tal cual venía en el QR. */
    val contenido: String = "",
    /** null si se llegó a la pantalla sin un escaneo reciente (por ejemplo, tras cerrar la app). */
    val resultado: ResultadoQr? = null
) {
    val esExito: Boolean get() = resultado != null && resultado !is ResultadoQr.Error
}
