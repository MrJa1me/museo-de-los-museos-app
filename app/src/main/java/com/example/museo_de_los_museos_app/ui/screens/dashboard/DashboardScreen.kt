package com.example.museo_de_los_museos_app.ui.screens.dashboard

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
fun DashboardScreen(navController: NavHostController) {
    PlaceholderScreen(
        titulo = R.string.pantalla_dashboard,
        responsable = "P2",
        acciones = listOf(
            AccionPlaceholder("Escáner QR") { navController.navigate(Routes.EscanerQr.route) },
            AccionPlaceholder("Favoritos") { navController.navigate(Routes.Favoritos.route) },
            AccionPlaceholder("Carrito") { navController.navigate(Routes.Carrito.route) },
            AccionPlaceholder("Historial") { navController.navigate(Routes.Historial.route) }
        )
    )
}
