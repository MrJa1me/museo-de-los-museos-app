package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Product
import org.json.JSONException
import org.json.JSONObject

/** Motivos por los que un QR no se puede usar. La UI los traduce a strings_museos.xml (MensajesErrores.kt). */
enum class MotivoQrInvalido {
    VACIO,
    FORMATO_DESCONOCIDO,
    TIPO_DESCONOCIDO,
    SIN_ID,
    ENLACE_NO_PERMITIDO,
    NO_ENCONTRADO
}

/** Lo que dice el texto del QR, antes de buscarlo en los datos locales. */
sealed interface ContenidoQr {
    data class Museo(val id: String) : ContenidoQr
    data class Producto(val id: String) : ContenidoQr
    data class Enlace(val url: String) : ContenidoQr
    data class Invalido(val motivo: MotivoQrInvalido) : ContenidoQr
}

/** Resultado final del escaneo, ya buscado en los repositorios. */
sealed interface ResultadoQr {
    data class MuseoEncontrado(val museo: Museum) : ResultadoQr
    data class ProductoEncontrado(val producto: Product) : ResultadoQr
    data class EnlaceValido(val url: String) : ResultadoQr
    data class Error(val motivo: MotivoQrInvalido) : ResultadoQr
}

/**
 * Lectura de los QR de la app (P3). No depende de la UI ni de la cámara, así se prueba en el PC.
 *
 * Formatos aceptados:
 *  - JSON con tipo e id: {"tipo":"museo","id":"m01"} o {"tipo":"producto","id":"p01"}
 *  - Enlaces que empiezan con http:// o https:// (cualquier otro esquema se rechaza por seguridad)
 */
object LectorQr {

    const val TIPO_MUSEO = "museo"
    const val TIPO_PRODUCTO = "producto"

    /** Códigos de ejemplo para probar sin cámara (por ejemplo en el emulador). */
    const val EJEMPLO_MUSEO = "{\"tipo\":\"museo\",\"id\":\"m04\"}"

    /** Algo como "ftp:", "javascript:" o "intent:" al inicio del texto. */
    private val regexEsquema = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

    fun interpretar(texto: String?): ContenidoQr {
        val valor = texto?.trim().orEmpty()
        if (valor.isEmpty()) return ContenidoQr.Invalido(MotivoQrInvalido.VACIO)

        if (valor.startsWith("{")) return interpretarJson(valor)

        val minusculas = valor.lowercase()
        if (minusculas.startsWith("http://") || minusculas.startsWith("https://")) {
            val resto = valor.substringAfter("://")
            return if (resto.isNotBlank() && valor.none { it.isWhitespace() }) {
                ContenidoQr.Enlace(valor)
            } else {
                ContenidoQr.Invalido(MotivoQrInvalido.FORMATO_DESCONOCIDO)
            }
        }
        if (regexEsquema.containsMatchIn(valor)) return ContenidoQr.Invalido(MotivoQrInvalido.ENLACE_NO_PERMITIDO)
        return ContenidoQr.Invalido(MotivoQrInvalido.FORMATO_DESCONOCIDO)
    }

    private fun interpretarJson(valor: String): ContenidoQr {
        val objeto = try {
            JSONObject(valor)
        } catch (e: JSONException) {
            return ContenidoQr.Invalido(MotivoQrInvalido.FORMATO_DESCONOCIDO)
        }
        val tipo = objeto.optString("tipo").trim().lowercase()
        val id = objeto.optString("id").trim().lowercase()
        if (tipo != TIPO_MUSEO && tipo != TIPO_PRODUCTO) return ContenidoQr.Invalido(MotivoQrInvalido.TIPO_DESCONOCIDO)
        if (id.isEmpty()) return ContenidoQr.Invalido(MotivoQrInvalido.SIN_ID)
        return if (tipo == TIPO_MUSEO) ContenidoQr.Museo(id) else ContenidoQr.Producto(id)
    }

    /** Interpreta el texto y busca el museo o producto en los datos locales. */
    fun resolver(
        texto: String?,
        buscarMuseo: (String) -> Museum?,
        buscarProducto: (String) -> Product?
    ): ResultadoQr = when (val contenido = interpretar(texto)) {
        is ContenidoQr.Museo -> buscarMuseo(contenido.id)?.let { ResultadoQr.MuseoEncontrado(it) }
            ?: ResultadoQr.Error(MotivoQrInvalido.NO_ENCONTRADO)
        is ContenidoQr.Producto -> buscarProducto(contenido.id)?.let { ResultadoQr.ProductoEncontrado(it) }
            ?: ResultadoQr.Error(MotivoQrInvalido.NO_ENCONTRADO)
        is ContenidoQr.Enlace -> ResultadoQr.EnlaceValido(contenido.url)
        is ContenidoQr.Invalido -> ResultadoQr.Error(contenido.motivo)
    }
}
