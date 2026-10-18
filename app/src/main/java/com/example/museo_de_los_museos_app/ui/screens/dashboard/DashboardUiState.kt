package com.example.museo_de_los_museos_app.ui.screens.dashboard

import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User

/**
 * Estado de la UI para el Dashboard / Pantalla de Inicio (P2).
 * Modela la información según el rol activo (Cliente, Administrador, Investigador).
 */
data class DashboardUiState(
    val usuario: User? = null,
    val rol: Rol = Rol.CLIENTE,
    val museosDestacados: List<Museum> = emptyList(),
    val cursosDestacados: List<Product> = emptyList(),
    val productosDestacados: List<Product> = emptyList(),
    val totalMuseos: Int = 0,
    val totalProductos: Int = 0,
    val totalCursos: Int = 0,
    val solicitudesPendientesCount: Int = 2,
    val cargando: Boolean = false,
)
