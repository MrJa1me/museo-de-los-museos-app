package com.example.museo_de_los_museos_app.repository


import com.example.museo_de_los_museos_app.model.User



interface UserRepository {
    suspend fun obtenerTodos(): List<User>
    suspend fun registrar(usuario: User)
    suspend fun actualizar(usuario: User)

    suspend fun buscarPorCorreo(correo: String): User? =
        obtenerTodos().firstOrNull { it.correo.equals(correo.trim(), ignoreCase = true) }
}
