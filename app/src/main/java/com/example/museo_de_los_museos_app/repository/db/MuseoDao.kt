package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT COUNT(*) FROM museos")
    suspend fun contar(): Int

    @Query("DELETE FROM museos WHERE id = :id")
    suspend fun eliminar(id: String)

    @Query("DELETE FROM museos")
    suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(museos: List<MuseoEntity>)

    /** Sincronización: la copia local queda igual a lo que tiene el microservicio (en una sola transacción). */
    @Transaction
    suspend fun reemplazarTodos(museos: List<MuseoEntity>) {
        borrarTodos()
        insertarTodos(museos)
    }
}
