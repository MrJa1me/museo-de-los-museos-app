package com.example.museo_de_los_museos_app.viewmodel.mapa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.MuseumRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mapa de museos virtuales (P3). La UI solo dibuja el mapa y envía eventos
 * (filtro por país, museo tocado, permiso de ubicación); las decisiones se toman aquí.
 */
class MapaViewModel(
    private val repositorio: MuseumRepository = Repositorios.museos
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    /** Copia de todos los museos del repositorio, sin filtrar. */
    private var todos: List<Museum> = emptyList()

    init {
        // Si el Investigador crea o edita una ficha, el mapa se actualiza solo.
        viewModelScope.launch {
            repositorio.museos.collect { lista ->
                todos = lista
                aplicarFiltro()
            }
        }
        viewModelScope.launch {
            SessionManager.usuarioActual.collect { usuario ->
                _uiState.update { it.copy(esInvestigador = usuario?.rol == Rol.INVESTIGADOR) }
            }
        }
    }

    /** Al tocar el país que ya está seleccionado, se quita el filtro. */
    fun onPaisSeleccionado(pais: String?) {
        _uiState.update {
            recalcular(it.copy(paisSeleccionado = if (it.paisSeleccionado == pais) null else pais, museoSeleccionado = null))
        }
    }

    fun onMuseoSeleccionado(museoId: String) {
        _uiState.update { it.copy(museoSeleccionado = todos.firstOrNull { museo -> museo.id == museoId }) }
    }

    fun onCerrarSeleccion() {
        _uiState.update { it.copy(museoSeleccionado = null) }
    }

    // ---------- Permiso de ubicación ----------

    /** La pantalla revisa al iniciar si el permiso ya estaba concedido. */
    fun onPermisoVerificado(concedido: Boolean) {
        if (concedido) _uiState.update { it.copy(permisoUbicacion = EstadoPermiso.CONCEDIDO) }
    }

    /** Con permiso centra el mapa; sin permiso primero explica para qué se usa. */
    fun onMiUbicacionClick() {
        _uiState.update {
            if (it.permisoUbicacion == EstadoPermiso.CONCEDIDO) it.copy(centrarEnUbicacion = true)
            else it.copy(mostrarExplicacionPermiso = true)
        }
    }

    fun onExplicacionAceptada() {
        _uiState.update { it.copy(mostrarExplicacionPermiso = false, solicitarPermiso = true) }
    }

    fun onExplicacionRechazada() {
        _uiState.update { it.copy(mostrarExplicacionPermiso = false, permisoUbicacion = EstadoPermiso.DENEGADO) }
    }

    fun onSolicitudLanzada() {
        _uiState.update { it.copy(solicitarPermiso = false) }
    }

    fun onResultadoPermiso(concedido: Boolean) {
        _uiState.update {
            it.copy(
                permisoUbicacion = if (concedido) EstadoPermiso.CONCEDIDO else EstadoPermiso.DENEGADO,
                centrarEnUbicacion = concedido
            )
        }
    }

    fun onCentradoRealizado() {
        _uiState.update { it.copy(centrarEnUbicacion = false) }
    }

    private fun aplicarFiltro() {
        _uiState.update { recalcular(it) }
    }

    /** Lista, países y selección en un solo cambio de estado, para que el mapa nunca vea datos a medias. */
    private fun recalcular(estado: MapaUiState): MapaUiState {
        val pais = estado.paisSeleccionado?.takeIf { p -> todos.any { it.pais == p } }
        return estado.copy(
            paisSeleccionado = pais,
            paises = paises(todos),
            museos = filtrar(todos, pais),
            museoSeleccionado = estado.museoSeleccionado?.let { sel -> todos.firstOrNull { it.id == sel.id } }
        )
    }

    companion object {
        /** Funciones puras (sin estado) para que sean fáciles de probar. */
        fun filtrar(museos: List<Museum>, pais: String?): List<Museum> =
            if (pais == null) museos else museos.filter { it.pais == pais }

        fun paises(museos: List<Museum>): List<String> = museos.map { it.pais }.distinct().sorted()
    }
}
