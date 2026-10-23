package com.example.museo_de_los_museos_app.viewmodel.detallemuseo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.ValidadorFichaMuseo
import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.FavoriteRepository
import com.example.museo_de_los_museos_app.repository.MuseumRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.favoritos
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Detalle del museo (P3): favorito por usuario, enlace al recorrido virtual y,
 * para el Investigador, el formulario para crear o editar la ficha. Toda la validación vive aquí.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleMuseoViewModel(
    private val museos: MuseumRepository = Repositorios.museos,
    private val favoritos: FavoriteRepository = Repositorios.favoritos
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleMuseoUiState())
    val uiState: StateFlow<DetalleMuseoUiState> = _uiState.asStateFlow()

    private var museoId: String? = null

    /** La pantalla lo llama con el id de la ruta. Si ya se cargó ese id, no hace nada (ej. al girar el teléfono). */
    fun cargar(id: String) {
        if (museoId == id) return
        museoId = id
        val esNuevo = id == ID_NUEVO
        _uiState.update { it.copy(esNuevo = esNuevo, editando = esNuevo, cargando = !esNuevo) }

        val favoritosDelUsuario = SessionManager.usuarioActual.flatMapLatest { usuario ->
            if (usuario == null) flowOf(emptySet()) else favoritos.observar(usuario.id)
        }
        viewModelScope.launch {
            combine(museos.museos, favoritosDelUsuario, SessionManager.usuarioActual) { lista, favs, usuario ->
                Triple(lista, favs, usuario)
            }.collect { (lista, favs, usuario) ->
                val museo = lista.firstOrNull { it.id == id }
                _uiState.update {
                    it.copy(
                        museo = museo,
                        // Con Room la lista llega vacía al inicio: solo es "no encontrado" si ya hay datos.
                        cargando = !esNuevo && museo == null && lista.isEmpty(),
                        noEncontrado = !esNuevo && museo == null && lista.isNotEmpty(),
                        esFavorito = id in favs,
                        esInvestigador = usuario?.rol == Rol.INVESTIGADOR
                    )
                }
            }
        }
    }

    // ---------- Favoritos ----------

    fun alternarFavorito() {
        val id = museoId ?: return
        val userId = SessionManager.userId
        if (userId == null) {
            _uiState.update { it.copy(mensaje = MensajeDetalle.SIN_SESION) }
            return
        }
        val eraFavorito = _uiState.value.esFavorito
        _uiState.update {
            it.copy(mensaje = if (eraFavorito) MensajeDetalle.FAVORITO_QUITADO else MensajeDetalle.FAVORITO_AGREGADO)
        }
        viewModelScope.launch { favoritos.alternar(userId, id) }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    // ---------- Formulario de ficha (solo Investigador) ----------

    fun onEditar() {
        val museo = _uiState.value.museo ?: return
        if (!_uiState.value.esInvestigador) return
        _uiState.update {
            it.copy(
                editando = true,
                fichaGuardada = false,
                intentoEnvio = false,
                errores = ErroresFicha(),
                formulario = FormularioFicha(
                    nombre = museo.nombre,
                    pais = museo.pais,
                    ciudad = museo.ciudad,
                    latitud = museo.latitud.toString(),
                    longitud = museo.longitud.toString(),
                    clasificacion = museo.clasificacion,
                    descripcion = museo.descripcion,
                    enlace = museo.enlace
                )
            )
        }
    }

    fun onCancelarEdicion() {
        _uiState.update { if (it.esNuevo) it.copy(cerrar = true) else it.copy(editando = false, intentoEnvio = false) }
    }

    fun onNombreChange(valor: String) = actualizarFormulario { copy(nombre = valor) }
    fun onPaisChange(valor: String) = actualizarFormulario { copy(pais = valor) }
    fun onCiudadChange(valor: String) = actualizarFormulario { copy(ciudad = valor) }
    fun onLatitudChange(valor: String) = actualizarFormulario { copy(latitud = valor) }
    fun onLongitudChange(valor: String) = actualizarFormulario { copy(longitud = valor) }
    fun onClasificacionChange(valor: ClasificacionMuseo) = actualizarFormulario { copy(clasificacion = valor) }
    fun onDescripcionChange(valor: String) = actualizarFormulario { copy(descripcion = valor) }
    fun onEnlaceChange(valor: String) = actualizarFormulario { copy(enlace = valor) }

    private fun actualizarFormulario(cambio: FormularioFicha.() -> FormularioFicha) {
        _uiState.update { actual ->
            val formulario = actual.formulario.cambio()
            actual.copy(
                formulario = formulario,
                fichaGuardada = false,
                errores = if (actual.intentoEnvio) validar(formulario) else actual.errores
            )
        }
    }

    private fun validar(formulario: FormularioFicha) = ErroresFicha(
        nombre = ValidadorFichaMuseo.nombre(formulario.nombre),
        pais = ValidadorFichaMuseo.lugar(formulario.pais),
        ciudad = ValidadorFichaMuseo.lugar(formulario.ciudad),
        latitud = ValidadorFichaMuseo.latitud(formulario.latitud),
        longitud = ValidadorFichaMuseo.longitud(formulario.longitud),
        descripcion = ValidadorFichaMuseo.descripcion(formulario.descripcion),
        enlace = ValidadorFichaMuseo.enlace(formulario.enlace)
    )

    fun guardar() {
        val actual = _uiState.value
        if (actual.guardando || !actual.esInvestigador) return

        val errores = validar(actual.formulario)
        _uiState.update { it.copy(intentoEnvio = true, errores = errores) }
        if (errores.hayErrores) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            delay(600) // carga simulada (patrón de la Guía 12)

            val formulario = actual.formulario
            val id = actual.museo?.id ?: ValidadorFichaMuseo.siguienteId(museos.museos.value.map { it.id })
            val nuevo = Museum(
                id = id,
                nombre = formulario.nombre.trim(),
                pais = formulario.pais.trim(),
                ciudad = formulario.ciudad.trim(),
                latitud = ValidadorFichaMuseo.convertirDecimal(formulario.latitud) ?: 0.0,
                longitud = ValidadorFichaMuseo.convertirDecimal(formulario.longitud) ?: 0.0,
                clasificacion = formulario.clasificacion,
                descripcion = formulario.descripcion.trim(),
                enlace = formulario.enlace.trim()
            )
            // Al editar se conservan imagen y estado de publicación; una ficha nueva queda pendiente de aprobación
            museos.guardar(
                actual.museo?.let { nuevo.copy(proyecto = it.proyecto, imagen = it.imagen, publicado = it.publicado) } ?: nuevo
            )

            _uiState.update {
                it.copy(guardando = false, editando = it.esNuevo, fichaGuardada = true, cerrar = it.esNuevo)
            }
        }
    }

    companion object {
        /** Id especial de la ruta para crear una ficha nueva:  Routes.DetalleMuseo.crear(ID_NUEVO) */
        const val ID_NUEVO = "nuevo"
    }
}
