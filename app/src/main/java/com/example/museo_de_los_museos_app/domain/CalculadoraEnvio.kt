package com.example.museo_de_los_museos_app.domain

/**
 * Calculadora de envío con las reglas del courier ficticio (sección 10.4 del plan).
 * No depende de la UI ni de Android: se prueba con pruebas unitarias (CalculadoraEnvioTest).
 *
 * Reglas:
 * - Solo los ítems físicos pesan; los cursos no tienen envío.
 * - Peso total = suma de (peso × cantidad). Cada kg sobre el primero se cobra completo (se redondea hacia arriba).
 * - Nacional: 3.500 hasta 1 kg + 1.000 por kg adicional. Región Metropolitana ×1.0, otras regiones ×1.3.
 *   Envío gratis si el subtotal es de 60.000 o más.
 * - Internacional: zona 1 (Sudamérica) 15.000 + 4.000 por kg adicional;
 *   zona 2 (México, España, Portugal) 25.000 + 6.000 por kg adicional.
 * - Modalidad: Estándar ×1.0, Express ×1.6 sobre el costo calculado.
 * - Total = subtotal + costo de envío.
 */
object CalculadoraEnvio {

    const val TARIFA_BASE_NACIONAL = 3500
    const val TARIFA_KG_ADICIONAL_NACIONAL = 1000
    const val FACTOR_REGION_METROPOLITANA = 1.0
    const val FACTOR_OTRAS_REGIONES = 1.3
    const val MONTO_ENVIO_GRATIS = 60000

    private const val GRAMOS_POR_KG = 1000

    fun calcular(lineas: List<LineaCarrito>, destino: Destino, modalidad: ModalidadEnvio): ResultadoEnvio {
        val subtotal = calcularSubtotal(lineas)
        val gramos = pesoTotalGramos(lineas)

        // Solo cursos (o carrito vacío): no hay nada que enviar.
        if (gramos == 0) {
            return ResultadoEnvio(
                subtotal = subtotal,
                pesoTotalKg = 0.0,
                costoEnvio = 0,
                total = subtotal,
                requiereEnvio = false,
                envioGratis = false
            )
        }

        val envioGratis = destino is Destino.Nacional && subtotal >= MONTO_ENVIO_GRATIS
        val costoEnvio = if (envioGratis) 0 else costoSegunDestino(gramos, destino, modalidad)

        return ResultadoEnvio(
            subtotal = subtotal,
            pesoTotalKg = gramos.toDouble() / GRAMOS_POR_KG,
            costoEnvio = costoEnvio,
            total = subtotal + costoEnvio,
            requiereEnvio = true,
            envioGratis = envioGratis
        )
    }

    fun calcularSubtotal(lineas: List<LineaCarrito>): Int =
        lineas.sumOf { it.producto.precio * it.cantidad }

    /**
     * Peso total en gramos (entero) para evitar errores de decimales:
     * por ejemplo 0.4 × 3 en Double da 1.2000000000000002 y podría cobrar un kg de más.
     */
    fun pesoTotalGramos(lineas: List<LineaCarrito>): Int =
        lineas.sumOf { linea ->
            val gramosUnidad = linea.producto.pesoKg?.let { Math.round(it * GRAMOS_POR_KG).toInt() } ?: 0
            gramosUnidad * linea.cantidad
        }

    /** Kilos que se cobran sobre el primero, redondeando hacia arriba (1,2 kg → 1 adicional; 2,0 kg → 1 adicional). */
    fun kgAdicionales(gramos: Int): Int {
        if (gramos <= GRAMOS_POR_KG) return 0
        // División entera redondeando hacia arriba: kilos totales cobrados, menos el primero (incluido en la tarifa base).
        val kilosCobrados = (gramos + GRAMOS_POR_KG - 1) / GRAMOS_POR_KG
        return kilosCobrados - 1
    }

    private fun costoSegunDestino(gramos: Int, destino: Destino, modalidad: ModalidadEnvio): Int {
        val adicionales = kgAdicionales(gramos)
        val costoBase: Double = when (destino) {
            is Destino.Nacional -> {
                val factorRegion = if (destino.esRegionMetropolitana) FACTOR_REGION_METROPOLITANA else FACTOR_OTRAS_REGIONES
                (TARIFA_BASE_NACIONAL + adicionales * TARIFA_KG_ADICIONAL_NACIONAL) * factorRegion
            }
            is Destino.Internacional -> {
                (destino.zona.tarifaBase + adicionales * destino.zona.tarifaKgAdicional).toDouble()
            }
        }
        return Math.round(costoBase * modalidad.factor).toInt()
    }
}
