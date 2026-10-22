package com.example.museo_de_los_museos_app.viewmodel.bienvenida

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Item para representar cada acceso directo disponible en la pantalla de bienvenida.
 */
data class AccesoRapidoItem(
    val id: String,
    val tituloRes: Int,
    val descripcionRes: Int,
    val icono: ImageVector,
    val ruta: String,
)

/**
 * Estado de la UI para la pantalla de Bienvenida (P2).
 */
data class BienvenidaUiState(
    val tituloRes: Int,
    val subtituloRes: Int,
    val animacionVisible: Boolean = false,
    val accesosRapidos: List<AccesoRapidoItem> = emptyList(),
)
