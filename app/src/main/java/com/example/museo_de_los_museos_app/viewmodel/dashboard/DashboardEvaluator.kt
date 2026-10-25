package com.example.museo_de_los_museos_app.viewmodel.dashboard

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User
import com.example.museo_de_los_museos_app.model.visiblePara

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
        val museosVisibles = listaMuseos.filter { it.visiblePara(usuario?.rol) }

        return DashboardUiState(
            usuario = usuario,
            rol = rolActivo,
            museosDestacados = museosVisibles.filter { it.publicado }.take(4),
            museosPendientes = if (rolActivo == Rol.ADMINISTRADOR || rolActivo == Rol.INVESTIGADOR)
                listaMuseos.filter { !it.publicado } else emptyList(),
            museosTodos = museosVisibles.sortedBy { it.nombre },
            cursosDestacados = cursos.take(3),
            productosDestacados = productosFisicos.take(4),
            totalMuseos = museosVisibles.size,
            totalProductos = productosFisicos.size,
            totalCursos = cursos.size,
            solicitudesPendientesCount = pendientes,
            cargando = false,
        )
    }
}
