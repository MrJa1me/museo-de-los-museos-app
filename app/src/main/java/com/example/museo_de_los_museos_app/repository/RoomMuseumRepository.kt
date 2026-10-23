package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.repository.db.MuseoDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class RoomMuseumRepository(
    private val dao: MuseoDao,
    private val alcance: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : MuseumRepository {

    override val museos: StateFlow<List<Museum>> =
        dao.observarTodos()
            .map { filas -> filas.map { it.aModelo() } }
            .stateIn(alcance, SharingStarted.Eagerly, emptyList())

    init {
        alcance.launch {
            dao.insertarSiNoExisten(SeedData.museos.map { it.aEntidad() })
        }
    }

    override fun obtenerPorId(id: String): Museum? =
        museos.value.firstOrNull { it.id == id }

    override fun guardar(museo: Museum) {
        alcance.launch { dao.guardar(museo.aEntidad()) }
    }

    override fun aprobar(id: String) {
        alcance.launch { dao.aprobar(id) }
    }
}
