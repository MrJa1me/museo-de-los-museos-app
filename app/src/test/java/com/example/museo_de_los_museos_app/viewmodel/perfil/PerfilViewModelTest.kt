package com.example.museo_de_los_museos_app.viewmodel.perfil

import com.example.museo_de_los_museos_app.R
import com.example.museo_de_los_museos_app.domain.ErrorCuenta
import com.example.museo_de_los_museos_app.fakes.FakePreferenciasRepository
import com.example.museo_de_los_museos_app.fakes.FakeUserRepository
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import io.mockk.every
import io.mockk.mockk
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
class PerfilViewModelTest {

    private val usuarios = FakeUserRepository()
    private val preferencias = FakePreferenciasRepository()
    private val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }
    private lateinit var viewModel: PerfilViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        SessionManager.iniciarSesion(cliente)
        viewModel = PerfilViewModel(usuarios, preferencias)
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    @Test
    fun cargaLosDatosDelUsuarioActivo() {
        val estado = viewModel.estado.value
        assertEquals(cliente.nombre, estado.nombre)
        assertEquals(cliente.correo, estado.correo)
        assertEquals(cliente.id, viewModel.userId)
        assertEquals(cliente.id, estado.preferencias?.userId)
    }

    @Test
    fun preferencias_seGuardanAlCambiar() {
        viewModel.onNotificacionesChange(false)
        viewModel.onTextoGrandeChange(true)
        viewModel.onIdiomaChange("pt")
        viewModel.onOpcionRolChange(true)
        val guardadas = preferencias.guardadas.value.getValue(cliente.id)
        assertFalse(guardadas.notificaciones)
        assertTrue(guardadas.textoGrande)
        assertEquals("pt", guardadas.idioma)
        assertTrue(guardadas.opcionRol)
    }

    @Test
    fun guardar_nombreInvalido_noGuarda() {
        viewModel.onNombreChange("")
        viewModel.guardar()
        assertEquals(ErrorCuenta.OBLIGATORIO, viewModel.estado.value.errorNombre)
    }

    @Test
    fun guardar_nombreValido_actualizaUsuarioYSesion() = runTest {
        viewModel.onNombreChange("  Cliente Renombrado ")
        viewModel.guardar()
        advanceUntilIdle()
        assertEquals("Cliente Renombrado", usuarios.usuarios.first { it.id == cliente.id }.nombre)
        assertEquals("Cliente Renombrado", SessionManager.usuarioActual.value?.nombre)
        assertEquals(R.string.perfil_guardado, viewModel.estado.value.mensaje)
    }

    @Test
    fun foto_nula_esError_yValida_seGuarda() = runTest {
        viewModel.onFotoNueva(null)
        assertTrue(viewModel.estado.value.mensajeEsError)

        val uri = mockk<android.net.Uri>()
        every { uri.toString() } returns "file:///avatar.jpg"
        viewModel.onFotoNueva(uri)
        advanceUntilIdle()
        assertEquals("file:///avatar.jpg", usuarios.usuarios.first { it.id == cliente.id }.avatarUri)
        assertEquals(R.string.perfil_foto_actualizada, viewModel.estado.value.mensaje)
    }

    @Test
    fun cerrarSesion_limpiaLaSesion() {
        viewModel.cerrarSesion()
        assertTrue(viewModel.estado.value.sesionCerrada)
        assertNull(SessionManager.userId)
    }
}
