package com.example.museo_de_los_museos_app.repository.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User


@Entity(tableName = "usuarios", indices = [Index(value = ["correo"], unique = true)])
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: Rol,
    val avatarUri: String?
)

fun UsuarioEntity.aModelo() = User(id, nombre, correo, contrasena, rol, avatarUri)

fun User.aEntidad() = UsuarioEntity(id, nombre, correo, contrasena, rol, avatarUri)
