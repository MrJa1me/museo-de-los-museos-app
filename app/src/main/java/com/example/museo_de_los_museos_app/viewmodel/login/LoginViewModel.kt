package com.example.museo_de_los_museos_app.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.ErrorCuenta
import com.example.museo_de_los_museos_app.domain.ValidadoresCuenta
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.UserRepository
import com.example.museo_de_los_museos_app.repository.usuarios
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Toda la validación y la autenticación del login viven aquí, nunca en el Composable. */
class LoginViewModel(
    private val usuarios: UserRepository = Repositorios.usuarios
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    fun onRolChange(rol: Rol) = actualizar { copy(rol = rol) }
    fun onCorreoChange(valor: String) = actualizar { copy(correo = valor) }
    fun onContrasenaChange(valor: String) = actualizar { copy(contrasena = valor) }

    private fun actualizar(cambio: LoginUiState.() -> LoginUiState) {
        _estado.update { actual ->
            val nuevo = actual.cambio().copy(errorGeneral = null)
            if (nuevo.intentoEnvio) nuevo.copy(errores = validar(nuevo)) else nuevo
        }
    }

    private fun validar(estado: LoginUiState) = LoginErrores(
        correo = ValidadoresCuenta.correo(estado.correo),
        // En el login solo se exige que no esté vacía; las reglas de formato aplican al registrarse
        contrasena = if (estado.contrasena.isEmpty()) ErrorCuenta.OBLIGATORIO else null
    )

    /** Rellena el formulario con las credenciales de prueba del perfil elegido (datos ficticios del plan). */
    fun usarCredencialesDemo() {
        val demo = SeedData.usuarios.first { it.rol == _estado.value.rol }
        _estado.update {
            it.copy(
                correo = demo.correo,
                contrasena = demo.contrasena,
                errores = LoginErrores(),
                errorGeneral = null
            )
        }
    }

    fun ingresar() {
        val actual = _estado.value
        if (actual.cargando) return

        val errores = validar(actual)
        _estado.update { it.copy(intentoEnvio = true, errores = errores, errorGeneral = null) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            delay(600) // carga simulada (patrón de la Guía 12)

            val usuario = usuarios.buscarPorCorreo(actual.correo)
            val credencialesValidas = usuario != null &&
                usuario.contrasena == actual.contrasena &&
                usuario.rol == actual.rol

            if (usuario == null || !credencialesValidas) {
                _estado.update { it.copy(cargando = false, errorGeneral = ErrorCuenta.CREDENCIALES_INCORRECTAS) }
                return@launch
            }

            SessionManager.iniciarSesion(usuario)
            _estado.update { it.copy(cargando = false, loginExitoso = true) }
        }
    }
}
