package com.example.museo_de_los_museos_app.viewmodel.dashboard

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User

/**
 * Lógica pura de evaluación de métricas y contenidos del Dashboard para pruebas unitarias sin dependencias Android.
 */
object DashboardEvaluator {

    fun evaluarEstado(
        usuario: User?,
        listaMuseos: List<Museum>,
        listaProductos: List<Product>,
        listaPedidos: List<Order>,
    ): DashboardUiState {
        val rolActivo = usuario?.rol ?: Rol.CLIENTE
        val cursos = listaProductos.filter { it.categoria == CategoriaProducto.CURSO }
        val productosFisicos = listaProductos.filter { it.categoria != CategoriaProducto.CURSO }
        val pendientes = listaPedidos.count { it.estado == EstadoPedido.PENDIENTE }

        return DashboardUiState(
            usuario = usuario,
            rol = rolActivo,
            museosDestacados = listaMuseos.take(4),
            cursosDestacados = cursos.take(3),
            productosDestacados = productosFisicos.take(4),
            totalMuseos = listaMuseos.size,
            totalProductos = productosFisicos.size,
            totalCursos = cursos.size,
            solicitudesPendientesCount = pendientes,
            cargando = false,
        )
    }
}
