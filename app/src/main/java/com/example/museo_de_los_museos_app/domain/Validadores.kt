package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product

/**
 * Tipos de error de validación. No tienen texto: la UI los traduce a strings.xml con
 * ErrorValidacion.mensaje() (ui/utils/MensajesValidacion.kt). Así esta clase no depende de Android.
 */
enum class ErrorValidacion {
    CAMPO_VACIO,
    TEXTO_MUY_CORTO,
    SOLO_LETRAS,
    NUMERO_INVALIDO,
    CANTIDAD_MINIMA,
    PRODUCTO_AGOTADO,
    SUPERA_STOCK,
    PRECIO_INVALIDO,
    STOCK_INVALIDO,
    PESO_INVALIDO,
    PESO_MAXIMO,
    TELEFONO_INVALIDO,
    REGION_INVALIDA,
    PAIS_NO_DISPONIBLE
}

/**
 * Validadores de P4 (catálogo, compra, destino de envío y formulario de producto del Administrador).
 * Cada función devuelve null si el valor es válido, o el error correspondiente.
 * Los ViewModels los llaman desde validarFormulario(); nunca se validan datos dentro de un Composable.
 */
object Validadores {

    const val LARGO_MINIMO_NOMBRE = 3
    const val LARGO_MINIMO_DIRECCION = 5
    const val LARGO_MINIMO_CIUDAD = 2
    const val DIGITOS_MINIMOS_TELEFONO = 8
    const val DIGITOS_MAXIMOS_TELEFONO = 15
    const val PESO_MAXIMO_KG = 30.0

    private val regexSoloLetras = Regex("^[\\p{L} '.-]+$")

    // ---------- Compra (Detalle de producto / Carrito) ----------

    /**
     * Cantidad que el usuario quiere agregar. [enCarrito] es lo que ya tiene de ese producto,
     * para no permitir superar el stock sumando varias veces.
     */
    fun validarCantidad(texto: String, producto: Product, enCarrito: Int = 0): ErrorValidacion? {
        if (!producto.disponible) return ErrorValidacion.PRODUCTO_AGOTADO
        if (texto.isBlank()) return ErrorValidacion.CAMPO_VACIO
        val cantidad = texto.trim().toIntOrNull() ?: return ErrorValidacion.NUMERO_INVALIDO
        if (cantidad < 1) return ErrorValidacion.CANTIDAD_MINIMA
        if (cantidad + enCarrito > producto.stock) return ErrorValidacion.SUPERA_STOCK
        return null
    }

    // ---------- Formulario de destino de envío (Carrito) ----------

    fun validarNombreDestinatario(texto: String): ErrorValidacion? {
        val valor = texto.trim()
        if (valor.isEmpty()) return ErrorValidacion.CAMPO_VACIO
        if (valor.length < LARGO_MINIMO_NOMBRE) return ErrorValidacion.TEXTO_MUY_CORTO
        if (!regexSoloLetras.matches(valor)) return ErrorValidacion.SOLO_LETRAS
        return null
    }

    fun validarDireccion(texto: String): ErrorValidacion? {
        val valor = texto.trim()
        if (valor.isEmpty()) return ErrorValidacion.CAMPO_VACIO
        if (valor.length < LARGO_MINIMO_DIRECCION) return ErrorValidacion.TEXTO_MUY_CORTO
        return null
    }

    fun validarCiudad(texto: String): ErrorValidacion? {
        val valor = texto.trim()
        if (valor.isEmpty()) return ErrorValidacion.CAMPO_VACIO
        if (valor.length < LARGO_MINIMO_CIUDAD) return ErrorValidacion.TEXTO_MUY_CORTO
        if (!regexSoloLetras.matches(valor)) return ErrorValidacion.SOLO_LETRAS
        return null
    }

    /** Acepta espacios y un + inicial (ej. "+56 9 1234 5678"); cuenta solo los dígitos. */
    fun validarTelefono(texto: String): ErrorValidacion? {
        val valor = texto.trim().replace(" ", "")
        if (valor.isEmpty()) return ErrorValidacion.CAMPO_VACIO
        val digitos = valor.removePrefix("+")
        if (!digitos.all { it.isDigit() }) return ErrorValidacion.TELEFONO_INVALIDO
        if (digitos.length !in DIGITOS_MINIMOS_TELEFONO..DIGITOS_MAXIMOS_TELEFONO) return ErrorValidacion.TELEFONO_INVALIDO
        return null
    }

    /** Envío nacional: la región debe ser una de [RegionesChile.todas]. */
    fun validarRegion(region: String): ErrorValidacion? {
        if (region.isBlank()) return ErrorValidacion.CAMPO_VACIO
        if (region !in RegionesChile.todas) return ErrorValidacion.REGION_INVALIDA
        return null
    }

    /** Envío internacional: el país debe pertenecer a alguna [ZonaInternacional]. */
    fun validarPais(pais: String): ErrorValidacion? {
        if (pais.isBlank()) return ErrorValidacion.CAMPO_VACIO
        if (ZonaInternacional.desdePais(pais) == null) return ErrorValidacion.PAIS_NO_DISPONIBLE
        return null
    }

    // ---------- Formulario de producto (Administrador en Catálogo) ----------

    /** Nombre obligatorio, de al menos [LARGO_MINIMO_NOMBRE] letras. */
    fun validarNombreProducto(texto: String): ErrorValidacion? {
        val valor = texto.trim()
        if (valor.isEmpty()) return ErrorValidacion.CAMPO_VACIO
        if (valor.length < LARGO_MINIMO_NOMBRE) return ErrorValidacion.TEXTO_MUY_CORTO
        return null
    }

    fun validarPrecio(texto: String): ErrorValidacion? {
        if (texto.isBlank()) return ErrorValidacion.CAMPO_VACIO
        val precio = texto.trim().toIntOrNull() ?: return ErrorValidacion.NUMERO_INVALIDO
        if (precio <= 0) return ErrorValidacion.PRECIO_INVALIDO
        return null
    }

    /** Stock o cupos: puede ser 0 (agotado), pero no negativo. */
    fun validarStock(texto: String): ErrorValidacion? {
        if (texto.isBlank()) return ErrorValidacion.CAMPO_VACIO
        val stock = texto.trim().toIntOrNull() ?: return ErrorValidacion.NUMERO_INVALIDO
        if (stock < 0) return ErrorValidacion.STOCK_INVALIDO
        return null
    }

    /** Los cursos no tienen peso (sin envío). Para el resto acepta decimales con punto o coma. */
    fun validarPeso(texto: String, categoria: CategoriaProducto): ErrorValidacion? {
        if (categoria == CategoriaProducto.CURSO) return null
        if (texto.isBlank()) return ErrorValidacion.CAMPO_VACIO
        val peso = convertirDecimal(texto) ?: return ErrorValidacion.NUMERO_INVALIDO
        if (peso <= 0.0) return ErrorValidacion.PESO_INVALIDO
        if (peso > PESO_MAXIMO_KG) return ErrorValidacion.PESO_MAXIMO
        return null
    }

    /** "0,5" o "0.5" → 0.5. Devuelve null si no es un número. */
    fun convertirDecimal(texto: String): Double? =
        texto.trim().replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() } // descarta "NaN" e "Infinity"
}
