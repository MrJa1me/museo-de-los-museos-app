package com.example.museo_de_los_museos_app.viewmodel.escanerqr

import com.example.museo_de_los_museos_app.domain.ResultadoQr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EscaneoReciente(val contenido: String, val resultado: ResultadoQr)

/**
 * Último código leído. Lo escribe EscanerQrViewModel y lo lee ResultadoQrViewModel,
 * así la ruta "resultado_qr" no necesita argumentos (no hay que tocar Routes ni el NavHost).
 */
object UltimoEscaneo {
    private val _escaneo = MutableStateFlow<EscaneoReciente?>(null)
    val escaneo: StateFlow<EscaneoReciente?> = _escaneo.asStateFlow()

    fun guardar(escaneo: EscaneoReciente) {
        _escaneo.value = escaneo
    }
}
