package com.example.museo_de_los_museos_app.fakes

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.ObraArte
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.model.Preferencias
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.QrHistory
import com.example.museo_de_los_museos_app.model.User
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.repository.EstadoConexion
import com.example.museo_de_los_museos_app.repository.FavoriteRepository
import com.example.museo_de_los_museos_app.repository.InMemoryMuseumRepository
import com.example.museo_de_los_museos_app.repository.InMemoryOrderRepository
import com.example.museo_de_los_museos_app.repository.InMemoryProductRepository
import com.example.museo_de_los_museos_app.repository.MuseumRepository
import com.example.museo_de_los_museos_app.repository.ObrasArteRepository
import com.example.museo_de_los_museos_app.repository.OrderRepository
import com.example.museo_de_los_museos_app.repository.PreferenciasRepository
import com.example.museo_de_los_museos_app.repository.ProductRepository
import com.example.museo_de_los_museos_app.repository.QrHistoryRepository
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.repository.UserRepository
import com.example.museo_de_los_museos_app.repository.agregarSinDuplicados
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * Dobles de prueba ("fakes"): implementaciones simples en memoria de las interfaces de repositorio.
 * Permiten probar los ViewModels sin Room, sin red y sin Android. Para las clases que hablan con
 * Retrofit o con los DAO se usa MockK (ver remote/ y repository/).
 */

class FakeUserRepository(inicial: List<User> = SeedData.usuarios) : UserRepository {
    val usuarios = inicial.toMutableList()
    override suspend fun obtenerTodos(): List<User> = usuarios.toList()
    override suspend fun registrar(usuario: User) { usuarios += usuario }
    override suspend fun actualizar(usuario: User) {
        usuarios.replaceAll { if (it.id == usuario.id) usuario else it }
    }
}

class FakePreferenciasRepository : PreferenciasRepository {
    val guardadas = MutableStateFlow<Map<String, Preferencias>>(emptyMap())
    override suspend fun obtener(userId: String) = guardadas.value[userId] ?: Preferencias(userId)
    override fun observar(userId: String): Flow<Preferencias> = guardadas.map { it[userId] ?: Preferencias(userId) }
    override suspend fun guardar(preferencias: Preferencias) {
        guardadas.update { it + (preferencias.userId to preferencias) }
    }
}

class FakeFavoriteRepository : FavoriteRepository {
    val favoritos = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    override fun observar(userId: String): Flow<Set<String>> = favoritos.map { it[userId].orEmpty() }
    override suspend fun alternar(userId: String, museoId: String) {
        favoritos.update { mapa ->
            val actuales = mapa[userId].orEmpty()
            mapa + (userId to if (museoId in actuales) actuales - museoId else actuales + museoId)
        }
    }
}

class FakeQrHistoryRepository : QrHistoryRepository {
    val historial = MutableStateFlow<List<QrHistory>>(emptyList())
    override fun observar(userId: String): Flow<List<QrHistory>> = historial.map { lista -> lista.filter { it.userId == userId } }
    override suspend fun registrar(item: QrHistory) {
        historial.update { it.agregarSinDuplicados(item) }
    }
}

class FakeObrasArteRepository(var respuesta: Result<List<ObraArte>> = Result.success(emptyList())) : ObrasArteRepository {
    val consultas = mutableListOf<String>()
    override suspend fun buscar(consulta: String): Result<List<ObraArte>> {
        consultas += consulta
        return respuesta
    }
}

/** Repositorio de museos en memoria al que se le puede ordenar que falle, como si el microservicio respondiera con error. */
class MuseosConFallo(private val base: InMemoryMuseumRepository = InMemoryMuseumRepository()) : MuseumRepository by base {
    var falla: ErrorRemoto? = null
    private fun <T> o(resultado: suspend () -> Result<T>): suspend () -> Result<T> = { falla?.let { Result.failure(it) } ?: resultado() }
    override suspend fun crear(museo: Museum) = o { base.crear(museo) }()
    override suspend fun actualizar(museo: Museum) = o { base.actualizar(museo) }()
    override suspend fun aprobar(id: String) = o { base.aprobar(id) }()
    override suspend fun eliminar(id: String) = o { base.eliminar(id) }()
    override suspend fun sincronizar() = o { base.sincronizar() }()
}

/** Igual que [MuseosConFallo], para las solicitudes de compra. */
class PedidosConFallo(private val base: InMemoryOrderRepository = InMemoryOrderRepository()) : OrderRepository by base {
    var falla: ErrorRemoto? = null
    private fun <T> o(resultado: suspend () -> Result<T>): suspend () -> Result<T> = { falla?.let { Result.failure(it) } ?: resultado() }
    override suspend fun crear(pedido: Order) = o { base.crear(pedido) }()
    override suspend fun cambiarEstado(id: String, estado: EstadoPedido) = o { base.cambiarEstado(id, estado) }()
    override suspend fun eliminar(id: String) = o { base.eliminar(id) }()
    override suspend fun sincronizar() = o { base.sincronizar() }()
}

/**
 * Catálogo en memoria al que se le puede ordenar que falle (como si ms-productos estuviera apagado o rechazara).
 */
class ProductosConFallo(
    private val base: InMemoryProductRepository = InMemoryProductRepository()
) : ProductRepository by base {
    var falla: ErrorRemoto? = null
    var sincronizaciones = 0
        private set
    private val _conexion = MutableStateFlow(EstadoConexion.CONECTADO)
    override val conexion: StateFlow<EstadoConexion> = _conexion

    private suspend fun <T> o(resultado: suspend () -> Result<T>): Result<T> {
        val error = falla
        _conexion.value = if (error?.tipo == TipoErrorRemoto.SIN_CONEXION) EstadoConexion.SIN_CONEXION else EstadoConexion.CONECTADO
        return if (error != null) Result.failure(error) else resultado()
    }

    override suspend fun sincronizar(): Result<Unit> { sincronizaciones++; return o { base.sincronizar() } }
    override suspend fun crear(producto: Product) = o { base.crear(producto) }
    override suspend fun actualizar(producto: Product) = o { base.actualizar(producto) }
    override suspend fun cambiarActivo(id: String, activo: Boolean) = o { base.cambiarActivo(id, activo) }
}
