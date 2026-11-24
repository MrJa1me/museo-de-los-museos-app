package com.example.museo_de_los_museos_app.remote

import com.example.museo_de_los_museos_app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Crea los servicios Retrofit de la app. Hay tres orígenes de datos remotos:
 *  - ms-museos  (microservicio propio, Spring Boot, puerto 8081)
 *  - ms-pedidos (microservicio propio, Spring Boot, puerto 8082)
 *  - API externa del Art Institute of Chicago (pública, de terceros)
 * Las URL vienen de BuildConfig (app/build.gradle.kts).
 */
object ClienteHttp {

    /** Tiempo corto: si el microservicio no está encendido, la app usa sus datos guardados sin quedarse esperando. */
    private const val TIEMPO_ESPERA_SEGUNDOS = 6L

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            // Muestra cada petición y respuesta en Logcat (filtro "okhttp"): útil para ver el consumo en tiempo real
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    private fun retrofit(urlBase: String): Retrofit = Retrofit.Builder()
        .baseUrl(urlBase)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val museos: MuseosApi by lazy { retrofit(BuildConfig.URL_MS_MUSEOS).create(MuseosApi::class.java) }
    val pedidos: PedidosApi by lazy { retrofit(BuildConfig.URL_MS_PEDIDOS).create(PedidosApi::class.java) }
    val obrasArte: ObrasArteApi by lazy { retrofit(BuildConfig.URL_API_OBRAS).create(ObrasArteApi::class.java) }
}
