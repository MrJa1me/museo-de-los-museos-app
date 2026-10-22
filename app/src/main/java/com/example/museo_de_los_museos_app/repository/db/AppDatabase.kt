package com.example.museo_de_los_museos_app.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos SQLite de la app (archivo museo.db). */
@Database(
    entities = [ProductoEntity::class, MuseoEntity::class, UsuarioEntity::class,
                FavoritoEntity::class, HistorialQrEntity::class, CarritoEntity::class,
                PedidoEntity::class, PedidoItemEntity::class, PreferenciasEntity::class],
    version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun museoDao(): MuseoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun historialQrDao(): HistorialQrDao
    abstract fun carritoDao(): CarritoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun preferenciasDao(): PreferenciasDao

    companion object {
        const val NOMBRE_BD = "museo.db"

        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NOMBRE_BD)
                .fallbackToDestructiveMigration(true)
                .build()
    }
}