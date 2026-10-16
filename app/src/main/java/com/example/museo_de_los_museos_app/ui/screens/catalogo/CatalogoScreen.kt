package com.example.museo_de_los_museos_app.ui.screens.catalogo

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.ui.components.AccionPlaceholder
import com.example.museo_de_los_museos_app.ui.components.PlaceholderScreen

// PLACEHOLDER (P4): reemplazar por la pantalla real.
// Cada pantalla vive en su carpeta: Screen.kt + ViewModel.kt + UiState.kt.
// Mantén la firma de la función para no romper el NavHost (si necesitas más parámetros, avisa a P2).
@Composable
fun CatalogoScreen(navController: NavHostController) {
    PlaceholderScreen(
        titulo = R.string.pantalla_catalogo,
        responsable = "P4",
        acciones = listOf(
            AccionPlaceholder("Ver detalle del curso c01") { navController.navigate(Routes.DetalleProducto.crear("c01")) }
        )
    )
}
