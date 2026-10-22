package com.example.museo_de_los_museos_app.viewmodel.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.repository.FavoriteRepository
import com.example.museo_de_los_museos_app.repository.MuseumRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.favoritos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Colección de museos favoritos (P3). Se filtra por el userId de SessionManager:
 * al cambiar de perfil la lista cambia sola y los datos de cada usuario no se mezclan.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritosViewModel(
    private val museos: MuseumRepository = Repositorios.museos,
    private val favoritos: FavoriteRepository = Repositorios.favoritos
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritosUiState())
    val uiState: StateFlow<FavoritosUiState> = _uiState.asStateFlow()

    init {
        val idsFavoritos = SessionManager.usuarioActual.flatMapLatest { usuario ->
            if (usuario == null) flowOf(null) else favoritos.observar(usuario.id)
        }
        viewModelScope.launch {
            combine(museos.museos, idsFavoritos) { lista, ids -> lista to ids }
                .collect { (lista, ids) ->
                    _uiState.update {
                        FavoritosUiState(
                            cargando = false,
                            haySesion = ids != null,
                            museos = if (ids == null) emptyList() else filtrar(lista, ids)
                        )
                    }
                }
        }
    }

    fun quitar(museoId: String) {
        val userId = SessionManager.userId ?: return
        viewModelScope.launch { favoritos.alternar(userId, museoId) }
    }

    companion object {
        /** Función pura para que sea fácil de probar. */
        fun filtrar(museos: List<Museum>, ids: Set<String>): List<Museum> =
            museos.filter { it.id in ids }.sortedBy { it.nombre }
    }
}
