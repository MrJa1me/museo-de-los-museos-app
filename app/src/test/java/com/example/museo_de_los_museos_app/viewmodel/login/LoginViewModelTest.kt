package com.example.museo_de_los_museos_app.viewmodel.login

import com.example.museo_de_los_museos_app.domain.ErrorCuenta
import com.example.museo_de_los_museos_app.fakes.FakeUserRepository
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(FakeUserRepository())
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    @Test
    fun camposVacios_muestranErrores_sinIntentarIngresar() {
        viewModel.ingresar()
        val estado = viewModel.estado.value
        assertEquals(ErrorCuenta.OBLIGATORIO, estado.errores.correo)
        assertEquals(ErrorCuenta.OBLIGATORIO, estado.errores.contrasena)
        assertFalse(estado.cargando)
    }

    @Test
    fun despuesDelPrimerIntento_seValidaMientrasEscribe() {
        viewModel.ingresar()
        viewModel.onCorreoChange("cliente1@museo.test")
        assertNull(viewModel.estado.value.errores.correo)
    }

    @Test
    fun credencialesDemo_inicianSesionConElRolElegido() = runTest {
        viewModel.onRolChange(Rol.INVESTIGADOR)
        viewModel.usarCredencialesDemo()
        viewModel.ingresar()
        advanceUntilIdle()

        assertTrue(viewModel.estado.value.loginExitoso)
        assertEquals(Rol.INVESTIGADOR, SessionManager.rol)
    }

    @Test
    fun contrasenaIncorrecta_oRolDistinto_sonCredencialesIncorrectas() = runTest {
        viewModel.onRolChange(Rol.CLIENTE)
        viewModel.onCorreoChange("cliente1@museo.test")
        viewModel.onContrasenaChange("Otra123")
        viewModel.ingresar()
        advanceUntilIdle()
        assertEquals(ErrorCuenta.CREDENCIALES_INCORRECTAS, viewModel.estado.value.errorGeneral)

        viewModel.onRolChange(Rol.ADMINISTRADOR)
        viewModel.onContrasenaChange("Cliente123")
        assertNull(viewModel.estado.value.errorGeneral)
        viewModel.ingresar()
        advanceUntilIdle()
        assertEquals(ErrorCuenta.CREDENCIALES_INCORRECTAS, viewModel.estado.value.errorGeneral)
        assertNull(SessionManager.userId)
    }

    @Test
    fun correoInexistente_esCredencialesIncorrectas() = runTest {
        viewModel.onCorreoChange("nadie@museo.test")
        viewModel.onContrasenaChange("Algo1234")
        viewModel.ingresar()
        advanceUntilIdle()
        assertEquals(ErrorCuenta.CREDENCIALES_INCORRECTAS, viewModel.estado.value.errorGeneral)
    }
}
