package com.example.museo_de_los_museos_app.ui.screens.mapa

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.AccionPlaceholder
import com.example.museo_de_los_museos_app.ui.components.PlaceholderScreen

// PLACEHOLDER (P3): reemplazar por la pantalla real.
// Cada pantalla vive en su carpeta: Screen.kt + ViewModel.kt + UiState.kt.
// Mantén la firma de la función para no romper el NavHost (si necesitas más parámetros, avisa a P2).
@Composable
fun MapaScreen(navController: NavHostController) {
    PlaceholderScreen(
        titulo = R.string.pantalla_mapa,
        responsable = "P3",
        acciones = listOf(
            AccionPlaceholder("Ver detalle del museo m01") { navController.navigate(Routes.DetalleMuseo.crear("m01")) }
        )
    )
}
