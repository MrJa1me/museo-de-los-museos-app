package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.remote.ErrorRemoto
import com.example.museo_de_los_museos_app.remote.MuseosApi
import com.example.museo_de_los_museos_app.remote.TipoErrorRemoto
import com.example.museo_de_los_museos_app.remote.aDto
import com.example.museo_de_los_museos_app.remote.aModelo
import com.example.museo_de_los_museos_app.remote.dto.MuseoDto
import com.example.museo_de_los_museos_app.remote.llamar
import com.example.museo_de_los_museos_app.repository.db.MuseoDao
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
import retrofit2.HttpException

/**
 * Fichas de museos con persistencia remota (microservicio ms-museos) y copia local (Room).
 *
 * - Lectura: la UI siempre observa Room (funciona sin internet). [sincronizar] reemplaza esa copia
 *   con lo que tiene el servidor.
 * - Escritura: primero el microservicio; solo si responde OK se actualiza Room con su respuesta.
 *   Así la base de datos del servidor es la fuente de verdad y la app nunca muestra algo que el servidor rechazó.
 */
class MuseumRepositoryRemoto(
    private val api: MuseosApi,
    private val dao: MuseoDao,
    alcance: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    sincronizarAlIniciar: Boolean = true
) : MuseumRepository {

    override val museos: StateFlow<List<Museum>> =
        dao.observarTodos()
            .map { filas -> filas.map { it.aModelo() } }
            .stateIn(alcance, SharingStarted.Eagerly, emptyList())

    private val _conexion = MutableStateFlow(EstadoConexion.SIN_VERIFICAR)
    override val conexion: StateFlow<EstadoConexion> = _conexion.asStateFlow()

    init {
        if (sincronizarAlIniciar) {
            alcance.launch {
                // Primera vez sin servidor: la app igual muestra los 10 museos de prueba
                if (dao.contar() == 0) dao.insertarSiNoExisten(SeedData.museos.map { it.aEntidad() })
                sincronizar()
            }
        }
    }

    override fun obtenerPorId(id: String): Museum? = museos.value.firstOrNull { it.id == id }

    override suspend fun sincronizar(): Result<Unit> = registrar(
        llamar { api.listar() }.map { respuesta ->
            dao.reemplazarTodos(respuesta.mapNotNull { it.aModelo()?.aEntidad() })
        }
    )

    override suspend fun crear(museo: Museum): Result<Museum> =
        guardarRespuesta(llamar { api.crear(museo.aDto(incluirId = false)) })

    override suspend fun actualizar(museo: Museum): Result<Museum> =
        guardarRespuesta(llamar { api.actualizar(museo.id, museo.aDto()) })

    override suspend fun aprobar(id: String): Result<Museum> =
        guardarRespuesta(llamar { api.aprobar(id) })

    override suspend fun eliminar(id: String): Result<Unit> = registrar(
        llamar {
            val respuesta = api.eliminar(id)
            // 404: ya no existía en el servidor, igual se borra la copia local
            if (!respuesta.isSuccessful && respuesta.code() != 404) throw HttpException(respuesta)
        }.map { dao.eliminar(id) }
    )

    /** Guarda en Room la ficha tal como la devolvió el servidor (con su id y estado definitivos). */
    private suspend fun guardarRespuesta(resultado: Result<MuseoDto>): Result<Museum> =
        registrar(resultado.mapCatching { dto ->
            val museo = dto.aModelo() ?: throw ErrorRemoto(TipoErrorRemoto.DESCONOCIDO, "Respuesta sin id")
            dao.guardar(museo.aEntidad())
            museo
        })

    /** Actualiza el aviso de conexión: solo "sin conexión" si el servidor no respondió (un 400/409 sí es respuesta). */
    private fun <T> registrar(resultado: Result<T>): Result<T> {
        val error = resultado.exceptionOrNull() as? ErrorRemoto
        _conexion.value = if (error?.tipo == TipoErrorRemoto.SIN_CONEXION) EstadoConexion.SIN_CONEXION else EstadoConexion.CONECTADO
        return resultado
    }
}
