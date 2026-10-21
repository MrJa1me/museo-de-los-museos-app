package com.example.museo_de_los_museos_app.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * DataStore de la app. Solo guarda la sesión (clave "sesion_user_id") en SessionManager.
 * Todos los demás datos (usuarios, museos, productos, favoritos, QR, carrito, pedidos) están en SQLite (Room).
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "museo_preferencias")
