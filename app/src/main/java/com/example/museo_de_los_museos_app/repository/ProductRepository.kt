package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.domain.IdsProducto
import com.example.museo_de_los_museos_app.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Contrato de acceso al catálogo. Las pantallas dependen de esta interfaz, no de la implementación:
 * en la app real es [ProductRepositoryRemoto] (microservicio ms-productos + copia en Room) y en las
 * pruebas, [InMemoryProductRepository].
 *
 * Las escrituras son suspend y devuelven Result: el ViewModel sabe si el servidor aceptó la operación.
 */
interface ProductRepository {
    val productos: StateFlow<List<Product>>
    val conexion: StateFlow<EstadoConexion>
    fun obtenerPorId(id: String): Product?

    /** Descarga el catálogo del microservicio y actualiza la copia local. */
    suspend fun sincronizar(): Result<Unit>

    /** Alta del Administrador: el servidor asigna el id según la categoría. */
    suspend fun crear(producto: Product): Result<Product>

    /** Edición del Administrador (no cambia si está activo). */
    suspend fun actualizar(producto: Product): Result<Product>

    /** Desactivar esconde el producto a los Clientes sin borrarlo. */
    suspend fun cambiarActivo(id: String, activo: Boolean): Result<Product>
}

/** Implementación en memoria, cargada con [SeedData]. Se usa en las pruebas de los ViewModels. */
class InMemoryProductRepository(inicial: List<Product> = SeedData.productos) : ProductRepository {
    private val _productos = MutableStateFlow(inicial)
    override val productos: StateFlow<List<Product>> = _productos.asStateFlow()
    override val conexion: StateFlow<EstadoConexion> = MutableStateFlow(EstadoConexion.CONECTADO)

    override fun obtenerPorId(id: String): Product? = _productos.value.firstOrNull { it.id == id }

    override suspend fun sincronizar(): Result<Unit> = Result.success(Unit)

    override suspend fun crear(producto: Product): Result<Product> {
        val nuevo = producto.copy(
            id = IdsProducto.siguienteId(producto.categoria, _productos.value.map { it.id }),
            activo = true
        )
        _productos.update { it + nuevo }
        return Result.success(nuevo)
    }

    override suspend fun actualizar(producto: Product): Result<Product> {
        val actual = obtenerPorId(producto.id) ?: return Result.failure(NoSuchElementException(producto.id))
        val editado = producto.copy(activo = actual.activo)
        reemplazar(editado)
        return Result.success(editado)
    }

    override suspend fun cambiarActivo(id: String, activo: Boolean): Result<Product> {
        val actual = obtenerPorId(id) ?: return Result.failure(NoSuchElementException(id))
        val cambiado = actual.copy(activo = activo)
        reemplazar(cambiado)
        return Result.success(cambiado)
    }

    private fun reemplazar(producto: Product) {
        _productos.update { lista -> lista.map { if (it.id == producto.id) producto else it } }
    }
}
