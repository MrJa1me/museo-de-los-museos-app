package com.example.museo_de_los_museos_app.viewmodel.perfil

import android.net.Uri
import androidx.annotation.StringRes
import com.example.museo_de_los_museos_app.domain.ErrorCuenta

data class PerfilUiState(
    val nombre: String = "",
    val correo: String = "",
    val rol: String = "",
    /** Foto elegida o capturada. null = sin foto (se muestra el ícono). */
    val fotoUri: Uri? = null,
    val errorNombre: ErrorCuenta? = null,
    val guardando: Boolean = false,
    /** Mensaje para el banner (éxito o error). */
    @StringRes val mensaje: Int? = null,
    val mensajeEsError: Boolean = false,
    val sesionCerrada: Boolean = false
)