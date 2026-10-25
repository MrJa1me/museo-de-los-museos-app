package com.example.museo_de_los_museos_app.viewmodel.formularioproducto

import com.example.museo_de_los_museos_app.domain.ErrorValidacion
import com.example.museo_de_los_museos_app.model.CategoriaProducto

/** Error de cada campo (null = sin error). Lo llena el ViewModel con Validadores. */
data class ErroresProducto(
    val nombre: ErrorValidacion? = null,
    val precio: ErrorValidacion? = null,
    val stock: ErrorValidacion? = null,
    val peso: ErrorValidacion? = null
) {
    val hayErrores: Boolean get() = listOf(nombre, precio, stock, peso).any { it != null }
}

data class FormularioProductoUiState(
    val cargando: Boolean = false,
    /** true = se está editando un producto existente; false = alta de un producto nuevo. */
    val esEdicion: Boolean = false,
    val esAdministrador: Boolean = false,
    val noEncontrado: Boolean = false,
    // Campos como texto, para poder validar lo que escribe el usuario
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: CategoriaProducto = CategoriaProducto.CURSO,
    val precio: String = "",
    val stock: String = "",
    val peso: String = "",
    val errores: ErroresProducto = ErroresProducto(),
    val guardando: Boolean = false,
    /** Cuando es true la pantalla vuelve atrás (el producto ya se guardó). */
    val guardado: Boolean = false
) {
    /** Los cursos tienen cupos y no tienen peso (no se envían). */
    val esCurso: Boolean get() = categoria == CategoriaProducto.CURSO
}
