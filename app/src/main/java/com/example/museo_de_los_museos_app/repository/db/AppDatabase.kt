package com.example.museo_de_los_museos_app.repository.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.museo_de_los_museos_app.repository.SeedData

/** Base de datos SQLite de la app (archivo museo.db). */
@Database(
    entities = [ProductoEntity::class, MuseoEntity::class, UsuarioEntity::class,
                FavoritoEntity::class, HistorialQrEntity::class, CarritoEntity::class,
                PedidoEntity::class, PedidoItemEntity::class, PreferenciasEntity::class],
    version = 7, exportSchema = false)
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
                .addMigrations(MIGRACION_6_7)
                .fallbackToDestructiveMigration(true)
                .build()

        /**
         * 6 → 7: agrega imagen y publicado a la tabla museos sin borrar los datos de los usuarios
         * (registros, favoritos, carrito, pedidos). Los museos que ya existían quedan publicados.
         */
        val MIGRACION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE museos ADD COLUMN imagen TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE museos ADD COLUMN publicado INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE museos SET publicado = 1")
                SeedData.museos.forEach { museo ->
                    db.execSQL("UPDATE museos SET imagen = ? WHERE id = ?", arrayOf<Any>(museo.imagen, museo.id))
                }
            }
        }
    }
}