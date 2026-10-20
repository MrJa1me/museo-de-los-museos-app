package com.example.museo_de_los_museos_app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Usuario activo y su rol. Toda consulta de datos propios del usuario (favoritos, carrito,
 * historial, notificaciones, historial QR) se filtra por [userId].
 *
 * La sesión se guarda en DataStore ("sesion_user_id"), así que sigue activa al cerrar y abrir la app.
 * MainActivity llama a [init] y [restaurar] al iniciar.
 */
object SessionManager {
    private val _usuarioActual = MutableStateFlow<User?>(null)
    val usuarioActual: StateFlow<User?> = _usuarioActual.asStateFlow()

    val userId: String? get() = _usuarioActual.value?.id
    val rol: Rol? get() = _usuarioActual.value?.rol

    private val claveSesion = stringPreferencesKey("sesion_user_id")
    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var contexto: Context? = null

    fun init(context: Context) {
        contexto = context.applicationContext
    }

    /** Recupera la sesión guardada (si existe) buscando el usuario por su id. */
    fun restaurar() {
        val ctx = contexto ?: return
        alcance.launch {
            val id = ctx.dataStore.data.first()[claveSesion] ?: return@launch
            _usuarioActual.value = Repositorios.usuarios.obtenerTodos().firstOrNull { it.id == id }
        }
    }

    fun iniciarSesion(usuario: User) {
        _usuarioActual.value = usuario
        guardarId(usuario.id)
    }

    fun cerrarSesion() {
        _usuarioActual.value = null
        guardarId(null)
    }

    /** Actualiza en memoria el usuario activo (por ejemplo tras editar el perfil). */
    fun actualizarUsuarioActual(usuario: User) {
        if (_usuarioActual.value?.id == usuario.id) _usuarioActual.value = usuario
    }

    private fun guardarId(id: String?) {
        val ctx = contexto ?: return
        alcance.launch {
            ctx.dataStore.edit { preferencias ->
                if (id == null) preferencias.remove(claveSesion) else preferencias[claveSesion] = id
            }
        }
    }
}
