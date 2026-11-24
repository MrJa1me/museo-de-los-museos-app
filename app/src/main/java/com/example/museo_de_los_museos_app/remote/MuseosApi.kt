package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.remote.dto.MuseoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints del microservicio ms-museos (MuseoController en Spring Boot). */
interface MuseosApi {

    @GET("api/museos")
    suspend fun listar(): List<MuseoDto>

    @POST("api/museos")
    suspend fun crear(@Body museo: MuseoDto): MuseoDto

    @PUT("api/museos/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body museo: MuseoDto): MuseoDto

    @PATCH("api/museos/{id}/aprobar")
    suspend fun aprobar(@Path("id") id: String): MuseoDto

    /** 204 sin cuerpo: por eso Response<Unit>. */
    @DELETE("api/museos/{id}")
    suspend fun eliminar(@Path("id") id: String): Response<Unit>
}
