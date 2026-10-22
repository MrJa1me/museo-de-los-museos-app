package com.example.museo_de_los_museos_app.viewmodel.detalleproducto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.Validadores
import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.navigation.Routes
import com.example.museo_de_los_museos_app.repository.CartRepository
import com.example.museo_de_los_museos_app.repository.NotificationRepository
import com.example.museo_de_los_museos_app.repository.ProductRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import com.example.museo_de_los_museos_app.repository.carrito
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Detalle y solicitud de compra (P4): elegir cantidad, validar stock, agregar al carrito
 * y, en los cursos, solicitar información. Toda la validación vive aquí (Validadores), no en la UI.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DetalleProductoViewModel(
    private val productos: ProductRepository = Repositorios.productos,
    private val carrito: CartRepository = Repositorios.carrito,
    private val notificaciones: NotificationRepository = Repositorios.notificaciones
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleProductoUiState())
    val uiState: StateFlow<DetalleProductoUiState> = _uiState.asStateFlow()

    private var productoId: String? = null

    /** La pantalla lo llama con el id de la ruta. Si ya se cargó ese id, no hace nada (ej. al girar el teléfono). */
    fun cargar(id: String) {
        if (productoId == id) return
        productoId = id

        // Cuántas unidades de este producto tiene ya el usuario (null = no hay sesión)
        val enCarritoDelUsuario = SessionManager.usuarioActual.flatMapLatest { usuario ->
            if (usuario == null) flowOf(null)
            else carrito.observar(usuario.id).map { items -> items.firstOrNull { it.productoId == id }?.cantidad ?: 0 }
        }
        viewModelScope.launch {
            combine(productos.productos, enCarritoDelUsuario) { lista, enCarrito -> lista to enCarrito }
                .collect { (lista, enCarrito) ->
                    val producto = lista.firstOrNull { it.id == id }
                    _uiState.update {
                        it.copy(
                            producto = producto,
                            // Con Room la lista llega vacía al inicio: solo es "no encontrado" si ya hay datos.
                            cargando = producto == null && lista.isEmpty(),
                            noEncontrado = producto == null && lista.isNotEmpty(),
                            haySesion = enCarrito != null,
                            enCarrito = enCarrito ?: 0
                        )
                    }
                }
        }
    }

    // ---------- Cantidad ----------

    fun onCantidadChange(texto: String) {
        // Solo dígitos y máximo 3 caracteres; el mensaje de error lo decide el validador
        val limpio = texto.filter { it.isDigit() }.take(3)
        _uiState.update { it.copy(cantidad = limpio, errorCantidad = validar(limpio)) }
    }

    fun incrementar() = cambiarEn(+1)

    fun decrementar() = cambiarEn(-1)

    private fun cambiarEn(delta: Int) {
        val actual = _uiState.value.cantidad.toIntOrNull() ?: 0
        onCantidadChange((actual + delta).coerceAtLeast(1).toString())
    }

    private fun validar(texto: String) =
        _uiState.value.producto?.let { Validadores.validarCantidad(texto, it, _uiState.value.enCarrito) }

    // ---------- Acciones ----------

    fun agregarAlCarrito() {
        val estado = _uiState.value
        val producto = estado.producto ?: return
        val userId = SessionManager.userId ?: return

        val error = validar(estado.cantidad)
        if (error != null) {
            _uiState.update { it.copy(errorCantidad = error) }
            return
        }
        _uiState.update { it.copy(agregando = true) }
        viewModelScope.launch {
            carrito.agregar(userId, producto.id, estado.cantidad.toInt())
            _uiState.update {
                it.copy(agregando = false, cantidad = "1", errorCantidad = null, mensaje = MensajeDetalleProducto.AGREGADO_AL_CARRITO)
            }
        }
    }

    /** Solo para cursos: deja un aviso en Notificaciones (P2) confirmando la solicitud. */
    fun solicitarInformacion() {
        val producto = _uiState.value.producto ?: return
        val userId = SessionManager.userId ?: return
        notificaciones.agregarAviso(
            NotificationItem(
                id = "info_${producto.id}_${System.currentTimeMillis()}",
                userId = userId,
                titulo = "Solicitud de información recibida",
                mensaje = "Pronto te enviaremos el programa, fechas y requisitos del curso \"${producto.nombre}\".",
                ruta = Routes.DetalleProducto.crear(producto.id)
            )
        )
        _uiState.update { it.copy(mensaje = MensajeDetalleProducto.INFORMACION_SOLICITADA) }
    }

    fun onMensajeMostrado() {
        _uiState.update { it.copy(mensaje = null) }
    }
}
