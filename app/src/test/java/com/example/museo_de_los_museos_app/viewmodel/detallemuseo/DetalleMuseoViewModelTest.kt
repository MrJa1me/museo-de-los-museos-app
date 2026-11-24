package com.example.museo_de_los_museos_app.viewmodel.detallemuseo

import com.example.museo_de_los_museos_app.domain.ErrorFicha
import com.example.museo_de_los_museos_app.fakes.FakeFavoriteRepository
import com.example.museo_de_los_museos_app.fakes.MuseosConFallo
import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.SessionManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleMuseoViewModelTest {

    private val museos = MuseosConFallo()
    private val favoritos = FakeFavoriteRepository()
    private val investigador = SeedData.usuarios.first { it.rol == Rol.INVESTIGADOR }
    private val admin = SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR }
    private val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }
    private lateinit var viewModel: DetalleMuseoViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = DetalleMuseoViewModel(museos, favoritos)
    }

    @After
    fun tearDown() {
        SessionManager.cerrarSesion()
        Dispatchers.resetMain()
    }

    private fun llenarFormularioValido() {
        viewModel.onNombreChange("Museo Virtual del Pacífico")
        viewModel.onPaisChange("Chile")
        viewModel.onCiudadChange("Valparaíso")
        viewModel.onLatitudChange("-33,04")
        viewModel.onLongitudChange("-71.61")
        viewModel.onClasificacionChange(ClasificacionMuseo.CIENCIA)
        viewModel.onDescripcionChange("Museo de prueba con una descripción suficientemente larga.")
        viewModel.onEnlaceChange("https://ejemplo.museo.test/m11")
    }

    @Test
    fun cargar_muestraElMuseo_yNoRecargaElMismoId() {
        viewModel.cargar("m01")
        viewModel.cargar("m01")
        val estado = viewModel.uiState.value
        assertEquals("m01", estado.museo?.id)
        assertFalse(estado.cargando)
        assertFalse(estado.noEncontrado)
    }

    @Test
    fun cargar_idInexistente_esNoEncontrado() {
        viewModel.cargar("m99")
        assertTrue(viewModel.uiState.value.noEncontrado)
    }

    @Test
    fun favorito_sinSesion_pideIniciarSesion() {
        viewModel.cargar("m01")
        viewModel.alternarFavorito()
        assertEquals(MensajeDetalle.SIN_SESION, viewModel.uiState.value.mensaje)
        viewModel.onMensajeMostrado()
        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun favorito_conSesion_seAgregaYSeQuita() {
        SessionManager.iniciarSesion(cliente)
        viewModel.cargar("m01")

        viewModel.alternarFavorito()
        assertTrue(viewModel.uiState.value.esFavorito)
        assertEquals(MensajeDetalle.FAVORITO_AGREGADO, viewModel.uiState.value.mensaje)

        viewModel.alternarFavorito()
        assertFalse(viewModel.uiState.value.esFavorito)
        assertEquals(MensajeDetalle.FAVORITO_QUITADO, viewModel.uiState.value.mensaje)
    }

    @Test
    fun editar_soloInvestigador_cargaLaFichaEnElFormulario() {
        SessionManager.iniciarSesion(cliente)
        viewModel.cargar("m01")
        viewModel.onEditar()
        assertFalse(viewModel.uiState.value.editando)

        SessionManager.iniciarSesion(investigador)
        viewModel.onEditar()
        val estado = viewModel.uiState.value
        assertTrue(estado.editando)
        assertEquals(SeedData.museos.first().nombre, estado.formulario.nombre)

        viewModel.onCancelarEdicion()
        assertFalse(viewModel.uiState.value.editando)
    }

    @Test
    fun guardar_conErrores_noLlamaAlServidor_yValidaMientrasEscribe() = runTest {
        SessionManager.iniciarSesion(investigador)
        viewModel.cargar(DetalleMuseoViewModel.ID_NUEVO)
        viewModel.guardar()

        val errores = viewModel.uiState.value.errores
        assertEquals(ErrorFicha.OBLIGATORIO, errores.nombre)
        assertTrue(errores.hayErrores)

        viewModel.onEnlaceChange("ftp://x")
        assertEquals(ErrorFicha.ENLACE_INVALIDO, viewModel.uiState.value.errores.enlace)
        assertEquals(SeedData.museos.size, museos.museos.value.size)
    }

    @Test
    fun guardar_fichaNueva_laCreaPendiente_yCierra() = runTest {
        SessionManager.iniciarSesion(investigador)
        viewModel.cargar(DetalleMuseoViewModel.ID_NUEVO)
        llenarFormularioValido()

        viewModel.guardar()

        val creada = museos.museos.value.last()
        assertEquals("m11", creada.id)
        assertFalse(creada.publicado)
        assertEquals(-33.04, creada.latitud, 0.0001)
        assertTrue(viewModel.uiState.value.fichaGuardada)
        assertTrue(viewModel.uiState.value.cerrar)
    }

    @Test
    fun guardar_edicion_conservaPublicacion() = runTest {
        SessionManager.iniciarSesion(investigador)
        viewModel.cargar("m01")
        viewModel.onEditar()
        viewModel.onNombreChange("Museo Virtual Renovado")

        viewModel.guardar()

        val editado = museos.obtenerPorId("m01")!!
        assertEquals("Museo Virtual Renovado", editado.nombre)
        assertTrue(editado.publicado)
        assertFalse(viewModel.uiState.value.editando)
    }

    @Test
    fun guardar_servidorRechaza_mantieneElFormularioYMuestraError() = runTest {
        SessionManager.iniciarSesion(investigador)
        museos.falla = ErrorRemoto(TipoErrorRemoto.SIN_CONEXION)
        viewModel.cargar(DetalleMuseoViewModel.ID_NUEVO)
        llenarFormularioValido()

        viewModel.guardar()

        val estado = viewModel.uiState.value
        assertEquals(TipoErrorRemoto.SIN_CONEXION, estado.errorRemoto?.tipo)
        assertTrue(estado.editando)
        assertFalse(estado.guardando)
        viewModel.onErrorMostrado()
        assertNull(viewModel.uiState.value.errorRemoto)
    }

    @Test
    fun guardar_clienteNoPuede() = runTest {
        SessionManager.iniciarSesion(cliente)
        viewModel.cargar(DetalleMuseoViewModel.ID_NUEVO)
        llenarFormularioValido()
        viewModel.guardar()
        assertFalse(viewModel.uiState.value.intentoEnvio)
    }

    @Test
    fun eliminar_administrador_confirmaYCierra() = runTest {
        SessionManager.iniciarSesion(admin)
        viewModel.cargar("m02")
        assertTrue(viewModel.uiState.value.puedeEliminar)

        viewModel.onSolicitarEliminacion()
        assertTrue(viewModel.uiState.value.confirmandoEliminacion)
        viewModel.onCancelarEliminacion()
        assertFalse(viewModel.uiState.value.confirmandoEliminacion)

        viewModel.onSolicitarEliminacion()
        viewModel.confirmarEliminacion()

        assertNull(museos.obtenerPorId("m02"))
        assertTrue(viewModel.uiState.value.cerrar)
    }

    @Test
    fun eliminar_falla_muestraElError() = runTest {
        SessionManager.iniciarSesion(investigador)
        viewModel.cargar("m02")
        museos.falla = ErrorRemoto(TipoErrorRemoto.RECHAZADO, "No existe el museo m02")
        viewModel.onSolicitarEliminacion()
        viewModel.confirmarEliminacion()

        assertEquals("No existe el museo m02", viewModel.uiState.value.errorRemoto?.mensajeServidor)
        assertFalse(viewModel.uiState.value.cerrar)
    }

    @Test
    fun eliminar_clienteNoPuedeNiAbrirElDialogo() {
        SessionManager.iniciarSesion(cliente)
        viewModel.cargar("m02")
        viewModel.onSolicitarEliminacion()
        assertFalse(viewModel.uiState.value.confirmandoEliminacion)
    }

    @Test
    fun imagenDeGaleria_seGuardaComoTexto() {
        val uri = mockk<android.net.Uri>()
        every { uri.toString() } returns "content://galeria/1"
        viewModel.onImagenElegida(uri)
        viewModel.onImagenElegida(null)
        assertEquals("content://galeria/1", viewModel.uiState.value.formulario.imagen)
        assertNotNull(viewModel.uiState.value.formulario)
    }
}
