package com.example.museo_de_los_museos_app.viewmodel.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.domain.ValidadoresCuenta
import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.repository.PreferenciasRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.UserRepository
import com.example.museo_de_los_museos_app.repository.preferencias
import com.example.museo_de_los_museos_app.repository.usuarios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Datos del usuario activo, edición del nombre, foto, configuración y cierre de sesión. */
class PerfilViewModel(
    private val usuarios: UserRepository = Repositorios.usuarios,
    private val repoPreferencias: PreferenciasRepository = Repositorios.preferencias
) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUiState())
    val estado: StateFlow<PerfilUiState> = _estado.asStateFlow()

    /** Id del usuario activo: la pantalla lo usa para nombrar el archivo de la foto. */
    val userId: String? get() = SessionManager.userId

    init {
        // Espera al primer usuario con sesión y llena el formulario con sus datos y su configuración.
        viewModelScope.launch {
            val usuario = SessionManager.usuarioActual.filterNotNull().first()
            val prefs = repoPreferencias.obtener(usuario.id)
            _estado.update {
                it.copy(
                    nombre = usuario.nombre,
                    correo = usuario.correo,
                    rol = usuario.rol.etiqueta,
                    rolUsuario = usuario.rol,
                    fotoUri = usuario.avatarUri?.let(Uri::parse),
                    preferencias = prefs
                )
            }
        }
    }

    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errorNombre = null, mensaje = null) }
    }

    // ---------- Configuración: cada cambio se guarda al tiro en SQLite ----------

    fun onNotificacionesChange(valor: Boolean) = cambiarPreferencias { copy(notificaciones = valor) }
    fun onTextoGrandeChange(valor: Boolean) = cambiarPreferencias { copy(textoGrande = valor) }
    fun onIdiomaChange(codigo: String) = cambiarPreferencias { copy(idioma = codigo) }
    fun onOpcionRolChange(valor: Boolean) = cambiarPreferencias { copy(opcionRol = valor) }

    private fun cambiarPreferencias(cambio: Preferencias.() -> Preferencias) {
        val actuales = _estado.value.preferencias ?: return // todavía cargando
        val nuevas = actuales.cambio()
        _estado.update { it.copy(preferencias = nuevas) }
        viewModelScope.launch { repoPreferencias.guardar(nuevas) }
    }

    /** La pantalla llama esto cuando la cámara o la galería entregan una foto (ya copiada a nuestra carpeta). */
    fun onFotoNueva(uri: Uri?) {
        if (uri == null) {
            _estado.update { it.copy(mensaje = R.string.perfil_error_foto, mensajeEsError = true) }
            return
        }
        _estado.update { it.copy(fotoUri = uri) }
        // La foto se guarda de inmediato en SQLite, sin esperar el botón "Guardar".
        viewModelScope.launch {
            val usuario = SessionManager.usuarioActual.value ?: return@launch
            val actualizado = usuario.copy(avatarUri = uri.toString())
            usuarios.actualizar(actualizado)
            SessionManager.actualizarUsuarioActual(actualizado)
            _estado.update { it.copy(mensaje = R.string.perfil_foto_actualizada, mensajeEsError = false) }
        }
    }

    fun guardar() {
        val actual = _estado.value
        if (actual.guardando) return

        val error = ValidadoresCuenta.nombre(actual.nombre)
        if (error != null) {
            _estado.update { it.copy(errorNombre = error) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(guardando = true) }
            delay(400) // carga simulada, igual que en Registro

            val usuario = SessionManager.usuarioActual.value ?: return@launch
            val actualizado = usuario.copy(nombre = actual.nombre.trim())
            usuarios.actualizar(actualizado)
            SessionManager.actualizarUsuarioActual(actualizado)

            _estado.update {
                it.copy(guardando = false, mensaje = R.string.perfil_guardado, mensajeEsError = false)
            }
        }
    }

    fun cerrarSesion() {
        SessionManager.cerrarSesion()
        _estado.update { it.copy(sesionCerrada = true) }
    }
}
