package com.example.museo_de_los_museos_app.ui.screens.detalleproducto

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.ui.components.PlaceholderScreen

// PLACEHOLDER (P4): reemplazar por la pantalla real.
// Cada pantalla vive en su carpeta: Screen.kt + ViewModel.kt + UiState.kt.
// Mantén la firma de la función para no romper el NavHost (si necesitas más parámetros, avisa a P2).
@Composable
fun DetalleProductoScreen(navController: NavHostController, productoId: String) {
    PlaceholderScreen(
        titulo = R.string.pantalla_detalle_producto,
        responsable = "P4",
        detalle = "id: $productoId"
    )
}
