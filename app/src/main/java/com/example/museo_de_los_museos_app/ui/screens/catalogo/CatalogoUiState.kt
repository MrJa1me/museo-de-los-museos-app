package com.example.museo_de_los_museos_app.ui.screens.catalogo

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product

data class CatalogoUiState(
    /** Productos que se muestran (ya filtrados por el ViewModel). */
    val productos: List<Product> = emptyList(),
    val busqueda: String = "",
    /** null = todas las categorías. */
    val categoriaSeleccionada: CategoriaProducto? = null,
    val soloDisponibles: Boolean = false,
    /** El Administrador también ve los productos desactivados. */
    val esAdministrador: Boolean = false
) {
    val hayFiltrosActivos: Boolean
        get() = busqueda.isNotBlank() || categoriaSeleccionada != null || soloDisponibles
}
