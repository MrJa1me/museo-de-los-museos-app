package com.example.museo_de_los_museos_app.viewmodel.mapa

import com.example.museo_de_los_museos_app.fakes.MuseosConFallo
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.InMemoryMuseumRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class MapaViewModelTest {

    private val pendiente = SeedData.museos.first().copy(id = "m11", nombre = "Pendiente", pais = "Chile", publicado = false)
    private val museos = MuseosConFallo(InMemoryMuseumRepository(SeedData.museos + pendiente))
    private lateinit var viewModel: MapaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MapaViewModel(museos)
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    @Test
    fun invitado_soloVeMuseosPublicados() {
        assertEquals(SeedData.museos.size, viewModel.uiState.value.museos.size)
    }

    @Test
    fun investigador_tambienVeLosPendientes() {
        SessionManager.iniciarSesion(SeedData.usuarios.first { it.rol == Rol.INVESTIGADOR })
        assertEquals(SeedData.museos.size + 1, viewModel.uiState.value.museos.size)
        assertTrue(viewModel.uiState.value.esInvestigador)
    }

    @Test
    fun filtroPorPais_seActivaYSeQuitaAlTocarloDeNuevo() {
        viewModel.onPaisSeleccionado("Chile")
        assertEquals(listOf("m04"), viewModel.uiState.value.museos.map { it.id })

        viewModel.onPaisSeleccionado("Chile")
        assertNull(viewModel.uiState.value.paisSeleccionado)
        assertEquals(SeedData.museos.size, viewModel.uiState.value.museos.size)
    }

    @Test
    fun paises_sinRepetir_yOrdenados() {
        val paises = viewModel.uiState.value.paises
        assertEquals(paises.sorted(), paises)
        assertEquals(paises.distinct(), paises)
    }

    @Test
    fun seleccionarYCerrarMuseo() {
        viewModel.onMuseoSeleccionado("m05")
        assertEquals("m05", viewModel.uiState.value.museoSeleccionado?.id)
        viewModel.onCerrarSeleccion()
        assertNull(viewModel.uiState.value.museoSeleccionado)
    }

    @Test
    fun permiso_sinConceder_primeroExplica_yLuegoPide() {
        viewModel.onPermisoVerificado(false)
        viewModel.onMiUbicacionClick()
        assertTrue(viewModel.uiState.value.mostrarExplicacionPermiso)

        viewModel.onExplicacionAceptada()
        assertTrue(viewModel.uiState.value.solicitarPermiso)
        viewModel.onSolicitudLanzada()
        assertFalse(viewModel.uiState.value.solicitarPermiso)

        viewModel.onResultadoPermiso(true)
        assertEquals(EstadoPermiso.CONCEDIDO, viewModel.uiState.value.permisoUbicacion)
        assertTrue(viewModel.uiState.value.centrarEnUbicacion)
        viewModel.onCentradoRealizado()
        assertFalse(viewModel.uiState.value.centrarEnUbicacion)
    }

    @Test
    fun permiso_rechazado_noSeCae() {
        viewModel.onMiUbicacionClick()
        viewModel.onExplicacionRechazada()
        assertEquals(EstadoPermiso.DENEGADO, viewModel.uiState.value.permisoUbicacion)
        viewModel.onResultadoPermiso(false)
        assertEquals(EstadoPermiso.DENEGADO, viewModel.uiState.value.permisoUbicacion)
    }

    @Test
    fun permisoYaConcedido_centraDirecto() {
        viewModel.onPermisoVerificado(true)
        viewModel.onMiUbicacionClick()
        assertTrue(viewModel.uiState.value.centrarEnUbicacion)
    }

    @Test
    fun actualizar_sincronizaConElServidor() = runTest {
        viewModel.actualizar()
        assertFalse(viewModel.uiState.value.actualizando)
    }
}
