package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.Museum


@Entity(tableName = "museos")
data class MuseoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val pais: String,
    val ciudad: String,
    val latitud: Double,
    val longitud: Double,
    val clasificacion: ClasificacionMuseo,
    val descripcion: String,
    val enlace: String,
    val proyecto: String
)

fun MuseoEntity.aModelo() = Museum(id, nombre, pais, ciudad, latitud, longitud, clasificacion, descripcion, enlace, proyecto)

fun Museum.aEntidad() = MuseoEntity(id, nombre, pais, ciudad, latitud, longitud, clasificacion, descripcion, enlace, proyecto)
