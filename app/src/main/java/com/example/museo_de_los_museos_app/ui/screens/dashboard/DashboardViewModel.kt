package com.example.museo_de_los_museos_app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.MuseumRepository
import com.example.museo_de_los_museos_app.repository.ProductRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel del Dashboard (P2).
 * Combina en tiempo real el usuario en sesión con el catálogo de productos y museos.
 */
class DashboardViewModel(
    museumRepository: MuseumRepository = Repositorios.museos,
    productRepository: ProductRepository = Repositorios.productos,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        SessionManager.usuarioActual,
        museumRepository.museos,
        productRepository.productos,
    ) { usuario, listaMuseos, listaProductos ->
        val rolActivo = usuario?.rol ?: Rol.CLIENTE
        val cursos = listaProductos.filter { it.categoria == CategoriaProducto.CURSO }
        val productosFisicos = listaProductos.filter { it.categoria != CategoriaProducto.CURSO }

        DashboardUiState(
            usuario = usuario,
            rol = rolActivo,
            museosDestacados = listaMuseos.take(4),
            cursosDestacados = cursos.take(3),
            productosDestacados = productosFisicos.take(4),
            totalMuseos = listaMuseos.size,
            totalProductos = productosFisicos.size,
            totalCursos = cursos.size,
            solicitudesPendientesCount = 2,
            cargando = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(cargando = true),
    )
}
