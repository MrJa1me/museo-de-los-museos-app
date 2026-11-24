package com.example.museo_de_los_museos_app.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Traducción de los errores de red y HTTP a algo que la pantalla pueda explicar. */
class ErrorRemotoTest {

    private fun httpError(codigo: Int, cuerpo: String) =
        HttpException(Response.error<Any>(codigo, cuerpo.toResponseBody("application/json".toMediaType())))

    @Test
    fun sinRed_esSinConexion() {
        assertEquals(TipoErrorRemoto.SIN_CONEXION, IOException("timeout").aErrorRemoto().tipo)
    }

    @Test
    fun conflicto409_tomaElMensajeDelServidor() {
        val error = httpError(409, """{"estado":409,"mensaje":"No se puede cancelar un pedido ENVIADO"}""").aErrorRemoto()
        assertEquals(TipoErrorRemoto.RECHAZADO, error.tipo)
        assertEquals(409, error.codigo)
        assertEquals("No se puede cancelar un pedido ENVIADO", error.mensajeServidor)
    }

    @Test
    fun validacion400_muestraElPrimerCampoConError() {
        val cuerpo = """{"mensaje":"Datos inválidos","campos":{"enlace":"El enlace debe empezar con http://"}}"""
        assertEquals("El enlace debe empezar con http://", httpError(400, cuerpo).aErrorRemoto().mensajeServidor)
    }

    @Test
    fun error500_esDeServidor_aunqueElCuerpoNoSeaJson() {
        val error = httpError(500, "<html>").aErrorRemoto()
        assertEquals(TipoErrorRemoto.SERVIDOR, error.tipo)
        assertNull(error.mensajeServidor)
    }

    @Test
    fun otrosErrores_sonDesconocidos_yUnErrorRemotoSeConserva() {
        assertEquals(TipoErrorRemoto.DESCONOCIDO, IllegalStateException("x").aErrorRemoto().tipo)
        val original = ErrorRemoto(TipoErrorRemoto.RECHAZADO, "m")
        assertSame(original, original.aErrorRemoto())
    }

    @Test
    fun mensajeDelCuerpo_casosBorde() {
        assertNull(mensajeDelCuerpo(null))
        assertNull(mensajeDelCuerpo(""))
        assertNull(mensajeDelCuerpo("""{"estado":404}"""))
        assertEquals("No existe", mensajeDelCuerpo("""{"mensaje":"No existe","campos":{}}"""))
    }

    @Test
    fun llamar_envuelveElResultado() = runTest {
        assertEquals(5, llamar { 5 }.getOrThrow())
        val fallo = llamar<Int> { throw IOException() }
        assertTrue(fallo.exceptionOrNull() is ErrorRemoto)
    }

    @Test(expected = CancellationException::class)
    fun llamar_noAtrapaLaCancelacion() = runTest {
        llamar<Int> { throw CancellationException("cancelado") }
    }
}
