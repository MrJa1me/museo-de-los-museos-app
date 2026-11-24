package com.example.museo_de_los_museos_app.viewmodel.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.CalculadoraEnvio
import com.example.museo_de_los_museos_app.domain.Destino
import com.example.museo_de_los_museos_app.domain.LineaCarrito
import com.example.museo_de_los_museos_app.domain.ModalidadEnvio
import com.example.museo_de_los_museos_app.domain.Validadores
import com.example.museo_de_los_museos_app.domain.ZonaInternacional
import com.example.museo_de_los_museos_app.model.CartItem
import com.example.museo_de_los_museos_app.model.ItemStock
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.OrderItem
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.repository.CartRepository
import com.example.museo_de_los_museos_app.repository.OrderRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.museo_de_los_museos_app.remote.aErrorRemoto

/**
 * Carrito y cálculo de envío (P4): cantidades, formulario de destino validado, cálculo con
 * CalculadoraEnvio y confirmación de la solicitud.
 *
 * Al confirmar participan dos microservicios:
 *  1. ms-productos revisa la disponibilidad real (POST /api/productos/disponibilidad);
 *  2. ms-pedidos guarda la solicitud (POST /api/pedidos);
 *  3. ms-productos descuenta el stock (POST /api/productos/descontar-stock).
 * Si algo falla antes del paso 2, el carrito queda intacto.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CarritoViewModel(
    private val productos: ProductRepository = Repositorios.productos,
    private val carrito: CartRepository = Repositorios.carrito,
    private val pedidos: OrderRepository = Repositorios.pedidos
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState: StateFlow<CarritoUiState> = _uiState.asStateFlow()

    init {
        // Ítems del carrito del usuario en sesión (null = no hay sesión)
        val itemsDelUsuario = SessionManager.usuarioActual.flatMapLatest { usuario ->
            if (usuario == null) flowOf(null) else carrito.observar(usuario.id)
        }
        viewModelScope.launch {
            combine(itemsDelUsuario, productos.productos) { items, lista -> items to lista }
                .collect { (items, lista) ->
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            haySesion = items != null,
                            lineas = unirConProductos(items.orEmpty(), lista)
                        )
                    }
                    recalcular()
                }
        }
    }

    // ---------- Cantidades ----------

    fun incrementar(productoId: String) {
        val linea = buscarLinea(productoId) ?: return
        // Valida que una unidad más no supere el stock (el validador cuenta lo que ya hay en el carrito)
        if (Validadores.validarCantidad("1", linea.producto, enCarrito = linea.cantidad) != null) {
            _uiState.update { it.copy(productoSinStock = linea.producto.nombre) }
            return
        }
        cambiarCantidad(productoId, linea.cantidad + 1)
    }

    fun decrementar(productoId: String) {
        val linea = buscarLinea(productoId) ?: return
        cambiarCantidad(productoId, linea.cantidad - 1)
    }

    fun eliminar(productoId: String) = cambiarCantidad(productoId, 0)

    private fun cambiarCantidad(productoId: String, cantidad: Int) {
        val userId = SessionManager.userId ?: return
        _uiState.update { it.copy(productoSinStock = null) }
        viewModelScope.launch { carrito.cambiarCantidad(userId, productoId, cantidad) }
    }

    private fun buscarLinea(productoId: String) = _uiState.value.lineas.firstOrNull { it.producto.id == productoId }

    // ---------- Formulario de destino ----------
    // Al escribir en un campo se borra su error; la validación completa se hace al confirmar.

    fun onNombreChange(valor: String) = actualizarFormulario({ it.copy(nombre = valor) }) { it.copy(nombre = null) }

    fun onDireccionChange(valor: String) = actualizarFormulario({ it.copy(direccion = valor) }) { it.copy(direccion = null) }

    fun onCiudadChange(valor: String) = actualizarFormulario({ it.copy(ciudad = valor) }) { it.copy(ciudad = null) }

    fun onTelefonoChange(valor: String) = actualizarFormulario({ it.copy(telefono = valor) }) { it.copy(telefono = null) }

    fun onTipoEnvioChange(tipo: TipoEnvio) =
        actualizarFormulario({ it.copy(tipo = tipo) }) { it.copy(region = null, pais = null) }

    fun onRegionChange(region: String) = actualizarFormulario({ it.copy(region = region) }) { it.copy(region = null) }

    fun onPaisChange(pais: String) = actualizarFormulario({ it.copy(pais = pais) }) { it.copy(pais = null) }

    fun onModalidadChange(modalidad: ModalidadEnvio) = actualizarFormulario({ it.copy(modalidad = modalidad) }) { it }

    private fun actualizarFormulario(
        cambio: (FormularioDestino) -> FormularioDestino,
        limpiarError: (ErroresDestino) -> ErroresDestino
    ) {
        _uiState.update { it.copy(formulario = cambio(it.formulario), errores = limpiarError(it.errores)) }
        recalcular()
    }

    /** Vuelve a calcular el envío con la CalculadoraEnvio cada vez que cambia el carrito o el destino. */
    private fun recalcular() {
        _uiState.update { estado ->
            val destino = destinoDe(estado.formulario)
            val lineas = estado.lineasDisponibles
            val resultado = when {
                lineas.isEmpty() -> null
                !estado.requiereEnvio -> CalculadoraEnvio.calcular(
                    lineas,
                    Destino.Nacional(estado.formulario.region),
                    ModalidadEnvio.ESTANDAR
                )

                destino == null -> null
                else -> CalculadoraEnvio.calcular(lineas, destino, estado.formulario.modalidad)
            }
            estado.copy(resultado = resultado)
        }
    }

    // ---------- Confirmación ----------

    /** Valida todo y, si está correcto, muestra el diálogo con el total final antes de confirmar. */
    fun solicitarConfirmacion() {
        val estado = _uiState.value
        val errores = if (estado.requiereEnvio) validarFormulario(estado.formulario) else ErroresDestino()
        val sinStock = estado.lineasDisponibles.firstOrNull { Validadores.validarCantidad(it.cantidad.toString(), it.producto) != null }
        _uiState.update {
            it.copy(
                errores = errores,
                productoSinStock = sinStock?.producto?.nombre,
                mostrandoConfirmacion = !errores.hayErrores && sinStock == null && !it.hayNoDisponibles && it.resultado != null
            )
        }
    }

    fun cancelarConfirmacion() {
        _uiState.update { it.copy(mostrandoConfirmacion = false) }
    }

    /** Revisa el stock en el servidor, crea la solicitud, descuenta el stock y vacía el carrito del usuario. */
    fun confirmarPedido() {
        val estado = _uiState.value
        val userId = SessionManager.userId ?: return
        val resultado = estado.resultado ?: return
        if (estado.hayNoDisponibles) {
            _uiState.update { it.copy(mostrandoConfirmacion = false) }
            return
        }
        _uiState.update { it.copy(mostrandoConfirmacion = false, confirmando = true) }

        val pedido = Order(
            id = "sol_${System.currentTimeMillis()}",
            userId = userId,
            items = estado.lineas.map { OrderItem(it.producto.id, it.producto.nombre, it.cantidad, it.producto.precio) },
            subtotal = resultado.subtotal,
            costoEnvio = resultado.costoEnvio,
            total = resultado.total,
            destino = textoDestino(estado),
            modalidad = if (resultado.requiereEnvio) estado.formulario.modalidad.etiqueta else SIN_ENVIO
        )
        val items = estado.lineas.map { ItemStock(it.producto.id, it.cantidad) }

        viewModelScope.launch {
            // 1) El stock guardado en el teléfono puede estar desactualizado: se le pregunta a ms-productos
            val problemas = productos.verificarDisponibilidad(items).getOrElse { error ->
                _uiState.update { it.copy(confirmando = false, errorRemoto = error.aErrorRemoto()) }
                return@launch
            }
            if (problemas.isNotEmpty()) {
                // Trae el stock real para que el carrito lo muestre y avisa qué producto no alcanza
                productos.sincronizar()
                val primero = problemas.first()
                _uiState.update { it.copy(confirmando = false, productoSinStock = primero.nombre.ifBlank { primero.productoId }) }
                return@launch
            }

            // 2) POST /api/pedidos: solo si el microservicio guarda la solicitud se descuenta stock y se vacía el carrito
            pedidos.crear(pedido)
                .onSuccess { creado ->
                    // 3) Si el descuento falla (otro cliente compró justo antes), se vuelve a leer el catálogo
                    productos.descontarStock(items).onFailure { productos.sincronizar() }
                    carrito.vaciar(userId)
                    _uiState.update {
                        it.copy(confirmando = false, pedidoConfirmado = creado.id, formulario = FormularioDestino(), errores = ErroresDestino())
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(confirmando = false, errorRemoto = error.aErrorRemoto()) }
                }
        }
    }

    fun onErrorMostrado() {
        _uiState.update { it.copy(errorRemoto = null) }
    }

    /** Después de la pantalla de éxito, el carrito vuelve a su estado normal. */
    fun onPedidoConfirmadoVisto() {
        _uiState.update { it.copy(pedidoConfirmado = null) }
    }

    companion object {
        const val SIN_ENVIO = "Sin envío"

        /** Une los ítems del carrito con sus productos (descarta los que ya no existen). */
        fun unirConProductos(items: List<CartItem>, productos: List<Product>): List<LineaCarrito> =
            items.mapNotNull { item ->
                productos.firstOrNull { it.id == item.productoId }?.let { LineaCarrito(it, item.cantidad) }
            }.sortedBy { it.producto.nombre }

        fun validarFormulario(f: FormularioDestino) = ErroresDestino(
            nombre = Validadores.validarNombreDestinatario(f.nombre),
            direccion = Validadores.validarDireccion(f.direccion),
            ciudad = Validadores.validarCiudad(f.ciudad),
            telefono = Validadores.validarTelefono(f.telefono),
            region = if (f.tipo == TipoEnvio.NACIONAL) Validadores.validarRegion(f.region) else null,
            pais = if (f.tipo == TipoEnvio.INTERNACIONAL) Validadores.validarPais(f.pais) else null
        )

        /** Destino para la calculadora, o null si falta elegir la región o el país. */
        fun destinoDe(f: FormularioDestino): Destino? = when (f.tipo) {
            TipoEnvio.NACIONAL -> if (Validadores.validarRegion(f.region) == null) Destino.Nacional(f.region) else null
            TipoEnvio.INTERNACIONAL -> ZonaInternacional.desdePais(f.pais)?.let { Destino.Internacional(it) }
        }

        fun textoDestino(estado: CarritoUiState): String {
            if (!estado.requiereEnvio) return "Curso online (sin envío)"
            val f = estado.formulario
            val lugar = if (f.tipo == TipoEnvio.NACIONAL) f.region else f.pais
            return "${f.direccion.trim()}, ${f.ciudad.trim()}, $lugar"
        }
    }
}
