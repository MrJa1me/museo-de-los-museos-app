package com.example.museo_de_los_museos_app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.museo_de_los_museos_app.model.User
import kotlinx.coroutines.flow.first

/**
 * Contrato de acceso a los usuarios. Las pantallas dependen de esta interfaz:
 * si el profesor pide Room/SQLite, solo se cambia la implementación.
 */
interface UserRepository {
    suspend fun obtenerTodos(): List<User>
    suspend fun registrar(usuario: User)
    suspend fun actualizar(usuario: User)

    suspend fun buscarPorCorreo(correo: String): User? =
        obtenerTodos().firstOrNull { it.correo.equals(correo.trim(), ignoreCase = true) }
}

/**
 * Usuarios guardados en DataStore como JSON. La primera vez carga los usuarios de prueba de [SeedData];
 * los que se registran después se guardan junto a ellos y sobreviven al cerrar la app.
 */
class DataStoreUserRepository(context: Context) : UserRepository {

    private val almacen = context.applicationContext.dataStore
    private val claveUsuarios = stringPreferencesKey("usuarios_json")

    override suspend fun obtenerTodos(): List<User> {
        val json = almacen.data.first()[claveUsuarios]
        if (json != null) {
            return runCatching { usuariosDesdeJson(json) }.getOrElse { SeedData.usuarios }
        }
        almacen.edit { preferencias ->
            if (preferencias[claveUsuarios] == null) {
                preferencias[claveUsuarios] = SeedData.usuarios.aJson()
            }
        }
        return SeedData.usuarios
    }

    override suspend fun registrar(usuario: User) {
        almacen.edit { preferencias ->
            val actuales = leerLista(preferencias[claveUsuarios])
            preferencias[claveUsuarios] = (actuales + usuario).aJson()
        }
    }

    override suspend fun actualizar(usuario: User) {
        almacen.edit { preferencias ->
            val actuales = leerLista(preferencias[claveUsuarios])
            preferencias[claveUsuarios] = actuales.map { if (it.id == usuario.id) usuario else it }.aJson()
        }
    }

    private fun leerLista(json: String?): List<User> =
        if (json == null) SeedData.usuarios else runCatching { usuariosDesdeJson(json) }.getOrElse { SeedData.usuarios }
}
