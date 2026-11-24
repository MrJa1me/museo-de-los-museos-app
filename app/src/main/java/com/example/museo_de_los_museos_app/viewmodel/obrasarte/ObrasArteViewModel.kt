package com.example.museo_de_los_museos_app.viewmodel.obrasarte

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.remote.aErrorRemoto
import com.example.museo_de_los_museos_app.repository.ObrasArteRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Obras de arte del mundo (API externa del Art Institute of Chicago).
 * La validación de la búsqueda y el manejo de errores viven aquí; la pantalla solo muestra el estado.
 */
class ObrasArteViewModel(
    private val repositorio: ObrasArteRepository = Repositorios.obrasArte
) : ViewModel() {

    private val _uiState = MutableStateFlow(ObrasArteUiState())
    val uiState: StateFlow<ObrasArteUiState> = _uiState.asStateFlow()

    private var iniciado = false
    private var busquedaActual: Job? = null

    /** Primera carga: si se llegó desde un museo, busca obras de su país; si no, una búsqueda por defecto. */
    fun iniciar(pais: String?) {
        if (iniciado) return
        iniciado = true
        _uiState.update { it.copy(consulta = pais?.takeIf { p -> p.isNotBlank() } ?: CONSULTA_POR_DEFECTO) }
        buscar()
    }

    fun onConsultaChange(valor: String) {
        _uiState.update { it.copy(consulta = valor, consultaInvalida = false) }
    }

    fun onSugerencia(pais: String) {
        _uiState.update { it.copy(consulta = pais, consultaInvalida = false) }
        buscar()
    }

    fun buscar() {
        val consulta = _uiState.value.consulta.trim()
        if (!esConsultaValida(consulta)) {
            _uiState.update { it.copy(consultaInvalida = true) }
            return
        }
        // Si el usuario busca otra cosa antes de que termine la anterior, la anterior se cancela
        busquedaActual?.cancel()
        _uiState.update { it.copy(cargando = true, consultaInvalida = false, errorRemoto = null) }
        busquedaActual = viewModelScope.launch {
            repositorio.buscar(traducirConsulta(consulta))
                .onSuccess { obras ->
                    _uiState.update { it.copy(cargando = false, obras = obras, ultimaBusqueda = consulta) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(cargando = false, obras = emptyList(), errorRemoto = error.aErrorRemoto()) }
                }
        }
    }

    companion object {
        const val LARGO_MINIMO = 2
        const val CONSULTA_POR_DEFECTO = "México"

        /** Países de los museos del mapa, como accesos rápidos. */
        val SUGERENCIAS = listOf("México", "Perú", "Chile", "Argentina", "Brasil", "España", "Portugal")

        /** La API está en inglés: los países de la app se traducen para obtener mejores resultados. */
        private val TRADUCCIONES = mapOf(
            "méxico" to "Mexico", "mexico" to "Mexico", "perú" to "Peru", "peru" to "Peru",
            "brasil" to "Brazil", "españa" to "Spain", "espana" to "Spain",
            "colombia" to "Colombia", "chile" to "Chile", "argentina" to "Argentina",
            "uruguay" to "Uruguay", "ecuador" to "Ecuador", "portugal" to "Portugal"
        )

        fun esConsultaValida(consulta: String): Boolean = consulta.trim().length >= LARGO_MINIMO

        fun traducirConsulta(consulta: String): String =
            TRADUCCIONES[consulta.trim().lowercase()] ?: consulta.trim()
    }
}
