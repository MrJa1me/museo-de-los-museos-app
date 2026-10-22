package com.example.museo_de_los_museos_app.viewmodel.bienvenida

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.navigation.Routes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel para la pantalla de bienvenida (P2).
 * Expone [BienvenidaUiState] con desacoplamiento total de la UI.
 */
class BienvenidaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(
        BienvenidaUiState(
            tituloRes = R.string.bienvenida_titulo,
            subtituloRes = R.string.bienvenida_subtitulo,
            accesosRapidos = listOf(
                AccesoRapidoItem(
                    id = "catalogo",
                    tituloRes = R.string.bienvenida_acceso_catalogo_titulo,
                    descripcionRes = R.string.bienvenida_acceso_catalogo_desc,
                    icono = Icons.Filled.ShoppingCart,
                    ruta = Routes.Catalogo.route,
                ),
                AccesoRapidoItem(
                    id = "mapa",
                    tituloRes = R.string.bienvenida_acceso_mapa_titulo,
                    descripcionRes = R.string.bienvenida_acceso_mapa_desc,
                    icono = Icons.Filled.Place,
                    ruta = Routes.Mapa.route,
                ),
                AccesoRapidoItem(
                    id = "dashboard",
                    tituloRes = R.string.bienvenida_acceso_dashboard_titulo,
                    descripcionRes = R.string.bienvenida_acceso_dashboard_desc,
                    icono = Icons.Filled.Dashboard,
                    ruta = Routes.Dashboard.route,
                ),
            ),
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
