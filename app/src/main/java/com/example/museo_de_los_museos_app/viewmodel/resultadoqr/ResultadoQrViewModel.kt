package com.example.museo_de_los_museos_app.viewmodel.resultadoqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.viewmodel.escanerqr.EscaneoReciente
import com.example.museo_de_los_museos_app.viewmodel.escanerqr.UltimoEscaneo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Muestra el último código leído en el escáner (P3). La interpretación ya la hizo LectorQr. */
class ResultadoQrViewModel : ViewModel() {

    val uiState: StateFlow<ResultadoQrUiState> = UltimoEscaneo.escaneo
        .map { it.aUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UltimoEscaneo.escaneo.value.aUiState())

    private fun EscaneoReciente?.aUiState() =
        if (this == null) ResultadoQrUiState() else ResultadoQrUiState(contenido, resultado)
}
