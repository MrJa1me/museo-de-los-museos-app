package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.EstadoPedido
import com.example.museo_de_los_museos_app.model.Order
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.PedidosApi
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.remote.aDto
import com.example.museo_de_los_museos_app.remote.aModelo
import com.example.museo_de_los_museos_app.remote.dto.CambioEstadoDto
import com.example.museo_de_los_museos_app.remote.dto.PedidoDto
import com.example.museo_de_los_museos_app.remote.llamar
import com.example.museo_de_los_museos_app.repository.db.PedidoDao
import com.example.museo_de_los_museos_app.repository.db.aEntidad
import com.example.museo_de_los_museos_app.repository.db.aModelo
import com.example.museo_de_los_museos_app.repository.db.itemsEntidad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Solicitudes de compra con persistencia remota (microservicio ms-pedidos) y copia local (Room).
 * Mismo esquema que [MuseumRepositoryRemoto]: se escribe primero en el servidor y Room guarda su respuesta.
 */
class OrderRepositoryRemoto(
    private val api: PedidosApi,
    private val dao: PedidoDao,
    alcance: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    sincronizarAlIniciar: Boolean = true
) : OrderRepository {

    override val pedidos: StateFlow<List<Order>> =
        combine(dao.observarPedidos(), dao.observarItems()) { pedidos, items ->
            val itemsPorPedido = items.groupBy { it.pedidoId }
            pedidos.map { it.aModelo(itemsPorPedido[it.id].orEmpty()) }
        }.stateIn(alcance, SharingStarted.Eagerly, emptyList())

    private val _conexion = MutableStateFlow(EstadoConexion.SIN_VERIFICAR)
    override val conexion: StateFlow<EstadoConexion> = _conexion.asStateFlow()

    init {
        if (sincronizarAlIniciar) {
            alcance.launch {
                if (dao.contar() == 0) {
                    dao.insertarConItems(PEDIDOS_SEMILLA.map { it.aEntidad() }, PEDIDOS_SEMILLA.flatMap { it.itemsEntidad() })
                }
                sincronizar()
            }
        }
    }

    override fun obtenerPorUsuario(userId: String) = pedidos.value.filter { it.userId == userId }

    override fun obtenerPendientes() = pedidos.value.filter { it.estado == EstadoPedido.PENDIENTE }

    override suspend fun sincronizar(): Result<Unit> = registrar(
        llamar { api.listar() }.map { respuesta ->
            val lista = respuesta.mapNotNull { it.aModelo() }
            dao.reemplazarTodos(lista.map { it.aEntidad() }, lista.flatMap { it.itemsEntidad() })
        }
    )

    override suspend fun crear(pedido: Order): Result<Order> =
        guardarRespuesta(llamar { api.crear(pedido.aDto()) })

    override suspend fun cambiarEstado(id: String, estado: EstadoPedido): Result<Order> =
        guardarRespuesta(llamar { api.cambiarEstado(id, CambioEstadoDto(estado.name)) })

    override suspend fun eliminar(id: String): Result<Unit> = registrar(
        llamar {
            val respuesta = api.eliminar(id)
            if (!respuesta.isSuccessful && respuesta.code() != 404) throw HttpException(respuesta)
        }.map { dao.eliminar(id) }
    )

    private suspend fun guardarRespuesta(resultado: Result<PedidoDto>): Result<Order> =
        registrar(resultado.mapCatching { dto ->
            val pedido = dto.aModelo() ?: throw ErrorRemoto(TipoErrorRemoto.DESCONOCIDO, "Respuesta sin id")
            dao.guardarConItems(pedido.aEntidad(), pedido.itemsEntidad())
            pedido
        })

    private fun <T> registrar(resultado: Result<T>): Result<T> {
        val error = resultado.exceptionOrNull() as? ErrorRemoto
        _conexion.value = if (error?.tipo == TipoErrorRemoto.SIN_CONEXION) EstadoConexion.SIN_CONEXION else EstadoConexion.CONECTADO
        return resultado
    }
}
