package com.example.museo_de_los_museos_app.viewmodel.carrito

import com.example.museo_de_los_museos_app.domain.CalculadoraEnvio
import com.example.museo_de_los_museos_app.domain.ErrorValidacion
import com.example.museo_de_los_museos_app.domain.LineaCarrito
import com.example.museo_de_los_museos_app.domain.ModalidadEnvio
import com.example.museo_de_los_museos_app.domain.RegionesChile
import com.example.museo_de_los_museos_app.domain.ResultadoEnvio

enum class TipoEnvio { NACIONAL, INTERNACIONAL }

/** Datos que escribe el usuario en el formulario de destino. */
data class FormularioDestino(
    val nombre: String = "",
    val direccion: String = "",
    val ciudad: String = "",
    val telefono: String = "",
    val tipo: TipoEnvio = TipoEnvio.NACIONAL,
    val region: String = RegionesChile.METROPOLITANA,
    val pais: String = "",
    val modalidad: ModalidadEnvio = ModalidadEnvio.ESTANDAR
)

/** Error de cada campo (null = sin error). Lo llena el ViewModel con Validadores. */
data class ErroresDestino(
    val nombre: ErrorValidacion? = null,
    val direccion: ErrorValidacion? = null,
    val ciudad: ErrorValidacion? = null,
    val telefono: ErrorValidacion? = null,
    val region: ErrorValidacion? = null,
    val pais: ErrorValidacion? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, direccion, ciudad, telefono, region, pais).any { it != null }
}

data class CarritoUiState(
    val cargando: Boolean = true,
    val haySesion: Boolean = false,
    val lineas: List<LineaCarrito> = emptyList(),
    val formulario: FormularioDestino = FormularioDestino(),
    val errores: ErroresDestino = ErroresDestino(),
    /** Costo de envío y total. Es null si el destino aún no está completo (ej. país sin elegir). */
    val resultado: ResultadoEnvio? = null,
    /** Nombre del producto que no tiene stock suficiente (para el aviso), o null. */
    val productoSinStock: String? = null,
    /** true mientras se muestra el diálogo con el total final antes de confirmar. */
    val mostrandoConfirmacion: Boolean = false,
    val confirmando: Boolean = false,
    /** Id de la solicitud creada; cuando no es null se muestra la pantalla de éxito. */
    val pedidoConfirmado: String? = null
) {
    val vacio: Boolean get() = lineas.isEmpty()
    val requiereEnvio: Boolean get() = lineas.any { it.producto.requiereEnvio }
    val subtotal: Int get() = CalculadoraEnvio.calcularSubtotal(lineas)
}
