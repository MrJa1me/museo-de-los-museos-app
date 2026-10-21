package com.example.museo_de_los_museos_app.ui.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.domain.ErrorValidacion

/**
 * Texto (strings.xml) de cada error de validación. El ViewModel guarda el error en su UiState
 * y el Composable muestra stringResource(error.mensaje()) en el supportingText del campo.
 */
@StringRes
fun ErrorValidacion.mensaje(): Int = when (this) {
    ErrorValidacion.CAMPO_VACIO -> R.string.error_campo_vacio
    ErrorValidacion.TEXTO_MUY_CORTO -> R.string.error_texto_muy_corto
    ErrorValidacion.SOLO_LETRAS -> R.string.error_solo_letras
    ErrorValidacion.NUMERO_INVALIDO -> R.string.error_numero_invalido
    ErrorValidacion.CANTIDAD_MINIMA -> R.string.error_cantidad_minima
    ErrorValidacion.PRODUCTO_AGOTADO -> R.string.error_producto_agotado
    ErrorValidacion.SUPERA_STOCK -> R.string.error_supera_stock
    ErrorValidacion.PRECIO_INVALIDO -> R.string.error_precio_invalido
    ErrorValidacion.STOCK_INVALIDO -> R.string.error_stock_invalido
    ErrorValidacion.PESO_INVALIDO -> R.string.error_peso_invalido
    ErrorValidacion.PESO_MAXIMO -> R.string.error_peso_maximo
    ErrorValidacion.TELEFONO_INVALIDO -> R.string.error_telefono_invalido
    ErrorValidacion.REGION_INVALIDA -> R.string.error_region_invalida
    ErrorValidacion.PAIS_NO_DISPONIBLE -> R.string.error_pais_no_disponible
}

/** Uso en la UI:  mensajeError = estado.errorCantidad.texto()  (null si no hay error). */
@Composable
fun ErrorValidacion?.texto(): String? = this?.let { stringResource(it.mensaje()) }
