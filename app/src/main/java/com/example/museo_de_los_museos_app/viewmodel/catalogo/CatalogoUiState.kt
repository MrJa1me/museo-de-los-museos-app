package com.example.museo_de_los_museos_app.viewmodel.catalogo

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.repository.EstadoConexion

data class CatalogoUiState(
    /** Productos que se muestran (ya filtrados por el ViewModel). */
    val productos: List<Product> = emptyList(),
    val busqueda: String = "",
    /** null = todas las categorías. */
    val categoriaSeleccionada: CategoriaProducto? = null,
    val soloDisponibles: Boolean = false,
    /** El Administrador también ve los productos desactivados. */
    val esAdministrador: Boolean = false,
    /** Si ms-productos no respondió, se muestra la copia de Room con el aviso «Sin conexión». */
    val conexion: EstadoConexion = EstadoConexion.SIN_VERIFICAR,
    /** true mientras se descarga el catálogo del servidor (botón Actualizar). */
    val actualizando: Boolean = false
) {
    val hayFiltrosActivos: Boolean
        get() = busqueda.isNotBlank() || categoriaSeleccionada != null || soloDisponibles
}
