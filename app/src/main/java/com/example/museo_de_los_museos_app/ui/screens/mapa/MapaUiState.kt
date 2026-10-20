package com.example.museo_de_los_museos_app.ui.screens.mapa

import com.example.museo_de_los_museos_app.model.Museum

/** Estado del permiso de ubicación, para no volver a preguntar sin que el usuario lo pida. */
enum class EstadoPermiso { SIN_CONSULTAR, CONCEDIDO, DENEGADO }

data class MapaUiState(
    /** Museos que se ven en el mapa (ya filtrados por país). */
    val museos: List<Museum> = emptyList(),
    val paises: List<String> = emptyList(),
    /** null = todos los países. */
    val paisSeleccionado: String? = null,
    val museoSeleccionado: Museum? = null,
    val permisoUbicacion: EstadoPermiso = EstadoPermiso.SIN_CONSULTAR,
    val mostrarExplicacionPermiso: Boolean = false,
    /** Eventos de un solo uso: la pantalla los ejecuta y avisa al ViewModel. */
    val solicitarPermiso: Boolean = false,
    val centrarEnUbicacion: Boolean = false,
    val esInvestigador: Boolean = false
)
