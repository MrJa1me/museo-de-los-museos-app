package com.example.museo_de_los_museos_app.model

/**
 * Ítem del catálogo (curso o producto físico).
 * - Cursos: [stock] son los cupos y [pesoKg] es null (no tienen envío).
 * - Productos físicos: [pesoKg] se usa en la calculadora de envío.
 */
data class Product(
    val id: String,
    val nombre: String,
    val categoria: CategoriaProducto,
    val precio: Int,
    val stock: Int,
    val pesoKg: Double? = null,
    val descripcion: String = "",
    val activo: Boolean = true
) {
    val requiereEnvio: Boolean get() = pesoKg != null
    val disponible: Boolean get() = activo && stock > 0
}
