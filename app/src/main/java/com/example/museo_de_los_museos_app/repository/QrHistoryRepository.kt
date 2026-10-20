package com.example.museo_de_los_museos_app.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.museo_de_los_museos_app.model.QrHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/** Historial de códigos QR escaneados por cada usuario (P3). */
interface QrHistoryRepository {
    /** Escaneos del usuario, del más reciente al más antiguo. */
    fun observar(userId: String): Flow<List<QrHistory>>

    /** Guarda el escaneo sin duplicar: si el mismo código ya estaba, sube al primer lugar. */
    suspend fun registrar(item: QrHistory)
}

/** Historial en DataStore como JSON con la clave "historial_qr_<userId>". */
class DataStoreQrHistoryRepository(context: Context) : QrHistoryRepository {

    private val almacen = context.applicationContext.dataStore

    private fun clave(userId: String) = stringPreferencesKey("historial_qr_$userId")

    override fun observar(userId: String): Flow<List<QrHistory>> =
        almacen.data.map { preferencias -> historialDesdeJson(preferencias[clave(userId)]) }

    override suspend fun registrar(item: QrHistory) {
        almacen.edit { preferencias ->
            val actuales = historialDesdeJson(preferencias[clave(item.userId)])
            preferencias[clave(item.userId)] = actuales.agregarSinDuplicados(item).aJsonHistorial()
        }
    }
}

const val MAXIMO_HISTORIAL_QR = 20

/** Pone [item] al inicio, quita el escaneo anterior con el mismo contenido y limita el largo de la lista. */
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

/** Si el JSON está vacío o dañado devuelve una lista vacía, así la app nunca se cae por el historial. */
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
