package com.example.museo_de_los_museos_app.viewmodel.escanerqr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.LectorQr
import com.example.museo_de_los_museos_app.domain.MotivoQrInvalido
import com.example.museo_de_los_museos_app.domain.ResultadoQr
import com.example.museo_de_los_museos_app.model.QrHistory
import com.example.museo_de_los_museos_app.model.visiblePara
import com.example.museo_de_los_museos_app.repository.MuseumRepository
import com.example.museo_de_los_museos_app.repository.ProductRepository
import com.example.museo_de_los_museos_app.repository.QrHistoryRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.historialQr
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Escáner QR (P3). La cámara la abre el lector de Google desde la pantalla; aquí solo llega el texto leído,
 * se interpreta con LectorQr, se guarda en el historial del usuario y se avisa a la pantalla para ir al resultado.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EscanerQrViewModel(
    private val museos: MuseumRepository = Repositorios.museos,
    private val productos: ProductRepository = Repositorios.productos,
    private val historial: QrHistoryRepository = Repositorios.historialQr
) : ViewModel() {

    private val _uiState = MutableStateFlow(EscanerQrUiState())
    val uiState: StateFlow<EscanerQrUiState> = _uiState.asStateFlow()

    init {
        // El historial cambia solo al cambiar de usuario: cada uno ve únicamente sus escaneos.
        viewModelScope.launch {
            SessionManager.usuarioActual.flatMapLatest { usuario ->
                if (usuario == null) flowOf(null) else historial.observar(usuario.id).map { usuario.id to it }
            }.collect { datos ->
                _uiState.update { it.copy(haySesion = datos != null, historial = datos?.second ?: emptyList()) }
            }
        }
    }

    // ---------- Resultado del lector de Google ----------

    fun onCodigoLeido(texto: String?) = procesar(texto)

    fun onEscaneoCancelado() {
        _uiState.update { it.copy(mensaje = MensajeEscaner.CANCELADO) }
    }

    fun onEscaneoFallido() {
        _uiState.update { it.copy(mensaje = MensajeEscaner.FALLO) }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }

    // ---------- Ingreso manual (para probar sin cámara) ----------

    fun onTextoManualChange(valor: String) {
        _uiState.update { it.copy(textoManual = valor, errorManual = null) }
    }

    fun onUsarEjemplo() {
        _uiState.update { it.copy(textoManual = LectorQr.EJEMPLO_MUSEO, errorManual = null) }
    }

    fun onRevisarManual() {
        val texto = _uiState.value.textoManual
        if (texto.isBlank()) {
            _uiState.update { it.copy(errorManual = MotivoQrInvalido.VACIO) }
            return
        }
        procesar(texto)
    }

    fun onHistorialSeleccionado(item: QrHistory) = procesar(item.contenido)

    fun onNavegacionRealizada() {
        _uiState.update { it.copy(navegarAResultado = false) }
    }

    private fun procesar(texto: String?) {
        val contenido = texto?.trim().orEmpty()
        // Un Cliente que escanea una ficha aún no aprobada recibe "no encontrado"
        val buscarMuseo = { id: String -> museos.obtenerPorId(id)?.takeIf { it.visiblePara(SessionManager.rol) } }
        val resultado = LectorQr.resolver(contenido, buscarMuseo, productos::obtenerPorId)
        UltimoEscaneo.guardar(EscaneoReciente(contenido, resultado))

        val userId = SessionManager.userId
        if (userId != null && contenido.isNotEmpty()) {
            viewModelScope.launch {
                historial.registrar(
                    QrHistory(
                        userId = userId,
                        contenido = contenido,
                        valido = resultado !is ResultadoQr.Error,
                        descripcion = descripcion(resultado)
                    )
                )
            }
        }
        _uiState.update { it.copy(navegarAResultado = true) }
    }

    private fun descripcion(resultado: ResultadoQr): String = when (resultado) {
        is ResultadoQr.MuseoEncontrado -> resultado.museo.nombre
        is ResultadoQr.ProductoEncontrado -> resultado.producto.nombre
        is ResultadoQr.EnlaceValido -> resultado.url
        is ResultadoQr.Error -> ""
    }
}
