package com.example.museo_de_los_museos_app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Museos favoritos de cada usuario (P3). Las pantallas dependen de esta interfaz:
 * cuando P1 agregue la tabla de favoritos en Room, solo se cambia la implementación.
 */
interface FavoriteRepository {
    /** Ids de los museos favoritos del usuario. El Flow se actualiza solo al agregar o quitar. */
    fun observar(userId: String): Flow<Set<String>>

    /** Agrega el museo si no estaba en favoritos, o lo quita si ya estaba. */
    suspend fun alternar(userId: String, museoId: String)
}

/** Favoritos en DataStore con la clave "favoritos_<userId>", como indica AppDataStore.kt. */
class DataStoreFavoriteRepository(context: Context) : FavoriteRepository {

    private val almacen = context.applicationContext.dataStore

    private fun clave(userId: String) = stringSetPreferencesKey("favoritos_$userId")

    override fun observar(userId: String): Flow<Set<String>> =
        almacen.data.map { it[clave(userId)] ?: emptySet() }

    override suspend fun alternar(userId: String, museoId: String) {
        almacen.edit { preferencias ->
            val actuales = preferencias[clave(userId)] ?: emptySet()
            preferencias[clave(userId)] = if (museoId in actuales) actuales - museoId else actuales + museoId
        }
    }
}
