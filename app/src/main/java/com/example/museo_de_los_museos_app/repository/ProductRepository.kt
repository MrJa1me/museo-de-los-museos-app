package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Contrato de acceso al catálogo (cursos y productos). Igual que MuseumRepository:
 * la implementación se puede cambiar a DataStore sin tocar las pantallas.
 */
interface ProductRepository {
    val productos: StateFlow<List<Product>>
    fun obtenerPorId(id: String): Product?

    /** Crea el producto si el id no existe o lo reemplaza si ya existe (alta/edición/desactivación del Administrador). */
    fun guardar(producto: Product)
}

/** Implementación temporal en memoria, cargada con [SeedData]. No persiste al cerrar la app. */
class InMemoryProductRepository(inicial: List<Product> = SeedData.productos) : ProductRepository {
    private val _productos = MutableStateFlow(inicial)
    override val productos: StateFlow<List<Product>> = _productos.asStateFlow()

    override fun obtenerPorId(id: String): Product? = _productos.value.firstOrNull { it.id == id }

    override fun guardar(producto: Product) {
        _productos.update { lista ->
            if (lista.any { it.id == producto.id }) {
                lista.map { if (it.id == producto.id) producto else it }
            } else {
                lista + producto
            }
        }
    }
}
