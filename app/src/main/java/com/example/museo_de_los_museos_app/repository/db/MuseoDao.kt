package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface MuseoDao {

    @Query("SELECT * FROM museos ORDER BY nombre ASC")
    fun observarTodos(): Flow<List<MuseoEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(museos: List<MuseoEntity>)


    @Upsert
    suspend fun guardar(museo: MuseoEntity)

    /** El Administrador aprueba una ficha: desde ahora la ven también los clientes. */
    @Query("UPDATE museos SET publicado = 1 WHERE id = :id")
    suspend fun aprobar(id: String)
}
