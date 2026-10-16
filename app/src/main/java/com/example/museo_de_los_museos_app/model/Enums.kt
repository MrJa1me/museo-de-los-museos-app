package com.example.museo_de_los_museos_app.model

enum class Rol(val etiqueta: String) {
    ADMINISTRADOR("Administrador"),
    INVESTIGADOR("Investigador"),
    CLIENTE("Cliente")
}

enum class ClasificacionMuseo(val etiqueta: String) {
    ARTE("Arte"),
    HISTORIA("Historia"),
    CIENCIA("Ciencia"),
    COMUNIDAD("Comunidad")
}

enum class CategoriaProducto(val etiqueta: String) {
    CURSO("Curso"),
    PUBLICACION("Publicación"),
    MATERIAL_CONSERVACION("Material de conservación"),
    ARTICULO_MUSEOLOGICO("Artículo museológico")
}

enum class EstadoPedido(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    ENVIADO("Enviado"),
    ENTREGADO("Entregado");

    /** Siguiente estado del flujo Pendiente → Confirmado → Enviado → Entregado (null si ya está entregado). */
    fun siguiente(): EstadoPedido? = entries.getOrNull(ordinal + 1)
}
