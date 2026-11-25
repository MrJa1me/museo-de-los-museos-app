package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.remote.dto.ObrasRespuestaDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * API externa pública del Art Institute of Chicago (https://api.artic.edu/docs).
 * No necesita clave. Es de terceros: solo se lee, nunca se guarda en Room ni en los microservicios.
 */
interface ObrasArteApi {

    @GET("api/v1/artworks/search")
    suspend fun buscar(
        @Query("q") consulta: String,
        @Query("limit") limite: Int = 20,
        @Query("fields") campos: String = CAMPOS,
        // El museo pide identificar la app que consume su API. OkHttp solo admite ASCII en los encabezados (sin tildes)
        @Header("AIC-User-Agent") agente: String = AGENTE
    ): ObrasRespuestaDto

    companion object {
        const val CAMPOS = "id,title,artist_display,date_display,place_of_origin,medium_display,image_id"
        const val AGENTE = "MuseoDeLosMuseos/1.0 (proyecto academico DSY1105)"
    }
}
