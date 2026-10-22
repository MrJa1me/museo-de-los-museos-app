package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenciasDao {

    @Query("SELECT * FROM preferencias WHERE userId = :userId LIMIT 1")
    suspend fun obtener(userId: String): PreferenciasEntity?

    /** Igual que obtener(), pero avisa cada vez que la fila cambia (lo usa el tema para el texto grande). */
    @Query("SELECT * FROM preferencias WHERE userId = :userId LIMIT 1")
    fun observar(userId: String): Flow<PreferenciasEntity?>

    /** REPLACE: si la fila no existe la crea; si existe, la reemplaza. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(preferencias: PreferenciasEntity)
}
