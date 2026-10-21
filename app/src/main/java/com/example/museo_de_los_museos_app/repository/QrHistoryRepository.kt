package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.QrHistory
import com.example.museo_de_los_museos_app.repository.db.HistorialQrDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject


interface QrHistoryRepository {
    fun observar(userId: String): Flow<List<QrHistory>>

    suspend fun registrar(item: QrHistory)
}


class RoomQrHistoryRepository(private val dao: HistorialQrDao) : QrHistoryRepository {

    // Entity a modelo
    override fun observar(userId: String): Flow<List<QrHistory>> =
        dao.observar(userId).map { filas -> filas.map { it.aModelo() } }

    override suspend fun registrar(item: QrHistory) =
        dao.registrar(item.aEntidad(), MAXIMO_HISTORIAL_QR)
}

const val MAXIMO_HISTORIAL_QR = 20

internal fun List<QrHistory>.agregarSinDuplicados(item: QrHistory, maximo: Int = MAXIMO_HISTORIAL_QR): List<QrHistory> =
    (listOf(item) + filterNot { it.contenido == item.contenido }).take(maximo)

internal fun List<QrHistory>.aJsonHistorial(): String {
    val arreglo = JSONArray()
    forEach { escaneo ->
        val objeto = JSONObject()
        objeto.put("userId", escaneo.userId)
        objeto.put("contenido", escaneo.contenido)
        objeto.put("valido", escaneo.valido)
        objeto.put("descripcion", escaneo.descripcion)
        objeto.put("fechaMillis", escaneo.fechaMillis)
        arreglo.put(objeto)
    }
    return arreglo.toString()
}

internal fun historialDesdeJson(json: String?): List<QrHistory> {
    if (json.isNullOrBlank()) return emptyList()
    return runCatching {
        val arreglo = JSONArray(json)
        List(arreglo.length()) { indice ->
            val objeto = arreglo.getJSONObject(indice)
            QrHistory(
                userId = objeto.getString("userId"),
                contenido = objeto.getString("contenido"),
                valido = objeto.getBoolean("valido"),
                descripcion = objeto.optString("descripcion"),
                fechaMillis = objeto.getLong("fechaMillis")
            )
        }
    }.getOrElse { emptyList() }
}
