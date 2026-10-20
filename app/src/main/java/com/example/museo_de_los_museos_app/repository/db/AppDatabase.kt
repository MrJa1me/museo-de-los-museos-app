package com.example.museo_de_los_museos_app.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de datos SQLite de la app (archivo museo.db). */
@Database(entities = [ProductoEntity::class, MuseoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun museoDao(): MuseoDao

    companion object {
        const val NOMBRE_BD = "museo.db"

        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NOMBRE_BD)
                .fallbackToDestructiveMigration(true)
                .build()
    }
}