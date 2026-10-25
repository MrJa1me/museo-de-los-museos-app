package com.example.museo_de_los_museos_app.viewmodel.detalleproducto

import com.example.museo_de_los_museos_app.domain.ErrorValidacion
import com.example.museo_de_los_museos_app.model.Product

data class DetalleProductoUiState(
    val cargando: Boolean = true,
    val noEncontrado: Boolean = false,
    val producto: Product? = null,
    /** Texto del campo cantidad (String para poder validar lo que escribe el usuario). */
    val cantidad: String = "1",
    val errorCantidad: ErrorValidacion? = null,
    /** Unidades de este producto que el usuario ya tiene en su carrito. */
    val enCarrito: Int = 0,
    val haySesion: Boolean = false,
    /** El Administrador ve los botones Editar y Desactivar/Activar en lugar de la compra. */
    val esAdministrador: Boolean = false,
    val agregando: Boolean = false,
    val mensaje: MensajeDetalleProducto? = null
) {
    val esCurso: Boolean get() = producto?.requiereEnvio == false
}

/** Mensaje de retroalimentación que se muestra con animación bajo los botones. */
enum class MensajeDetalleProducto {
    AGREGADO_AL_CARRITO,
    INFORMACION_SOLICITADA,
    PRODUCTO_ACTIVADO,
    PRODUCTO_DESACTIVADO
}
