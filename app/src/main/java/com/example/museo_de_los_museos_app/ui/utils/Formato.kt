package com.example.museo_de_los_museos_app.ui.utils

/** 45000 → "$45.000" (pesos chilenos, separador de miles con punto). */
fun formatearPrecio(valor: Int): String {
    val conMiles = valor.toString().reversed().chunked(3).joinToString(".").reversed()
    return "\$$conMiles"
}
