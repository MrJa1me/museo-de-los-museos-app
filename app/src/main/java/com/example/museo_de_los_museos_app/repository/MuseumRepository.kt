package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Museum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


interface MuseumRepository {
    val museos: StateFlow<List<Museum>>
    fun obtenerPorId(id: String): Museum?

    fun guardar(museo: Museum)
}

/** Implementación temporal en memoria, cargada con [SeedData]. No persiste al cerrar la app. */
class InMemoryMuseumRepository(inicial: List<Museum> = SeedData.museos) : MuseumRepository {
    private val _museos = MutableStateFlow(inicial)
    override val museos: StateFlow<List<Museum>> = _museos.asStateFlow()

    override fun obtenerPorId(id: String): Museum? = _museos.value.firstOrNull { it.id == id }

    override fun guardar(museo: Museum) {
        _museos.update { lista ->
            if (lista.any { it.id == museo.id }) {
                lista.map { if (it.id == museo.id) museo else it }
            } else {
                lista + museo
            }
        }
    }
}
