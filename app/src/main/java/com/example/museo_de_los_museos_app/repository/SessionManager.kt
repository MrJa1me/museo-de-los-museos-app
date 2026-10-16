package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Usuario activo y su rol. Toda consulta de datos propios del usuario (favoritos, carrito,
 * historial, notificaciones, historial QR) se filtra por [userId].
 *
 * Versión inicial en memoria. P1 la conectará a DataStore para que la sesión persista.
 */
object SessionManager {
    private val _usuarioActual = MutableStateFlow<User?>(null)
    val usuarioActual: StateFlow<User?> = _usuarioActual.asStateFlow()

    val userId: String? get() = _usuarioActual.value?.id
    val rol: Rol? get() = _usuarioActual.value?.rol

    fun iniciarSesion(usuario: User) {
        _usuarioActual.value = usuario
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}
