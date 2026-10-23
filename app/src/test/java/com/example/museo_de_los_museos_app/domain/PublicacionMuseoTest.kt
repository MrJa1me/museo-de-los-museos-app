package com.example.museo_de_los_museos_app.domain

import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.visiblePara
import com.example.museo_de_los_museos_app.repository.SeedData
import com.example.museo_de_los_museos_app.viewmodel.dashboard.DashboardEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PublicacionMuseoTest {

    private val pendiente = SeedData.museos.first().copy(id = "m11", nombre = "Museo Pendiente", publicado = false)

    @Test
    fun museosDePrueba_vienenPublicadosYConImagen() {
        assertTrue(SeedData.museos.all { it.publicado })
        assertTrue(SeedData.museos.all { it.imagen.isNotBlank() })
        assertEquals(SeedData.museos.size, SeedData.museos.map { it.imagen }.distinct().size)
    }

    @Test
    fun fichaNueva_quedaPendientePorDefecto() {
        assertFalse(SeedData.museos.first().copy(publicado = false).publicado)
    }

    @Test
    fun clienteEInvitado_noVenPendientes() {
        assertFalse(pendiente.visiblePara(Rol.CLIENTE))
        assertFalse(pendiente.visiblePara(null))
        assertTrue(pendiente.visiblePara(Rol.ADMINISTRADOR))
        assertTrue(pendiente.visiblePara(Rol.INVESTIGADOR))
    }

    @Test
    fun dashboard_soloElAdministradorVeLaListaDePendientes() {
        val museos = SeedData.museos + pendiente
        val admin = SeedData.usuarios.first { it.rol == Rol.ADMINISTRADOR }
        val cliente = SeedData.usuarios.first { it.rol == Rol.CLIENTE }

        val estadoAdmin = DashboardEvaluator.evaluarEstado(admin, museos, emptyList(), emptyList())
        assertEquals(listOf("m11"), estadoAdmin.museosPendientes.map { it.id })

        val estadoCliente = DashboardEvaluator.evaluarEstado(cliente, museos, emptyList(), emptyList())
        assertTrue(estadoCliente.museosPendientes.isEmpty())
        assertEquals(SeedData.museos.size, estadoCliente.totalMuseos)
        assertFalse(estadoCliente.museosDestacados.any { it.id == "m11" })
    }
}
