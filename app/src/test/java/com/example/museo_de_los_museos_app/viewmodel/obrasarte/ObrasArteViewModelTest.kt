package com.example.museo_de_los_museos_app.viewmodel.obrasarte

import com.example.museo_de_los_museos_app.fakes.FakeObrasArteRepository
import com.example.museo_de_los_museos_app.model.ObraArte
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
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

@OptIn(ExperimentalCoroutinesApi::class)
class ObrasArteViewModelTest {

    private val obra = ObraArte(1, "Machu Picchu", "Georgia O'Keeffe", "1957", "United States", "Óleo", null)
    private val repositorio = FakeObrasArteRepository(Result.success(listOf(obra)))
    private lateinit var viewModel: ObrasArteViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ObrasArteViewModel(repositorio)
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun iniciar_desdeUnMuseo_buscaSuPaisTraducido_yUnaSolaVez() {
        viewModel.iniciar("Perú")
        viewModel.iniciar("Chile")
        assertEquals(listOf("Peru"), repositorio.consultas)
        assertEquals(listOf(obra), viewModel.uiState.value.obras)
        assertEquals("Perú", viewModel.uiState.value.ultimaBusqueda)
    }

    @Test
    fun iniciar_sinPais_usaLaBusquedaPorDefecto() {
        viewModel.iniciar(null)
        assertEquals(listOf("Mexico"), repositorio.consultas)
    }

    @Test
    fun consultaMuyCorta_noLlamaALaApi() {
        viewModel.onConsultaChange("a")
        viewModel.buscar()
        assertTrue(viewModel.uiState.value.consultaInvalida)
        assertTrue(repositorio.consultas.isEmpty())

        viewModel.onConsultaChange("ab")
        assertFalse(viewModel.uiState.value.consultaInvalida)
    }

    @Test
    fun sugerencia_buscaDeInmediato() {
        viewModel.onSugerencia("Brasil")
        assertEquals(listOf("Brazil"), repositorio.consultas)
    }

    @Test
    fun sinResultados_seInforma() {
        repositorio.respuesta = Result.success(emptyList())
        viewModel.onConsultaChange("zzzz")
        viewModel.buscar()
        assertTrue(viewModel.uiState.value.sinResultados)
    }

    @Test
    fun errorDeRed_seMuestra_yReintentarLoLimpia() {
        repositorio.respuesta = Result.failure(ErrorRemoto(TipoErrorRemoto.SIN_CONEXION))
        viewModel.onConsultaChange("Chile")
        viewModel.buscar()
        assertEquals(TipoErrorRemoto.SIN_CONEXION, viewModel.uiState.value.errorRemoto?.tipo)
        assertFalse(viewModel.uiState.value.sinResultados)

        repositorio.respuesta = Result.success(listOf(obra))
        viewModel.buscar()
        assertNull(viewModel.uiState.value.errorRemoto)
        assertEquals(1, viewModel.uiState.value.obras.size)
    }

    @Test
    fun traduccion_dejaIgualLoQueNoEsUnPais() {
        assertEquals("Van Gogh", ObrasArteViewModel.traducirConsulta(" Van Gogh "))
        assertEquals("Spain", ObrasArteViewModel.traducirConsulta("ESPAÑA"))
    }
}
