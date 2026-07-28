package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Song(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "titulo") val titulo: String = "",
    @Json(name = "artista_principal") val artistaPrincipal: String? = null,
    @Json(name = "artista") val artistaRaw: String? = null,
    @Json(name = "album") val album: String? = "Single",
    @Json(name = "duracion_segundos") val duracionSegundos: Int? = 180,
    @Json(name = "reproducciones") val reproducciones: Long? = 0L,
    @Json(name = "audio_url") val audioUrl: String = "",
    @Json(name = "portada_url") val portadaUrl: String = "",
    @Json(name = "genero") val genero: Genre? = null,
    @Json(name = "artistas") val artistas: List<ArtistDetail>? = emptyList(),
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val isFavorite: Boolean = false
) {
    val artista: String
        get() = artistaPrincipal
            ?: artistaRaw
            ?: artistas?.firstOrNull { it.rol == "principal" }?.nombre
            ?: artistas?.firstOrNull()?.nombre
            ?: "Artista Desconocido"

    val displayArtist: String
        get() {
            if (!artistas.isNullOrEmpty()) {
                val main = artistas.find { it.rol == "principal" }?.nombre ?: artista
                val collabs = artistas.filter { it.rol != "principal" && it.nombre != main }.map { it.nombre }
                return if (collabs.isNotEmpty()) {
                    "$main ft. ${collabs.joinToString(", ")}"
                } else {
                    main
                }
            }
            return artista
        }

    val mainArtistId: Long?
        get() = artistas?.firstOrNull { it.rol == "principal" }?.id
            ?: artistas?.firstOrNull()?.id

    val duracionSeconds: Int
        get() = duracionSegundos ?: 180

    val generoNombre: String
        get() = genero?.nombre?.ifBlank { "Sin género" } ?: "Sin género"

    val formattedDuration: String
        get() {
            val minutes = duracionSeconds / 60
            val seconds = duracionSeconds % 60
            return String.format("%d:%02d", minutes, seconds)
        }
}

