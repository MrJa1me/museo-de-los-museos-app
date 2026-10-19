package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.repository.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresCuentaTest {

    @Test
    fun correoVacio_esObligatorio() {
        assertEquals(ErrorCuenta.OBLIGATORIO, ValidadoresCuenta.correo("   "))
    }

    @Test
    fun correoSinArroba_esInvalido() {
        assertEquals(ErrorCuenta.CORREO_INVALIDO, ValidadoresCuenta.correo("juan.correo.cl"))
    }

    @Test
    fun correosValidos_seAceptan() {
        assertNull(ValidadoresCuenta.correo("admin@museo.test"))
        assertNull(ValidadoresCuenta.correo(" persona@duocuc.cl "))
    }

    @Test
    fun contrasenaCorta_seRechaza() {
        assertEquals(ErrorCuenta.CONTRASENA_CORTA, ValidadoresCuenta.contrasena("Ab1"))
    }

    @Test
    fun contrasenaSinNumeroOSinLetra_esDebil() {
        assertEquals(ErrorCuenta.CONTRASENA_DEBIL, ValidadoresCuenta.contrasena("solo letras"))
        assertEquals(ErrorCuenta.CONTRASENA_DEBIL, ValidadoresCuenta.contrasena("12345678"))
    }

    @Test
    fun confirmacion_debeCoincidir() {
        assertEquals(ErrorCuenta.NO_COINCIDEN, ValidadoresCuenta.confirmacionContrasena("Admin123", "Admin124"))
        assertNull(ValidadoresCuenta.confirmacionContrasena("Admin123", "Admin123"))
    }

    @Test
    fun nombre_requiereAlMenosDosCaracteres() {
        assertEquals(ErrorCuenta.OBLIGATORIO, ValidadoresCuenta.nombre(""))
        assertEquals(ErrorCuenta.TEXTO_CORTO, ValidadoresCuenta.nombre("A"))
        assertNull(ValidadoresCuenta.nombre("Ana"))
    }

    @Test
    fun usuariosDePruebaDelEquipo_cumplenLasReglas() {
        SeedData.usuarios.forEach { usuario ->
            assertNull(ValidadoresCuenta.correo(usuario.correo))
            assertNull(ValidadoresCuenta.contrasena(usuario.contrasena))
        }
    }
}
