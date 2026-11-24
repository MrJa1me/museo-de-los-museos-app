package com.example.museo_de_los_museos_app.viewmodel.registro

import com.example.museo_de_los_museos_app.domain.ErrorCuenta
import com.example.museo_de_los_museos_app.fakes.FakePreferenciasRepository
import com.example.museo_de_los_museos_app.fakes.FakeUserRepository
import com.example.museo_de_los_museos_app.model.Rol
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
class RegistroViewModelTest {

    private val usuarios = FakeUserRepository()
    private val preferencias = FakePreferenciasRepository()
    private lateinit var viewModel: RegistroViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RegistroViewModel(usuarios, preferencias)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun llenar(correo: String) {
        viewModel.onNombreChange("Ana Pérez")
        viewModel.onCorreoChange(correo)
        viewModel.onContrasenaChange("Clave123")
        viewModel.onConfirmacionChange("Clave123")
    }

    @Test
    fun formularioVacio_marcaErroresPorCampo() {
        viewModel.registrar()
        val errores = viewModel.estado.value.errores
        assertTrue(errores.hayErrores)
        assertEquals(ErrorCuenta.OBLIGATORIO, errores.correo)
    }

    @Test
    fun confirmacionDistinta_seDetectaAlEscribir() {
        viewModel.registrar()
        viewModel.onContrasenaChange("Clave123")
        viewModel.onConfirmacionChange("Clave124")
        assertEquals(ErrorCuenta.NO_COINCIDEN, viewModel.estado.value.errores.confirmacion)
    }

    @Test
    fun registroValido_creaClienteConSusPreferencias() = runTest {
        llenar("  Ana@Correo.CL ")
        viewModel.onNotificacionesChange(false)
        viewModel.registrar()
        advanceUntilIdle()

        val nuevo = usuarios.usuarios.last()
        assertEquals("ana@correo.cl", nuevo.correo)
        assertEquals(Rol.CLIENTE, nuevo.rol)
        assertFalse(preferencias.guardadas.value.getValue(nuevo.id).notificaciones)
        assertTrue(viewModel.estado.value.registroExitoso)
    }

    @Test
    fun correoEnUso_noRegistra() = runTest {
        llenar("admin@museo.test")
        viewModel.registrar()
        advanceUntilIdle()
        assertEquals(ErrorCuenta.CORREO_EN_USO, viewModel.estado.value.errores.correo)
        assertEquals(4, usuarios.usuarios.size)
        assertNull(viewModel.estado.value.errores.nombre)
    }
}
