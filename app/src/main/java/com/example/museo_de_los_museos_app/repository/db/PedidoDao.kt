package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
}
