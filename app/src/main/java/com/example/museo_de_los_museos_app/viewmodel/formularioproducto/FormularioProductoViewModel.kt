package com.example.museo_de_los_museos_app.viewmodel.formularioproducto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.domain.Validadores
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.ProductRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Formulario de producto del Administrador (P4): alta de un producto nuevo o edición de uno existente.
 * Valida nombre obligatorio, precio mayor que 0, stock o cupos de 0 o más y, si no es curso, el peso.
 * Se guarda en Room a través de ProductRepository.guardar (crea o actualiza según el id).
 */
class FormularioProductoViewModel(
    private val productos: ProductRepository = Repositorios.productos
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormularioProductoUiState())
    val uiState: StateFlow<FormularioProductoUiState> = _uiState.asStateFlow()

    /** Producto que se está editando (null en un alta). Se usa para conservar su id y si está activo. */
    private var original: Product? = null
    private var cargado = false

    init {
        viewModelScope.launch {
            SessionManager.usuarioActual.collect { usuario ->
                _uiState.update { it.copy(esAdministrador = usuario?.rol == Rol.ADMINISTRADOR) }
            }
        }
    }

    /** La pantalla lo llama con el id de la ruta (null = producto nuevo). Solo carga una vez. */
    fun cargar(productoId: String?) {
        if (cargado) return
        cargado = true
        if (productoId == null) return

        _uiState.update { it.copy(cargando = true, esEdicion = true) }
        viewModelScope.launch {
            // Con Room la lista llega vacía al inicio: se espera a que haya datos
            val lista = productos.productos.first { it.isNotEmpty() }
            val producto = lista.firstOrNull { it.id == productoId }
            original = producto
            _uiState.update {
                if (producto == null) it.copy(cargando = false, noEncontrado = true)
                else llenarDesde(it, producto)
            }
        }
    }

    // ---------- Campos ----------
    // Al escribir en un campo se borra su error; la validación completa se hace al guardar.

    fun onNombreChange(valor: String) =
        _uiState.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }

    fun onDescripcionChange(valor: String) = _uiState.update { it.copy(descripcion = valor) }

    fun onCategoriaChange(categoria: CategoriaProducto) =
        _uiState.update { it.copy(categoria = categoria, errores = it.errores.copy(peso = null)) }

    fun onPrecioChange(valor: String) =
        _uiState.update { it.copy(precio = soloDigitos(valor), errores = it.errores.copy(precio = null)) }

    fun onStockChange(valor: String) =
        _uiState.update { it.copy(stock = soloDigitos(valor), errores = it.errores.copy(stock = null)) }

    fun onPesoChange(valor: String) =
        _uiState.update { it.copy(peso = valor.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(6), errores = it.errores.copy(peso = null)) }

    private fun soloDigitos(valor: String) = valor.filter { it.isDigit() }.take(7)

    // ---------- Guardar ----------

    fun guardar() {
        val estado = _uiState.value
        if (!estado.esAdministrador) return

        val errores = validarFormulario(estado)
        if (errores.hayErrores) {
            _uiState.update { it.copy(errores = errores) }
            return
        }
        _uiState.update { it.copy(guardando = true) }
        val producto = construirProducto(estado, original)
        viewModelScope.launch {
            productos.guardar(producto)
            _uiState.update { it.copy(guardando = false, guardado = true) }
        }
    }

    companion object {
        fun validarFormulario(estado: FormularioProductoUiState) = ErroresProducto(
            nombre = Validadores.validarNombreProducto(estado.nombre),
            precio = Validadores.validarPrecio(estado.precio),
            stock = Validadores.validarStock(estado.stock),
            peso = Validadores.validarPeso(estado.peso, estado.categoria)
        )

        /** Arma el producto con los datos del formulario (en una edición conserva id y si está activo). */
        fun construirProducto(estado: FormularioProductoUiState, original: Product?) = Product(
            id = original?.id ?: nuevoId(estado.categoria),
            nombre = estado.nombre.trim(),
            categoria = estado.categoria,
            precio = estado.precio.trim().toInt(),
            stock = estado.stock.trim().toInt(),
            pesoKg = if (estado.esCurso) null else Validadores.convertirDecimal(estado.peso),
            descripcion = estado.descripcion.trim(),
            activo = original?.activo ?: true
        )

        /** Id con el prefijo de la categoría, como en SeedData (c = curso, p = publicación, m = material, a = artículo). */
        fun nuevoId(categoria: CategoriaProducto): String {
            val prefijo = when (categoria) {
                CategoriaProducto.CURSO -> "c"
                CategoriaProducto.PUBLICACION -> "p"
                CategoriaProducto.MATERIAL_CONSERVACION -> "m"
                CategoriaProducto.ARTICULO_MUSEOLOGICO -> "a"
            }
            return "$prefijo${System.currentTimeMillis()}"
        }

        private fun llenarDesde(estado: FormularioProductoUiState, p: Product) = estado.copy(
            cargando = false,
            nombre = p.nombre,
            descripcion = p.descripcion,
            categoria = p.categoria,
            precio = p.precio.toString(),
            stock = p.stock.toString(),
            peso = p.pesoKg?.toString()?.replace('.', ',').orEmpty()
        )
    }
}
