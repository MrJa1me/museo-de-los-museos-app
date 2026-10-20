package com.example.museo_de_los_museos_app.ui.screens.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.CalculadoraEnvio
import com.example.museo_de_los_museos_app.domain.Destino
import com.example.museo_de_los_museos_app.domain.LineaCarrito
import com.example.museo_de_los_museos_app.domain.ModalidadEnvio
import com.example.museo_de_los_museos_app.domain.Validadores
import com.example.museo_de_los_museos_app.domain.ZonaInternacional
import com.example.museo_de_los_museos_app.model.CartItem
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

/**
 * Carrito y cálculo de envío (P4): cantidades, formulario de destino validado, cálculo con
 * CalculadoraEnvio y confirmación de la solicitud (se guarda en la tabla de pedidos).
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
            val resultado = when {
                estado.vacio -> null
                // Solo cursos: no importa el destino, no hay envío
                !estado.requiereEnvio -> CalculadoraEnvio.calcular(estado.lineas, Destino.Nacional(estado.formulario.region), ModalidadEnvio.ESTANDAR)
                destino == null -> null
                else -> CalculadoraEnvio.calcular(estado.lineas, destino, estado.formulario.modalidad)
            }
            estado.copy(resultado = resultado)
        }
    }

    // ---------- Confirmación ----------

    /** Valida todo y, si está correcto, muestra el diálogo con el total final antes de confirmar. */
    fun solicitarConfirmacion() {
        val estado = _uiState.value
        val errores = if (estado.requiereEnvio) validarFormulario(estado.formulario) else ErroresDestino()
        val sinStock = estado.lineas.firstOrNull { Validadores.validarCantidad(it.cantidad.toString(), it.producto) != null }
        _uiState.update {
            it.copy(
                errores = errores,
                productoSinStock = sinStock?.producto?.nombre,
                mostrandoConfirmacion = !errores.hayErrores && sinStock == null && it.resultado != null
            )
        }
    }

    fun cancelarConfirmacion() {
        _uiState.update { it.copy(mostrandoConfirmacion = false) }
    }

    /** Crea la solicitud, descuenta el stock y vacía el carrito del usuario. */
    fun confirmarPedido() {
        val estado = _uiState.value
        val userId = SessionManager.userId ?: return
        val resultado = estado.resultado ?: return
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
        viewModelScope.launch {
            pedidos.crear(pedido)
            estado.lineas.forEach { productos.guardar(it.producto.copy(stock = it.producto.stock - it.cantidad)) }
            carrito.vaciar(userId)
            _uiState.update {
                it.copy(confirmando = false, pedidoConfirmado = pedido.id, formulario = FormularioDestino(), errores = ErroresDestino())
            }
        }
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
