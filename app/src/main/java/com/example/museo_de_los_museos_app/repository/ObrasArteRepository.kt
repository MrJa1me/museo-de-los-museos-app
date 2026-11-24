package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.ObraArte
import com.example.museo_de_los_museos_app.remote.ObrasArteApi
import com.example.museo_de_los_museos_app.remote.aModelo
import com.example.museo_de_los_museos_app.remote.llamar

/**
 * Obras de arte de la API externa. A diferencia de museos y pedidos, no se guardan en Room ni en los
 * microservicios: son datos de terceros que se consultan en el momento.
 */
interface ObrasArteRepository {
    suspend fun buscar(consulta: String): Result<List<ObraArte>>
}

class ObrasArteRepositoryRemoto(private val api: ObrasArteApi) : ObrasArteRepository {

    override suspend fun buscar(consulta: String): Result<List<ObraArte>> =
        llamar { api.buscar(consulta.trim()) }.map { respuesta ->
            val iiif = respuesta.config?.iiifUrl
            respuesta.data.orEmpty().mapNotNull { it.aModelo(iiif) }
        }
}
