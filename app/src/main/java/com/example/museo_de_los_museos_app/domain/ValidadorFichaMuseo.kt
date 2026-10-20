package com.example.museo_de_los_museos_app.domain

/**
 * Errores del formulario de ficha de museo (Investigador). Sin texto, igual que ErrorValidacion:
 * la UI los traduce con ErrorFicha.mensaje() (ui/utils/MensajesP3.kt).
 */
enum class ErrorFicha {
    OBLIGATORIO,
    TEXTO_CORTO,
    SOLO_LETRAS,
    NUMERO_INVALIDO,
    LATITUD_FUERA_DE_RANGO,
    LONGITUD_FUERA_DE_RANGO,
    DESCRIPCION_CORTA,
    ENLACE_INVALIDO
}

/**
 * Validadores de la ficha de museo virtual (P3). Cada función devuelve null si el valor es válido.
 * Los llama DetalleMuseoViewModel; nunca se valida dentro de un Composable.
 */
object ValidadorFichaMuseo {

    const val LARGO_MINIMO_NOMBRE = 5
    const val LARGO_MINIMO_LUGAR = 2
    const val LARGO_MINIMO_DESCRIPCION = 20

    private val regexSoloLetras = Regex("^[\\p{L} '.-]+$")
    private val regexIdMuseo = Regex("^m(\\d+)$")

    fun nombre(texto: String): ErrorFicha? {
        val valor = texto.trim()
        if (valor.isEmpty()) return ErrorFicha.OBLIGATORIO
        if (valor.length < LARGO_MINIMO_NOMBRE) return ErrorFicha.TEXTO_CORTO
        return null
    }

    /** País y ciudad: solo letras, espacios y signos simples (ej. "São Paulo", "Ciudad de México"). */
    fun lugar(texto: String): ErrorFicha? {
        val valor = texto.trim()
        if (valor.isEmpty()) return ErrorFicha.OBLIGATORIO
        if (valor.length < LARGO_MINIMO_LUGAR) return ErrorFicha.TEXTO_CORTO
        if (!regexSoloLetras.matches(valor)) return ErrorFicha.SOLO_LETRAS
        return null
    }

    fun latitud(texto: String): ErrorFicha? {
        if (texto.isBlank()) return ErrorFicha.OBLIGATORIO
        val valor = convertirDecimal(texto) ?: return ErrorFicha.NUMERO_INVALIDO
        if (valor !in -90.0..90.0) return ErrorFicha.LATITUD_FUERA_DE_RANGO
        return null
    }

    fun longitud(texto: String): ErrorFicha? {
        if (texto.isBlank()) return ErrorFicha.OBLIGATORIO
        val valor = convertirDecimal(texto) ?: return ErrorFicha.NUMERO_INVALIDO
        if (valor !in -180.0..180.0) return ErrorFicha.LONGITUD_FUERA_DE_RANGO
        return null
    }

    fun descripcion(texto: String): ErrorFicha? {
        val valor = texto.trim()
        if (valor.isEmpty()) return ErrorFicha.OBLIGATORIO
        if (valor.length < LARGO_MINIMO_DESCRIPCION) return ErrorFicha.DESCRIPCION_CORTA
        return null
    }

    /** El recorrido virtual debe ser un enlace http:// o https:// (misma regla que los QR). */
    fun enlace(texto: String): ErrorFicha? {
        if (texto.isBlank()) return ErrorFicha.OBLIGATORIO
        if (LectorQr.interpretar(texto) !is ContenidoQr.Enlace) return ErrorFicha.ENLACE_INVALIDO
        return null
    }

    /** "-33,4489" o "-33.4489" → -33.4489. Devuelve null si no es un número. */
    fun convertirDecimal(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }

    /** Siguiente id libre con el formato de SeedData: m01…m10 → "m11". */
    fun siguienteId(idsExistentes: Collection<String>): String {
        val mayor = idsExistentes.mapNotNull { regexIdMuseo.matchEntire(it)?.groupValues?.get(1)?.toIntOrNull() }
            .maxOrNull() ?: 0
        return "m" + (mayor + 1).toString().padStart(2, '0')
    }
}
