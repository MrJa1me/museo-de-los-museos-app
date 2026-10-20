package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import com.example.museo_de_los_museos_app.model.CartItem


@Entity(tableName = "carrito", primaryKeys = ["userId", "productoId"])
data class CarritoEntity(
    val userId: String,
    val productoId: String,
    val cantidad: Int
)

fun CarritoEntity.aModelo() = CartItem(userId, productoId, cantidad)
fun CartItem.aEntidad() = CarritoEntity(userId, productoId, cantidad)
