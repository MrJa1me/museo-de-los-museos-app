package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.domain.ValidadorFichaMuseo
import com.example.museo_de_los_museos_app.model.Museum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


/**
 * Contrato de acceso a las fichas de museos. Las pantallas dependen de esta interfaz, no de la implementación:
 * en la app real es [MuseumRepositoryRemoto] (microservicio ms-museos + copia en Room) y en las pruebas,
 * [InMemoryMuseumRepository].
 *
 * Las escrituras son suspend y devuelven Result: el ViewModel sabe si el servidor aceptó la operación.
 */
interface MuseumRepository {
    val museos: StateFlow<List<Museum>>
    val conexion: StateFlow<EstadoConexion>
    fun obtenerPorId(id: String): Museum?

    /** Descarga las fichas del microservicio y actualiza la copia local. */
    suspend fun sincronizar(): Result<Unit>

    /** El servidor asigna el id (m11, m12...) y la ficha queda pendiente de aprobación. */
    suspend fun crear(museo: Museum): Result<Museum>

    suspend fun actualizar(museo: Museum): Result<Museum>

    /** Marca la ficha como publicada (solo el Administrador). */
    suspend fun aprobar(id: String): Result<Museum>

    suspend fun eliminar(id: String): Result<Unit>
}

/** Implementación temporal en memoria, cargada con [SeedData]. No persiste al cerrar la app. */
class InMemoryMuseumRepository(inicial: List<Museum> = SeedData.museos) : MuseumRepository {
    private val _museos = MutableStateFlow(inicial)
    override val museos: StateFlow<List<Museum>> = _museos.asStateFlow()
    override val conexion: StateFlow<EstadoConexion> = MutableStateFlow(EstadoConexion.CONECTADO)

    override fun obtenerPorId(id: String): Museum? = _museos.value.firstOrNull { it.id == id }

    override suspend fun sincronizar(): Result<Unit> = Result.success(Unit)

    override suspend fun crear(museo: Museum): Result<Museum> {
        val nuevo = museo.copy(id = ValidadorFichaMuseo.siguienteId(_museos.value.map { it.id }), publicado = false)
        _museos.update { it + nuevo }
        return Result.success(nuevo)
    }

    override suspend fun actualizar(museo: Museum): Result<Museum> {
        _museos.update { lista -> lista.map { if (it.id == museo.id) museo else it } }
        return Result.success(museo)
    }

    override suspend fun aprobar(id: String): Result<Museum> {
        _museos.update { lista -> lista.map { if (it.id == id) it.copy(publicado = true) else it } }
        return obtenerPorId(id)?.let { Result.success(it) } ?: Result.failure(NoSuchElementException(id))
    }

    override suspend fun eliminar(id: String): Result<Unit> {
        _museos.update { lista -> lista.filterNot { it.id == id } }
        return Result.success(Unit)
    }
}
