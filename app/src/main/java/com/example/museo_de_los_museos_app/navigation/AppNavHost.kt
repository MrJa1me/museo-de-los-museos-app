package com.example.museo_de_los_museos_app.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.ui.screens.bienvenida.BienvenidaScreen
import com.example.museo_de_los_museos_app.ui.screens.carrito.CarritoScreen
import com.example.museo_de_los_museos_app.ui.screens.catalogo.CatalogoScreen
import com.example.museo_de_los_museos_app.ui.screens.dashboard.DashboardScreen
import com.example.museo_de_los_museos_app.ui.screens.detallemuseo.DetalleMuseoScreen
import com.example.museo_de_los_museos_app.ui.screens.detalleproducto.DetalleProductoScreen
import com.example.museo_de_los_museos_app.ui.screens.escanerqr.EscanerQrScreen
import com.example.museo_de_los_museos_app.ui.screens.favoritos.FavoritosScreen
import com.example.museo_de_los_museos_app.ui.screens.formularioproducto.FormularioProductoScreen
import com.example.museo_de_los_museos_app.ui.screens.historial.HistorialScreen
import com.example.museo_de_los_museos_app.ui.screens.login.LoginScreen
import com.example.museo_de_los_museos_app.ui.screens.mapa.MapaScreen
import com.example.museo_de_los_museos_app.ui.screens.notificaciones.NotificacionesScreen
import com.example.museo_de_los_museos_app.ui.screens.perfil.PerfilScreen
import com.example.museo_de_los_museos_app.ui.screens.registro.RegistroScreen
import com.example.museo_de_los_museos_app.ui.screens.resultadoqr.ResultadoQrScreen

private data class DestinoBarra(
    val ruta: String,
    @StringRes val etiqueta: Int,
    val icono: ImageVector
)

private val destinosBarra = listOf(
    DestinoBarra(Routes.Dashboard.route, R.string.nav_inicio, Icons.Filled.Home),
    DestinoBarra(Routes.Mapa.route, R.string.nav_mapa, Icons.Filled.Place),
    DestinoBarra(Routes.Catalogo.route, R.string.nav_catalogo, Icons.Filled.ShoppingCart),
    DestinoBarra(Routes.Notificaciones.route, R.string.nav_notificaciones, Icons.Filled.Notifications),
    DestinoBarra(Routes.Perfil.route, R.string.nav_perfil, Icons.Filled.Person)
)

/** Pantallas donde NO se muestra la barra inferior. */
private val rutasSinBarra = setOf(
    Routes.Bienvenida.route,
    Routes.Registro.route,
    Routes.Login.route
)

/**
 * NavHost principal de la app (responsable: P2). Cada pantalla vive en su carpeta
 * ui/screens/<pantalla>/ y aquí solo se conecta. Nadie modifica este archivo sin avisar a P2.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (rutaActual != null && rutaActual !in rutasSinBarra) {
                BarraInferior(navController = navController, rutaActual = rutaActual)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Bienvenida.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Routes.Bienvenida.route) { BienvenidaScreen(navController) }
            composable(Routes.Registro.route) { RegistroScreen(navController) }
            composable(Routes.Login.route) { LoginScreen(navController) }
            composable(Routes.Dashboard.route) { DashboardScreen(navController) }
            composable(Routes.Mapa.route) { MapaScreen(navController) }
            composable(
                route = Routes.DetalleMuseo.route,
                arguments = listOf(navArgument(Routes.DetalleMuseo.ARG_MUSEO_ID) { type = NavType.StringType })
            ) { entrada ->
                val museoId = entrada.arguments?.getString(Routes.DetalleMuseo.ARG_MUSEO_ID).orEmpty()
                DetalleMuseoScreen(navController, museoId)
            }
            composable(Routes.EscanerQr.route) { EscanerQrScreen(navController) }
            composable(Routes.ResultadoQr.route) { ResultadoQrScreen(navController) }
            composable(Routes.Catalogo.route) { CatalogoScreen(navController) }
            composable(
                route = Routes.DetalleProducto.route,
                arguments = listOf(navArgument(Routes.DetalleProducto.ARG_PRODUCTO_ID) { type = NavType.StringType })
            ) { entrada ->
                val productoId = entrada.arguments?.getString(Routes.DetalleProducto.ARG_PRODUCTO_ID).orEmpty()
                DetalleProductoScreen(navController, productoId)
            }
            composable(
                route = Routes.FormularioProducto.route,
                arguments = listOf(navArgument(Routes.FormularioProducto.ARG_PRODUCTO_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entrada ->
                val productoId = entrada.arguments?.getString(Routes.FormularioProducto.ARG_PRODUCTO_ID)
                FormularioProductoScreen(navController, productoId)
            }
            composable(Routes.Favoritos.route) { FavoritosScreen(navController) }
            composable(Routes.Carrito.route) { CarritoScreen(navController) }
            composable(Routes.Historial.route) { HistorialScreen(navController) }
            composable(Routes.Notificaciones.route) { NotificacionesScreen(navController) }
            composable(Routes.Perfil.route) { PerfilScreen(navController) }
        }
    }
}

@Composable
private fun BarraInferior(navController: NavHostController, rutaActual: String) {
    NavigationBar {
        destinosBarra.forEach { destino ->
            NavigationBarItem(
                selected = rutaActual == destino.ruta,
                onClick = { navController.navegarATab(destino.ruta) },
                icon = { Icon(imageVector = destino.icono, contentDescription = null) },
                label = { Text(stringResource(destino.etiqueta)) }
            )
        }
    }
}
