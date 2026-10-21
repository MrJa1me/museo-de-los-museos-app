package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.repository.db.ProductoDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class RoomProductRepository(
    private val dao: ProductoDao,
    private val alcance: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : ProductRepository {

    override val productos: StateFlow<List<Product>> =
        dao.observarTodos()
            .map { filas -> filas.map { it.aModelo() } }
            .stateIn(alcance, SharingStarted.Eagerly, emptyList())

    init {
        // Primera carga: inserta los productos de SeedData que aún no estén en la tabla
        alcance.launch {
            dao.insertarSiNoExisten(SeedData.productos.map { it.aEntidad() })
        }
    }

    override fun obtenerPorId(id: String): Product? =
        productos.value.firstOrNull { it.id == id }

    override fun guardar(producto: Product) {
        alcance.launch { dao.guardar(producto.aEntidad()) }
    }
}