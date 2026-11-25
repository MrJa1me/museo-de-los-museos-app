package com.example.museo_de_los_museos_app.remote

import okhttp3.Headers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** El encabezado que identifica la app no puede traer tildes: OkHttp lanzaría IllegalArgumentException y la petición nunca saldría. */
class ObrasArteApiTest {

    @Test
    fun elAgenteSoloUsaCaracteresAscii() {
        assertTrue(ObrasArteApi.AGENTE.all { it.code in 32..126 })
    }

    @Test
    fun okHttpAceptaElAgenteComoValorDeEncabezado() {
        val encabezados = Headers.Builder().add("AIC-User-Agent", ObrasArteApi.AGENTE).build()
        assertEquals(ObrasArteApi.AGENTE, encabezados["AIC-User-Agent"])
    }
}
