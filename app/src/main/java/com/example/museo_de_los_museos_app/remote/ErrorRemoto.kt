package com.example.museo_de_los_museos_app.remote

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

enum class TipoErrorRemoto {
    /** El microservicio está apagado, no hay red o la IP es incorrecta. */
    SIN_CONEXION,
    /** El servidor rechazó la operación (400/404/409) y explicó por qué. */
    RECHAZADO,
    /** Error interno del servidor (5xx). */
    SERVIDOR,
    DESCONOCIDO
}

/**
 * Error de una llamada remota, sin textos de interfaz: la pantalla muestra [mensajeServidor] si existe
 * o un texto de strings.xml según el [tipo] (ver ui/utils/MensajesRemotos.kt).
 */
class ErrorRemoto(
    val tipo: TipoErrorRemoto,
    val mensajeServidor: String? = null,
    val codigo: Int? = null
) : Exception(mensajeServidor ?: tipo.name)

/** Traduce cualquier excepción de Retrofit/OkHttp a un ErrorRemoto. */
fun Throwable.aErrorRemoto(): ErrorRemoto = when (this) {
    is ErrorRemoto -> this
    is HttpException -> {
        val mensaje = runCatching { mensajeDelCuerpo(response()?.errorBody()?.string()) }.getOrNull()
        ErrorRemoto(if (code() >= 500) TipoErrorRemoto.SERVIDOR else TipoErrorRemoto.RECHAZADO, mensaje, code())
    }
    is IOException -> ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)
    else -> ErrorRemoto(TipoErrorRemoto.DESCONOCIDO, message)
}

/**
 * Lee el JSON de error de los microservicios (GlobalExceptionHandler):
 * {"mensaje":"Datos inválidos","campos":{"enlace":"El enlace debe empezar con http://"}}
 * Si hay errores por campo, se muestra el primero porque es más útil que el mensaje general.
 */
internal fun mensajeDelCuerpo(cuerpo: String?): String? {
    if (cuerpo.isNullOrBlank()) return null
    val json = JSONObject(cuerpo)
    val campos = json.optJSONObject("campos")
    if (campos != null && campos.length() > 0) {
        val primero = campos.keys().next()
        return campos.optString(primero).takeIf { it.isNotBlank() }
    }
    return json.optString("mensaje").takeIf { it.isNotBlank() }
}

/** Ejecuta una llamada remota y convierte cualquier excepción en Result.failure(ErrorRemoto). */
internal suspend fun <T> llamar(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (e: kotlinx.coroutines.CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.aErrorRemoto())
    }
