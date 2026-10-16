package com.example.museo_de_los_museos_app.ui.screens.detallemuseo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.ui.components.PlaceholderScreen

// PLACEHOLDER (P3): reemplazar por la pantalla real.
// Cada pantalla vive en su carpeta: Screen.kt + ViewModel.kt + UiState.kt.
// Mantén la firma de la función para no romper el NavHost (si necesitas más parámetros, avisa a P2).
@Composable
fun DetalleMuseoScreen(navController: NavHostController, museoId: String) {
    PlaceholderScreen(
        titulo = R.string.pantalla_detalle_museo,
        responsable = "P3",
        detalle = "id: $museoId"
    )
}
