package com.example.museo_de_los_museos_app.navigation

/**
 * Todas las rutas de la app en un único archivo (responsable: P2).
 * Si una pantalla necesita un argumento nuevo, agrégalo aquí con un cambio pequeño y avisa a P2.
 */
sealed class Routes(val route: String) {
    data object Bienvenida : Routes("bienvenida")
    data object Registro : Routes("registro")
    data object Login : Routes("login")
    data object Dashboard : Routes("dashboard")
    data object Mapa : Routes("mapa")

    data object DetalleMuseo : Routes("detalle_museo/{museoId}") {
        const val ARG_MUSEO_ID = "museoId"
        fun crear(museoId: String) = "detalle_museo/$museoId"
    }

    data object EscanerQr : Routes("escaner_qr")
    data object ResultadoQr : Routes("resultado_qr")
    data object Catalogo : Routes("catalogo")

    data object DetalleProducto : Routes("detalle_producto/{productoId}") {
        const val ARG_PRODUCTO_ID = "productoId"
        fun crear(productoId: String) = "detalle_producto/$productoId"
    }

    data object Favoritos : Routes("favoritos")
    data object Carrito : Routes("carrito")
    data object Historial : Routes("historial")
    data object Notificaciones : Routes("notificaciones")
    data object Perfil : Routes("perfil")
}
