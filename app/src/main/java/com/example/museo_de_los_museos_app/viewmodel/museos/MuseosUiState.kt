package com.example.museo_de_los_museos_app.viewmodel.museos

import com.example.museo_de_los_museos_app.model.Museum

data class MuseosUiState(
    val museos: List<Museum> = emptyList(),
    /** Admin e Investigador también ven las fichas pendientes; se marcan en la lista. */
    val muestraPendientes: Boolean = false,
    val cargando: Boolean = true,
)


