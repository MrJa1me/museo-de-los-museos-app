package com.example.museo_de_los_museos_app.viewmodel.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.ErrorCuenta
import com.example.museo_de_los_museos_app.domain.ValidadoresCuenta
import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User
import com.example.museo_de_los_museos_app.repository.PreferenciasRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.UserRepository
import com.example.museo_de_los_museos_app.repository.preferencias
import com.example.museo_de_los_museos_app.repository.usuarios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Toda la validación del registro vive aquí (y en ValidadoresCuenta), nunca en el Composable. */
class RegistroViewModel(
    private val usuarios: UserRepository = Repositorios.usuarios,
    private val repoPreferencias: PreferenciasRepository = Repositorios.preferencias
) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    fun onNombreChange(valor: String) = actualizar { copy(nombre = valor) }
    fun onCorreoChange(valor: String) = actualizar { copy(correo = valor) }
    fun onContrasenaChange(valor: String) = actualizar { copy(contrasena = valor) }
    fun onConfirmacionChange(valor: String) = actualizar { copy(confirmacion = valor) }
    fun onNotificacionesChange(valor: Boolean) = _estado.update { it.copy(recibirNotificaciones = valor) }

    private fun actualizar(cambio: RegistroUiState.() -> RegistroUiState) {
        _estado.update { actual ->
            val nuevo = actual.cambio()
            if (nuevo.intentoEnvio) nuevo.copy(errores = validar(nuevo)) else nuevo
        }
    }

    private fun validar(estado: RegistroUiState) = RegistroErrores(
        nombre = ValidadoresCuenta.nombre(estado.nombre),
        correo = ValidadoresCuenta.correo(estado.correo),
        contrasena = ValidadoresCuenta.contrasena(estado.contrasena),
        confirmacion = ValidadoresCuenta.confirmacionContrasena(estado.contrasena, estado.confirmacion)
    )

    fun registrar() {
        val actual = _estado.value
        if (actual.cargando) return

        val errores = validar(actual)
        _estado.update { it.copy(intentoEnvio = true, errores = errores) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            delay(600) // carga simulada (patrón de la Guía 12)

            if (usuarios.buscarPorCorreo(actual.correo) != null) {
                _estado.update {
                    it.copy(cargando = false, errores = it.errores.copy(correo = ErrorCuenta.CORREO_EN_USO))
                }
                return@launch
            }

            val nuevoId = "u" + System.currentTimeMillis()
            usuarios.registrar(
                User(
                    id = nuevoId,
                    nombre = actual.nombre.trim(),
                    correo = actual.correo.trim().lowercase(),
                    contrasena = actual.contrasena,
                    rol = Rol.CLIENTE
                )
            )
            repoPreferencias.guardar(
                Preferencias(userId = nuevoId, notificaciones = actual.recibirNotificaciones)
            )
            _estado.update { it.copy(cargando = false, registroExitoso = true) }
        }
    }
}
