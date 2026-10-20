package com.example.museo_de_los_museos_app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore único de la app (persistencia local, como enseñó el profesor).
 *
 * Todos lo usan así, con claves PREFIJADAS POR USUARIO para que los datos no se mezclen:
 *   val clave = stringSetPreferencesKey("favoritos_" + userId)
 *   context.dataStore.edit { it[clave] = setOf("m01", "m04") }
 *   context.dataStore.data.map { it[clave] ?: emptySet() }   // Flow que se actualiza solo
 *
 * Claves ya usadas por P1: "usuarios_json" y "sesion_user_id".
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "museo_preferencias")
