package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.remote.dto.CambioEstadoDto
import com.example.museo_de_los_museos_app.remote.dto.PedidoDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoints del microservicio ms-pedidos (PedidoController en Spring Boot). */
interface PedidosApi {

    @GET("api/pedidos")
    suspend fun listar(): List<PedidoDto>

    @POST("api/pedidos")
    suspend fun crear(@Body pedido: PedidoDto): PedidoDto

    @PATCH("api/pedidos/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: String, @Body cambio: CambioEstadoDto): PedidoDto

    @DELETE("api/pedidos/{id}")
    suspend fun eliminar(@Path("id") id: String): Response<Unit>
}
