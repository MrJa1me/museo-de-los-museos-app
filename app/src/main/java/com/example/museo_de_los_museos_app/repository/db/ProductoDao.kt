package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Consultas permitidas sobre la tabla "productos" (copia local del catálogo de ms-productos). */
@Dao
interface ProductoDao {

    /** Todos los productos. El Flow avisa solo cada vez que la tabla cambia. */
    @Query("SELECT * FROM productos ORDER BY id ASC")
    fun observarTodos(): Flow<List<ProductoEntity>>

    /** Carga inicial: si el id ya existe, lo ignora. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarSiNoExisten(productos: List<ProductoEntity>)

    /** Crea el producto si no existe, o lo actualiza si ya existe (respuesta del servidor). */
    @Upsert
    suspend fun guardar(producto: ProductoEntity)

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int

    @Query("DELETE FROM productos")
    suspend fun borrarTodos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<ProductoEntity>)

    /** Sincronización: la copia local queda igual a lo que tiene el microservicio (en una sola transacción). */
    @Transaction
    suspend fun reemplazarTodos(productos: List<ProductoEntity>) {
        borrarTodos()
        insertarTodos(productos)
    }
}
