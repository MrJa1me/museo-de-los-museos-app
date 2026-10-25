package com.example.museo_de_los_museos_app.navigation

import androidx.navigation.NavHostController

/** Rutas que son pestañas de la barra inferior. */
val rutasDeTabs = setOf(
    Routes.Dashboard.route,
    Routes.Mapa.route,
    Routes.Catalogo.route,
    Routes.Notificaciones.route,
    Routes.Perfil.route
)

fun NavHostController.navegarATab(ruta: String) {
    val rutaActual = currentBackStackEntry?.destination?.route
    if (rutaActual == ruta) return

    if (ruta != Routes.Dashboard.route && popBackStack(ruta, inclusive = false)) return

    navigate(ruta) {
        popUpTo(Routes.Dashboard.route) { saveState = true }
        launchSingleTop = true
        restoreState = (ruta != Routes.Dashboard.route)
    }
}

fun NavHostController.navegarDesdeAviso(ruta: String) {
    try {
        if (ruta in rutasDeTabs) navegarATab(ruta) else navigate(ruta)
    } catch (e: IllegalArgumentException) {
        android.util.Log.w("Navegacion", "Ruta de aviso inválida: $ruta")
    }
}