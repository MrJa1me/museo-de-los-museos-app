package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Consultas permitidas sobre la tabla "productos". */
@Dao
interface ProductoDao {

    /** Todos los productos. El Flow avisa solo cada vez que la tabla cambia. */
    @Query("SELECT * FROM productos ORDER BY id ASC")
    fun observarTodos(): Flow<List<ProductoEntity>>

    /** Carga inicial: si el id ya existe, lo ignora. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(productos: List<ProductoEntity>)

    /** Crea el producto si no existe, o lo actualiza si ya existe (alta/edición del Administrador). */
    @Upsert
    suspend fun guardar(producto: ProductoEntity)
}