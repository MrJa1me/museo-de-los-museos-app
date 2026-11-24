package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.ItemStock
import com.example.museo_de_los_museos_app.model.ProblemaStock
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.ProductosApi
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.remote.aDto
import com.example.museo_de_los_museos_app.remote.aModelo
import com.example.museo_de_los_museos_app.remote.dto.CambioActivoDto
import com.example.museo_de_los_museos_app.remote.dto.ProductoDto
import com.example.museo_de_los_museos_app.remote.llamar
import com.example.museo_de_los_museos_app.repository.db.ProductoDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Catálogo con persistencia remota (microservicio ms-productos) y copia local (Room).
 * Mismo esquema que [MuseumRepositoryRemoto]:
 *
 * - Lectura: la UI siempre observa Room, así el catálogo se ve aunque el servidor esté apagado.
 *   [sincronizar] reemplaza esa copia con lo que tiene el servidor.
 * - Escritura: primero el microservicio; solo si responde OK se guarda en Room su respuesta.
 * - Stock: el stock real vive en el servidor, por eso el carrito pregunta con [verificarDisponibilidad]
 *   antes de confirmar y luego llama a [descontarStock].
 */
class ProductRepositoryRemoto(
    private val api: ProductosApi,
    private val dao: ProductoDao,
    alcance: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    sincronizarAlIniciar: Boolean = true
) : ProductRepository {

    override val productos: StateFlow<List<Product>> =
        dao.observarTodos()
            .map { filas -> filas.map { it.aModelo() } }
            .stateIn(alcance, SharingStarted.Eagerly, emptyList())

    private val _conexion = MutableStateFlow(EstadoConexion.SIN_VERIFICAR)
    override val conexion: StateFlow<EstadoConexion> = _conexion.asStateFlow()

    init {
        if (sincronizarAlIniciar) {
            alcance.launch {
                // Primera vez sin servidor: la app igual muestra los 10 productos de prueba
                if (dao.contar() == 0) dao.insertarSiNoExisten(SeedData.productos.map { it.aEntidad() })
                sincronizar()
            }
        }
    }

    override fun obtenerPorId(id: String): Product? = productos.value.firstOrNull { it.id == id }

    override suspend fun sincronizar(): Result<Unit> = registrar(
        llamar { api.listar() }.map { respuesta ->
            dao.reemplazarTodos(respuesta.mapNotNull { it.aModelo()?.aEntidad() })
        }
    )

    override suspend fun crear(producto: Product): Result<Product> =
        guardarRespuesta(llamar { api.crear(producto.aDto(incluirId = false)) })

    override suspend fun actualizar(producto: Product): Result<Product> =
        guardarRespuesta(llamar { api.actualizar(producto.id, producto.aDto()) })

    override suspend fun cambiarActivo(id: String, activo: Boolean): Result<Product> =
        guardarRespuesta(llamar { api.cambiarActivo(id, CambioActivoDto(activo)) })

    override suspend fun verificarDisponibilidad(items: List<ItemStock>): Result<List<ProblemaStock>> = registrar(
        llamar { api.disponibilidad(items.aDto()) }.map { respuesta ->
            respuesta.problemas.orEmpty().map { it.aModelo() }
        }
    )

    override suspend fun descontarStock(items: List<ItemStock>): Result<Unit> = registrar(
        llamar { api.descontarStock(items.aDto()) }.map { actualizados ->
            // El servidor devuelve los productos con el stock nuevo: se guardan en la copia local
            actualizados.mapNotNull { it.aModelo() }.forEach { dao.guardar(it.aEntidad()) }
        }
    )

    /** Guarda en Room el producto tal como lo devolvió el servidor (con su id y valores definitivos). */
    private suspend fun guardarRespuesta(resultado: Result<ProductoDto>): Result<Product> =
        registrar(resultado.mapCatching { dto ->
            val producto = dto.aModelo() ?: throw ErrorRemoto(TipoErrorRemoto.DESCONOCIDO, "Respuesta sin id")
            dao.guardar(producto.aEntidad())
            producto
        })

    /** Actualiza el aviso de conexión: solo "sin conexión" si el servidor no respondió (un 400/409 sí es respuesta). */
    private fun <T> registrar(resultado: Result<T>): Result<T> {
        val error = resultado.exceptionOrNull() as? ErrorRemoto
        _conexion.value = if (error?.tipo == TipoErrorRemoto.SIN_CONEXION) EstadoConexion.SIN_CONEXION else EstadoConexion.CONECTADO
        return resultado
    }
}
