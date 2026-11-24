package com.example.museo_de_los_museos_app.viewmodel.dashboard

import com.example.museo_de_los_museos_app.fakes.MuseosConFallo
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.repository.InMemoryMuseumRepository
import com.example.museo_de_los_museos_app.repository.InMemoryOrderRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Aprobación de fichas pendientes (PATCH /api/museos/{id}/aprobar) desde el Dashboard del Administrador. */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardAprobacionTest {

    private val museos = MuseosConFallo(InMemoryMuseumRepository(SeedData.museos + SeedData.museos.first().copy(id = "m11", publicado = false)))
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = DashboardViewModel(museos, InMemoryProductRepository(), InMemoryOrderRepository())
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    @Test
    fun administrador_apruebaLaFicha() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR })
        viewModel.aprobarMuseo("m11")
        assertTrue(museos.obtenerPorId("m11")!!.publicado)
    }

    @Test
    fun cliente_noPuedeAprobar() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.CLIENTE })
        viewModel.aprobarMuseo("m11")
        assertFalse(museos.obtenerPorId("m11")!!.publicado)
    }

    @Test
    fun servidorCaido_muestraElError() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR })
        museos.falla = ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)
        viewModel.aprobarMuseo("m11")
        assertEquals(TipoErrorRemoto.SIN_CONEXION, viewModel.errorAprobacion.value?.tipo)
        viewModel.onErrorMostrado()
        assertNull(viewModel.errorAprobacion.value)
    }
}
