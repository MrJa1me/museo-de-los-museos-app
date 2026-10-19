package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.Product

/** Un producto del carrito con su cantidad (el ViewModel del carrito une CartItem con su Product). */
data class LineaCarrito(
    val producto: Product,
    val cantidad: Int
)

/** Modalidad de envío del courier ficticio (sección 10.4 del plan). */
enum class ModalidadEnvio(val etiqueta: String, val factor: Double) {
    ESTANDAR("Estándar", 1.0),
    EXPRESS("Express", 1.6)
}

/** Zonas internacionales del courier ficticio, con su tarifa y los países que cubren. */
enum class ZonaInternacional(
    val etiqueta: String,
    val tarifaBase: Int,
    val tarifaKgAdicional: Int,
    val paises: List<String>
) {
    SUDAMERICA(
        "Zona 1 · Sudamérica", 15000, 4000,
        listOf("Argentina", "Bolivia", "Brasil", "Colombia", "Ecuador", "Paraguay", "Perú", "Uruguay", "Venezuela")
    ),
    ZONA_2(
        "Zona 2 · México, España y Portugal", 25000, 6000,
        listOf("México", "España", "Portugal")
    );

    companion object {
        /** Todos los países a los que se puede enviar fuera de Chile (para la lista del formulario). */
        val paisesDisponibles: List<String> = entries.flatMap { it.paises }

        /** Zona a la que pertenece el país, o null si el courier no envía a ese país. */
        fun desdePais(pais: String): ZonaInternacional? =
            entries.firstOrNull { zona -> zona.paises.any { it.equals(pais.trim(), ignoreCase = true) } }
    }
}

/** Regiones de Chile para el envío nacional. La primera es la Región Metropolitana (factor ×1.0). */
object RegionesChile {
    const val METROPOLITANA = "Región Metropolitana de Santiago"

    val todas: List<String> = listOf(
        METROPOLITANA,
        "Arica y Parinacota",
        "Tarapacá",
        "Antofagasta",
        "Atacama",
        "Coquimbo",
        "Valparaíso",
        "Libertador General Bernardo O'Higgins",
        "Maule",
        "Ñuble",
        "Biobío",
        "La Araucanía",
        "Los Ríos",
        "Los Lagos",
        "Aysén del General Carlos Ibáñez del Campo",
        "Magallanes y de la Antártica Chilena"
    )
}

/** Destino del envío: nacional (por región) o internacional (por zona). */
sealed class Destino {
    data class Nacional(val region: String) : Destino() {
        val esRegionMetropolitana: Boolean get() = region == RegionesChile.METROPOLITANA
    }

    data class Internacional(val zona: ZonaInternacional) : Destino()
}

/** Resultado de la calculadora, listo para mostrarlo en el resumen del carrito. */
data class ResultadoEnvio(
    val subtotal: Int,
    val pesoTotalKg: Double,
    val costoEnvio: Int,
    val total: Int,
    /** true si el pedido tiene ítems físicos (si solo hay cursos, no hay envío). */
    val requiereEnvio: Boolean,
    /** true si se aplicó el envío gratis nacional por superar el monto mínimo. */
    val envioGratis: Boolean
)
