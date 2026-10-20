package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {


    @Query("SELECT museoId FROM favoritos WHERE userId = :userId")
    fun observarIds(userId: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favoritos WHERE userId = :userId AND museoId = :museoId")
    suspend fun contar(userId: String, museoId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE userId = :userId AND museoId = :museoId")
    suspend fun eliminar(userId: String, museoId: String)


    @Transaction
    suspend fun alternar(userId: String, museoId: String) {
        if (contar(userId, museoId) > 0) eliminar(userId, museoId)
        else insertar(FavoritoEntity(userId, museoId))
    }
}
