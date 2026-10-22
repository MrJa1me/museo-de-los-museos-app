package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.museo_de_los_museos_app.model.Preferencias

/** Tabla "preferencias": una fila por usuario. */
@Entity(tableName = "preferencias")
data class PreferenciasEntity(
    @PrimaryKey val userId: String,
    val notificaciones: Boolean,
    val textoGrande: Boolean,
    val idioma: String,
    val opcionRol: Boolean
)

fun PreferenciasEntity.aModelo() = Preferencias(userId, notificaciones, textoGrande, idioma, opcionRol)

fun Preferencias.aEntidad() = PreferenciasEntity(userId, notificaciones, textoGrande, idioma, opcionRol)
