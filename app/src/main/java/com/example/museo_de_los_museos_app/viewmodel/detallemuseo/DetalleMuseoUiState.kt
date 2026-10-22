package com.example.museo_de_los_museos_app.viewmodel.detallemuseo

import com.example.museo_de_los_museos_app.domain.ErrorFicha
import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.Museum

/** Campos del formulario de ficha (Investigador), como texto tal cual se escriben. */
data class FormularioFicha(
    val nombre: String = "",
    val pais: String = "",
    val ciudad: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val clasificacion: ClasificacionMuseo = ClasificacionMuseo.ARTE,
    val descripcion: String = "",
    val enlace: String = ""
)

data class ErroresFicha(
    val nombre: ErrorFicha? = null,
    val pais: ErrorFicha? = null,
    val ciudad: ErrorFicha? = null,
    val latitud: ErrorFicha? = null,
    val longitud: ErrorFicha? = null,
    val descripcion: ErrorFicha? = null,
    val enlace: ErrorFicha? = null
) {
    val hayErrores: Boolean
        get() = listOf(nombre, pais, ciudad, latitud, longitud, descripcion, enlace).any { it != null }
}

/** Mensajes breves que la pantalla muestra en un Snackbar. */
enum class MensajeDetalle { FAVORITO_AGREGADO, FAVORITO_QUITADO, SIN_SESION }

data class DetalleMuseoUiState(
    val cargando: Boolean = true,
    val museo: Museum? = null,
    val noEncontrado: Boolean = false,
    /** true cuando el Investigador está creando una ficha nueva (ruta detalle_museo/nuevo). */
    val esNuevo: Boolean = false,
    val esFavorito: Boolean = false,
    val esInvestigador: Boolean = false,
    val editando: Boolean = false,
    val formulario: FormularioFicha = FormularioFicha(),
    val errores: ErroresFicha = ErroresFicha(),
    /** Después del primer intento de guardar, los campos se validan mientras el usuario escribe. */
    val intentoEnvio: Boolean = false,
    val guardando: Boolean = false,
    val fichaGuardada: Boolean = false,
    val mensaje: MensajeDetalle? = null,
    /** Evento: volver atrás después de crear una ficha nueva. */
    val cerrar: Boolean = false
)
