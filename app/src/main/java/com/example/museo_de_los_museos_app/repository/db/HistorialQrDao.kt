package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HistorialQrDao {


    @Query("SELECT * FROM historial_qr WHERE userId = :userId ORDER BY fechaMillis DESC")
    fun observar(userId: String): Flow<List<HistorialQrEntity>>

    @Upsert
    suspend fun guardar(item: HistorialQrEntity)


    @Query("""
        DELETE FROM historial_qr WHERE userId = :userId AND contenido NOT IN (
            SELECT contenido FROM historial_qr WHERE userId = :userId
            ORDER BY fechaMillis DESC LIMIT :maximo
        )
    """)
    suspend fun recortar(userId: String, maximo: Int)


    @Transaction
    suspend fun registrar(item: HistorialQrEntity, maximo: Int = 20) {
        guardar(item)
        recortar(item.userId, maximo)
    }
}