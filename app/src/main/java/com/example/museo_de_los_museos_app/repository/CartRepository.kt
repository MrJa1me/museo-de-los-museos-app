package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.CartItem
import com.example.museo_de_los_museos_app.repository.db.CarritoDao
import com.example.museo_de_los_museos_app.repository.db.CarritoEntity
import com.example.museo_de_los_museos_app.repository.db.aModelo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Carrito de cada usuario (P4). Toda operación recibe el userId de SessionManager,
 * así los carritos de distintos usuarios no se mezclan en el mismo teléfono.
 */
interface CartRepository {
    /** Ítems del carrito del usuario. El Flow se actualiza solo al agregar, cambiar o quitar. */
    fun observar(userId: String): Flow<List<CartItem>>

    /** Suma [cantidad] a lo que ya había de ese producto (o lo agrega si no estaba). */
    suspend fun agregar(userId: String, productoId: String, cantidad: Int)

    /** Fija la cantidad de un producto. Con 0 o menos lo quita del carrito. */
    suspend fun cambiarCantidad(userId: String, productoId: String, cantidad: Int)

    suspend fun eliminar(userId: String, productoId: String)

    suspend fun vaciar(userId: String)
}

/** Carrito guardado en SQLite (tabla "carrito" de P1): persiste al cerrar la app. */
class RoomCartRepository(private val dao: CarritoDao) : CartRepository {

    override fun observar(userId: String): Flow<List<CartItem>> =
        dao.observar(userId).map { filas -> filas.map { it.aModelo() } }

    override suspend fun agregar(userId: String, productoId: String, cantidad: Int) {
        val actual = dao.observar(userId).first().firstOrNull { it.productoId == productoId }?.cantidad ?: 0
        dao.guardar(CarritoEntity(userId, productoId, actual + cantidad))
    }

    override suspend fun cambiarCantidad(userId: String, productoId: String, cantidad: Int) {
        if (cantidad <= 0) dao.eliminar(userId, productoId)
        else dao.guardar(CarritoEntity(userId, productoId, cantidad))
    }

    override suspend fun eliminar(userId: String, productoId: String) = dao.eliminar(userId, productoId)

    override suspend fun vaciar(userId: String) = dao.vaciar(userId)
}

/** Versión en memoria para pruebas unitarias (no persiste). */
class InMemoryCartRepository : CartRepository {
    private val items = MutableStateFlow<List<CartItem>>(emptyList())

    override fun observar(userId: String): Flow<List<CartItem>> =
        items.map { lista -> lista.filter { it.userId == userId } }

    override suspend fun agregar(userId: String, productoId: String, cantidad: Int) {
        val actual = items.value.firstOrNull { it.userId == userId && it.productoId == productoId }?.cantidad ?: 0
        cambiarCantidad(userId, productoId, actual + cantidad)
    }

    override suspend fun cambiarCantidad(userId: String, productoId: String, cantidad: Int) {
        items.update { lista ->
            val sinEste = lista.filterNot { it.userId == userId && it.productoId == productoId }
            if (cantidad <= 0) sinEste else sinEste + CartItem(userId, productoId, cantidad)
        }
    }

    override suspend fun eliminar(userId: String, productoId: String) = cambiarCantidad(userId, productoId, 0)

    override suspend fun vaciar(userId: String) {
        items.update { lista -> lista.filterNot { it.userId == userId } }
    }
}
