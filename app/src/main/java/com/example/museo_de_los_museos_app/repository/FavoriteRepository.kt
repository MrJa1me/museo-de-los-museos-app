package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.repository.db.FavoritoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


interface FavoriteRepository {
    fun observar(userId: String): Flow<Set<String>>

    suspend fun alternar(userId: String, museoId: String)
}



class RoomFavoriteRepository(private val dao: FavoritoDao) : FavoriteRepository {

    // La consulta devuelve una lista, se convierte a Set
    override fun observar(userId: String): Flow<Set<String>> =
        dao.observarIds(userId).map { it.toSet() }

    override suspend fun alternar(userId: String, museoId: String) =
        dao.alternar(userId, museoId)
}
