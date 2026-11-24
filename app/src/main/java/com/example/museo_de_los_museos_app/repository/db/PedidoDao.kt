package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.museo_de_los_museos_app.model.EstadoPedido
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Query("SELECT * FROM pedidos ORDER BY fechaMillis DESC")
    fun observarPedidos(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedido_items")
    fun observarItems(): Flow<List<PedidoItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarPedidos(pedidos: List<PedidoEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarItems(items: List<PedidoItemEntity>)


    @Transaction
    suspend fun insertarConItems(pedidos: List<PedidoEntity>, items: List<PedidoItemEntity>) {
        insertarPedidos(pedidos)
        insertarItems(items)
    }

    @Query("UPDATE pedidos SET estado = :estado WHERE id = :id")
    suspend fun cambiarEstado(id: String, estado: EstadoPedido)

    @Query("SELECT COUNT(*) FROM pedidos")
    suspend fun contar(): Int

    @Query("DELETE FROM pedido_items WHERE pedidoId = :pedidoId")
    suspend fun borrarItemsDe(pedidoId: String)

    @Query("DELETE FROM pedidos WHERE id = :id")
    suspend fun borrarPedido(id: String)

    @Query("DELETE FROM pedido_items")
    suspend fun borrarTodosLosItems()

    @Query("DELETE FROM pedidos")
    suspend fun borrarTodosLosPedidos()

    /** Upsert (y no REPLACE) para no borrar en cascada los ítems del pedido al actualizarlo. */
    @Upsert
    suspend fun guardarPedido(pedido: PedidoEntity)

    /** Guarda un pedido que respondió el microservicio, con sus ítems actualizados. */
    @Transaction
    suspend fun guardarConItems(pedido: PedidoEntity, items: List<PedidoItemEntity>) {
        guardarPedido(pedido)
        borrarItemsDe(pedido.id)
        insertarItems(items)
    }

    @Transaction
    suspend fun eliminar(id: String) {
        borrarItemsDe(id)
        borrarPedido(id)
    }

    /** Sincronización: la copia local queda igual a lo que tiene el microservicio. */
    @Transaction
    suspend fun reemplazarTodos(pedidos: List<PedidoEntity>, items: List<PedidoItemEntity>) {
        borrarTodosLosItems()
        borrarTodosLosPedidos()
        insertarPedidos(pedidos)
        insertarItems(items)
    }
}
