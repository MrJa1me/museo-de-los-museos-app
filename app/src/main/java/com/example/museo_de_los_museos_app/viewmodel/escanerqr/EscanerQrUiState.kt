package com.example.museo_de_los_museos_app.viewmodel.escanerqr

import com.example.museo_de_los_museos_app.domain.MotivoQrInvalido
import com.example.museo_de_los_museos_app.model.QrHistory

/** Mensajes breves del lector que la pantalla muestra en un Snackbar. */
enum class MensajeEscaner { CANCELADO, FALLO }

data class EscanerQrUiState(
    val textoManual: String = "",
    val errorManual: MotivoQrInvalido? = null,
    val historial: List<QrHistory> = emptyList(),
    val haySesion: Boolean = false,
    val mensaje: MensajeEscaner? = null,
    /** Evento: ir a la pantalla de resultado. */
    val navegarAResultado: Boolean = false
)
