package com.example.museo_de_los_museos_app.viewmodel.favoritos

import com.example.museo_de_los_museos_app.fakes.FakeFavoriteRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritosViewModelTest {

    private val favoritos = FakeFavoriteRepository()
    private val cliente1 = SeedData.usuarios.first { it.id == "u03" }
    private val cliente2 = SeedData.usuarios.first { it.id == "u04" }
    private lateinit var viewModel: FavoritosViewModel

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        favoritos.alternar(cliente1.id, "m05")
        favoritos.alternar(cliente1.id, "m01")
        viewModel = FavoritosViewModel(InMemoryMuseumRepository(), favoritos)
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    @Test
    fun sinSesion_noMuestraNada() {
        assertFalse(viewModel.uiState.value.haySesion)
        assertTrue(viewModel.uiState.value.museos.isEmpty())
    }

    @Test
    fun cadaUsuarioVeSoloSusFavoritos_ordenadosPorNombre() {
        SessionManager.iniciarSesion(cliente1)
        val nombres = viewModel.uiState.value.museos.map { it.nombre }
        assertEquals(nombres.sorted(), nombres)
        assertEquals(setOf("m01", "m05"), viewModel.uiState.value.museos.map { it.id }.toSet())

        SessionManager.iniciarSesion(cliente2)
        assertTrue(viewModel.uiState.value.museos.isEmpty())
    }

    @Test
    fun quitar_actualizaLaLista() {
        SessionManager.iniciarSesion(cliente1)
        viewModel.quitar("m05")
        assertEquals(listOf("m01"), viewModel.uiState.value.museos.map { it.id })
    }
}
