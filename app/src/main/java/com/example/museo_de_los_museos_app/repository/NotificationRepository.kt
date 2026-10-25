package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.NotificationItem
import com.example.museo_de_los_museos_app.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User


interface NotificationRepository {
    val notificaciones: StateFlow<List<NotificationItem>>
    fun obtenerPorUsuario(userId: String): List<NotificationItem>
    fun marcarComoLeida(id: String)
    fun agregarAviso(item: NotificationItem)
    fun publicarParaRol(rol: Rol, aviso: NotificationItem)
    fun entregarAvisosDeRol(usuario: User)
}

/** Implementación en memoria pre-poblada con avisos de prueba por usuario. */
class InMemoryNotificationRepository : NotificationRepository {

    private val _notificaciones = MutableStateFlow(
        listOf(
            NotificationItem(
                id = "n01",
                userId = "u03",
                titulo = "¡Bienvenido al Museo de los Museos!",
                mensaje = "Explora el catálogo de cursos y el mapa interactivo de museos virtuales de Iberoamérica.",
                leida = false,
                fechaMillis = System.currentTimeMillis() - 86400000L,
                ruta = Routes.Mapa.route,
            ),
            NotificationItem(
                id = "n02",
                userId = "u03",
                titulo = "Nuevo curso disponible",
                mensaje = "Se ha publicado el curso 'Mediación Cultural en Museos'. Revisa los cupos disponibles.",
                leida = false,
                fechaMillis = System.currentTimeMillis() - 43200000L,
                ruta = Routes.Catalogo.route,
            ),
            NotificationItem(
                id = "n03",
                userId = "u03",
                titulo = "Solicitud Confirmada",
                mensaje = "Tu solicitud de compra #101 ha cambiado al estado CONFIRMADO.",
                leida = true,
                fechaMillis = System.currentTimeMillis() - 10800000L,
                ruta = Routes.Historial.route,
            ),
            NotificationItem(
                id = "n04",
                userId = "u01",
                titulo = "Panel de Administración activo",
                mensaje = "Puedes publicar avisos de prueba y gestionar el catálogo del museo.",
                leida = false,
                fechaMillis = System.currentTimeMillis() - 72000000L,
                ruta = Routes.Catalogo.route,
            ),
            NotificationItem(
                id = "n05",
                userId = "u02",
                titulo = "Fichas de museos de Iberoamérica",
                mensaje = "Tienes permisos para revisar y editar la información de los museos virtuales.",
                leida = false,
                fechaMillis = System.currentTimeMillis() - 36000000L,
                ruta = Routes.Mapa.route,
            ),
        )
    )
    private val avisosPorRol = MutableStateFlow<List<Pair<Rol, NotificationItem>>>(emptyList())

    override fun publicarParaRol(rol: Rol, aviso: NotificationItem) {
        avisosPorRol.update { it + (rol to aviso) }
    }

    override fun entregarAvisosDeRol(usuario: User) {
        val pendientes = avisosPorRol.value
            .filter { (rol, _) -> rol == usuario.rol }
            .map { (_, a) -> a.copy(id = "${a.id}_${usuario.id}", userId = usuario.id, leida = false) }
        if (pendientes.isEmpty()) return
        _notificaciones.update { actual ->
            val ids = actual.mapTo(HashSet()) { it.id }
            pendientes.filter { it.id !in ids } + actual
        }
    }

    override val notificaciones: StateFlow<List<NotificationItem>> = _notificaciones.asStateFlow()

    override fun obtenerPorUsuario(userId: String): List<NotificationItem> {
        return _notificaciones.value.filter { it.userId == userId }
    }

    override fun marcarComoLeida(id: String) {
        _notificaciones.update { lista ->
            lista.map { if (it.id == id) it.copy(leida = true) else it }
        }
    }

    override fun agregarAviso(item: NotificationItem) {
        _notificaciones.update { listOf(item) + it }
    }
}
