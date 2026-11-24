package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.remote.dto.CambioActivoDto
import com.example.museo_de_los_museos_app.remote.dto.ProductoDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Endpoints del microservicio ms-productos (ProductoController en Spring Boot). */
interface ProductosApi {

    @GET("api/productos")
    suspend fun listar(): List<ProductoDto>

    @POST("api/productos")
    suspend fun crear(@Body producto: ProductoDto): ProductoDto

    @PUT("api/productos/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body producto: ProductoDto): ProductoDto

    @PATCH("api/productos/{id}/activo")
    suspend fun cambiarActivo(@Path("id") id: String, @Body cambio: CambioActivoDto): ProductoDto

}
