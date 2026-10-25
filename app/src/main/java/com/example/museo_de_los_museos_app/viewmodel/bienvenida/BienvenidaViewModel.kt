package com.example.museo_de_los_museos_app.viewmodel.bienvenida

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class BienvenidaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        BienvenidaUiState(
            tituloRes = R.string.bienvenida_titulo,
            subtituloRes = R.string.bienvenida_subtitulo,
        )
    )
    val uiState: StateFlow<BienvenidaUiState> = _uiState.asStateFlow()

    init {
        iniciarAnimacionEntrada()
    }

    fun iniciarAnimacionEntrada() {
        viewModelScope.launch {
            _uiState.update { it.copy(animacionVisible = false) }
            delay(100.milliseconds)
            _uiState.update { it.copy(animacionVisible = true) }
        }
    }
}