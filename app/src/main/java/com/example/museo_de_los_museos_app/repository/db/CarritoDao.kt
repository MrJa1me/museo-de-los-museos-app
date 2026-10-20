package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito WHERE userId = :userId")
    fun observar(userId: String): Flow<List<CarritoEntity>>


    @Upsert
    suspend fun guardar(item: CarritoEntity)

    @Query("DELETE FROM carrito WHERE userId = :userId AND productoId = :productoId")
    suspend fun eliminar(userId: String, productoId: String)


    @Query("DELETE FROM carrito WHERE userId = :userId")
    suspend fun vaciar(userId: String)
}
