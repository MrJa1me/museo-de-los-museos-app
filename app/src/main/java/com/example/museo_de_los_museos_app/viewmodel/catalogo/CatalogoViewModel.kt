package com.example.museo_de_los_museos_app.viewmodel.catalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.repository.ProductRepository
import com.example.museo_de_los_museos_app.repository.Repositorios
import com.example.museo_de_los_museos_app.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Catálogo de cursos y productos. La UI solo envía eventos (búsqueda, categoría, disponibilidad)
 * y muestra [uiState]; el filtrado se hace aquí.
 */
class CatalogoViewModel(
    private val repositorio: ProductRepository = Repositorios.productos
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogoUiState())
    val uiState: StateFlow<CatalogoUiState> = _uiState.asStateFlow()

    /** Copia de todos los productos del repositorio, sin filtrar. */
    private var todos: List<Product> = emptyList()

    init {
        // Si el Administrador agrega o edita un producto, el catálogo se actualiza solo.
        viewModelScope.launch {
            repositorio.productos.collect { lista ->
                todos = lista
                aplicarFiltros()
            }
        }
        viewModelScope.launch {
            SessionManager.usuarioActual.collect { usuario ->
                _uiState.update { it.copy(esAdministrador = usuario?.rol == Rol.ADMINISTRADOR) }
                aplicarFiltros()
            }
        }
    }

    fun onBusquedaChange(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        aplicarFiltros()
    }

    /** Al tocar la categoría que ya está seleccionada, se quita el filtro. */
    fun onCategoriaSeleccionada(categoria: CategoriaProducto?) {
        _uiState.update {
            it.copy(categoriaSeleccionada = if (it.categoriaSeleccionada == categoria) null else categoria)
        }
        aplicarFiltros()
    }

    fun onSoloDisponiblesChange(soloDisponibles: Boolean) {
        _uiState.update { it.copy(soloDisponibles = soloDisponibles) }
        aplicarFiltros()
    }

    fun limpiarFiltros() {
        _uiState.update { it.copy(busqueda = "", categoriaSeleccionada = null, soloDisponibles = false) }
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        _uiState.update { estado -> estado.copy(productos = filtrar(todos, estado)) }
    }

    companion object {
        /** Función pura (sin estado) para que sea fácil de probar. */
        fun filtrar(productos: List<Product>, estado: CatalogoUiState): List<Product> {
            val texto = estado.busqueda.trim()
            return productos.filter { producto ->
                (estado.esAdministrador || producto.activo) &&
                    (estado.categoriaSeleccionada == null || producto.categoria == estado.categoriaSeleccionada) &&
                    (!estado.soloDisponibles || producto.disponible) &&
                    (texto.isEmpty() ||
                        producto.nombre.contains(texto, ignoreCase = true) ||
                        producto.descripcion.contains(texto, ignoreCase = true))
            }
        }
    }
}
