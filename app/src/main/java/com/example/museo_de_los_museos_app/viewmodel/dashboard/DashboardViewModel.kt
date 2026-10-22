package com.example.museo_de_los_museos_app.viewmodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.repository.MuseumRepository
import com.example.museo_de_los_museos_app.repository.OrderRepository
import com.example.museo_de_los_museos_app.repository.ProductRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel del Dashboard (P2).
 * Combina en tiempo real el usuario en sesión con los repositorios de museos, productos y pedidos de P4.
 */
class DashboardViewModel(
    museumRepository: MuseumRepository = Repositorios.museos,
    productRepository: ProductRepository = Repositorios.productos,
    orderRepository: OrderRepository = Repositorios.pedidos,
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        SessionManager.usuarioActual,
        museumRepository.museos,
        productRepository.productos,
        orderRepository.pedidos,
    ) { usuario, listaMuseos, listaProductos, listaPedidos ->
        DashboardEvaluator.evaluarEstado(usuario, listaMuseos, listaProductos, listaPedidos)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(cargando = true),
    )
}
