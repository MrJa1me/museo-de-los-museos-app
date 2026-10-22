package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.repository.db.PreferenciasDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PreferenciasRepository {
    /** Preferencias del usuario; si no tiene fila, devuelve los valores por defecto. */
    suspend fun obtener(userId: String): Preferencias
    fun observar(userId: String): Flow<Preferencias>
    suspend fun guardar(preferencias: Preferencias)
}

class RoomPreferenciasRepository(private val dao: PreferenciasDao) : PreferenciasRepository {

    override suspend fun obtener(userId: String): Preferencias =
        dao.obtener(userId)?.aModelo() ?: Preferencias(userId)

    override fun observar(userId: String): Flow<Preferencias> =
        dao.observar(userId).map { it?.aModelo() ?: Preferencias(userId) }

    override suspend fun guardar(preferencias: Preferencias) {
        dao.guardar(preferencias.aEntidad())
    }
}

// Acceso como el resto:  Repositorios.preferencias   (mismo patrón que RepositorioUsuarios.kt)
private object AlmacenPreferencias {
    lateinit var repositorio: PreferenciasRepository
}

val Repositorios.preferencias: PreferenciasRepository
    get() = AlmacenPreferencias.repositorio

/** MainActivity lo llama después de inicializarBaseDatos(). */
fun Repositorios.inicializarPreferencias() {
    AlmacenPreferencias.repositorio = RoomPreferenciasRepository(baseDatos.preferenciasDao())
}
