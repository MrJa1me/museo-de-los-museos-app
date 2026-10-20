package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import com.example.museo_de_los_museos_app.model.Favorite


@Entity(tableName = "favoritos", primaryKeys = ["userId", "museoId"])
data class FavoritoEntity(
    val userId: String,
    val museoId: String
)

fun FavoritoEntity.aModelo() = Favorite(userId, museoId)
fun Favorite.aEntidad() = FavoritoEntity(userId, museoId)
