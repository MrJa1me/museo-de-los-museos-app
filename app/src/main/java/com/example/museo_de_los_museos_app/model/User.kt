package com.example.museo_de_los_museos_app.model

data class User(
    val id: String,
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val rol: Rol,
    val avatarUri: String? = null
)
