package com.example.museo_de_los_museos_app.remote.dto

import com.google.gson.annotations.SerializedName

/** Respuesta de la búsqueda del Art Institute of Chicago: {"data":[...], "config":{"iiif_url": ...}} */
data class ObrasRespuestaDto(
    val data: List<ObraDto>? = null,
    val config: ConfigDto? = null
)

/** @SerializedName traduce los nombres en snake_case del JSON a camelCase de Kotlin. */
data class ObraDto(
    val id: Long? = null,
    val title: String? = null,
    @SerializedName("artist_display") val artista: String? = null,
    @SerializedName("date_display") val fecha: String? = null,
    @SerializedName("place_of_origin") val lugarOrigen: String? = null,
    @SerializedName("medium_display") val tecnica: String? = null,
    @SerializedName("image_id") val imagenId: String? = null
)

data class ConfigDto(@SerializedName("iiif_url") val iiifUrl: String? = null)
