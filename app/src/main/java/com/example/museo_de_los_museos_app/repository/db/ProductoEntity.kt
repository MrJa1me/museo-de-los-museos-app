package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product

/** Tabla "productos" de SQLite. Cada objeto es una fila. */
@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val categoria: CategoriaProducto,
    val precio: Int,
    val stock: Int,
    val pesoKg: Double?,
    val descripcion: String,
    val activo: Boolean
)

/** De fila de la tabla a modelo de la app. */
fun ProductoEntity.aModelo() = Product(id, nombre, categoria, precio, stock, pesoKg, descripcion, activo)

/** De modelo de la app a fila de la tabla. */
fun Product.aEntidad() = ProductoEntity(id, nombre, categoria, precio, stock, pesoKg, descripcion, activo)