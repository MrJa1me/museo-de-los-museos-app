package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.User
import com.example.museo_de_los_museos_app.repository.db.UsuarioDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo


class RoomUserRepository(private val dao: UsuarioDao) : UserRepository {

    private var semillaCargada = false


    private suspend fun asegurarDatosIniciales() {
        if (!semillaCargada) {
            dao.insertarSiNoExisten(SeedData.usuarios.map { it.aEntidad() })
            semillaCargada = true
        }
    }

    override suspend fun obtenerTodos(): List<User> {
        asegurarDatosIniciales()
        return dao.obtenerTodos().map { it.aModelo() }
    }

    override suspend fun buscarPorCorreo(correo: String): User? {
        asegurarDatosIniciales()
        return dao.buscarPorCorreo(correo.trim())?.aModelo()
    }

    override suspend fun registrar(usuario: User) {
        asegurarDatosIniciales()
        dao.insertar(usuario.aEntidad())
    }

    override suspend fun actualizar(usuario: User) {
        dao.actualizar(usuario.aEntidad())
    }
}
