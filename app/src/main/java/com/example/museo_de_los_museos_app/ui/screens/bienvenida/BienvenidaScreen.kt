package com.example.museo_de_los_museos_app.ui.screens.bienvenida

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.AccionPlaceholder
import com.example.museo_de_los_museos_app.ui.components.PlaceholderScreen

// PLACEHOLDER (P2): reemplazar por la pantalla real.
// Cada pantalla vive en su carpeta: Screen.kt + ViewModel.kt + UiState.kt.
// Mantén la firma de la función para no romper el NavHost (si necesitas más parámetros, avisa a P2).
@Composable
fun BienvenidaScreen(navController: NavHostController) {
    PlaceholderScreen(
        titulo = R.string.pantalla_bienvenida,
        responsable = "P2",
        acciones = listOf(
            AccionPlaceholder("Comenzar") { navController.navigate(Routes.Login.route) },
            AccionPlaceholder("Ir al inicio (temporal)") { navController.navigate(Routes.Dashboard.route) }
        )
    )
}
