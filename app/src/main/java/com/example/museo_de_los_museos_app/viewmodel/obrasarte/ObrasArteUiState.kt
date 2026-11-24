package com.example.museo_de_los_museos_app.viewmodel.obrasarte

import com.example.museo_de_los_museos_app.model.ObraArte
import com.example.museo_de_los_museos_app.remote.ErrorRemoto

data class ObrasArteUiState(
    val consulta: String = "",
    /** true si se intentó buscar con menos de 2 caracteres. */
    val consultaInvalida: Boolean = false,
    val cargando: Boolean = false,
    val obras: List<ObraArte> = emptyList(),
    /** Última búsqueda que respondió la API (para el mensaje "no se encontraron obras para…"). */
    val ultimaBusqueda: String? = null,
    val errorRemoto: ErrorRemoto? = null
) {
    val sinResultados: Boolean get() = !cargando && errorRemoto == null && ultimaBusqueda != null && obras.isEmpty()
}
