package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import com.example.museo_de_los_museos_app.model.QrHistory


@Entity(tableName = "historial_qr", primaryKeys = ["userId", "contenido"])
data class HistorialQrEntity(
    val userId: String,
    val contenido: String,
    val valido: Boolean,
    val descripcion: String,
    val fechaMillis: Long
)

fun HistorialQrEntity.aModelo() = QrHistory(userId, contenido, valido, descripcion, fechaMillis)
fun QrHistory.aEntidad() = HistorialQrEntity(userId, contenido, valido, descripcion, fechaMillis)