package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArtistAlbum(
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "portada_url") val portadaUrl: String? = null,
    @Json(name = "num_canciones") val numCanciones: Int? = 0,
    @Json(name = "canciones") val canciones: List<Song>? = emptyList()
) {
    val songList: List<Song>
        get() = canciones ?: emptyList()
}

@JsonClass(generateAdapter = true)
data class FullArtistProfile(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "foto_url") val fotoUrl: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "biografia") val biografia: String? = null,
    @Json(name = "reproducciones_totales") val reproduccionesTotales: Long? = 0L,
    @Json(name = "top_canciones") val topCanciones: List<Song>? = emptyList(),
    @Json(name = "albumes") val albumes: List<ArtistAlbum>? = emptyList(),
    @Json(name = "canciones") val canciones: List<Song>? = emptyList()
) {
    val displayBio: String?
        get() = bio ?: biografia

    val formattedPlays: String
        get() {
            val total = reproduccionesTotales ?: 0L
            return when {
                total >= 1_000_000 -> String.format("%.1fM reproducciones", total / 1_000_000.0)
                total >= 1_000 -> String.format("%.1fK reproducciones", total / 1_000.0)
                else -> "$total reproducciones"
            }
        }
}

@JsonClass(generateAdapter = true)
data class ArtistResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "artista") val artista: FullArtistProfile? = null
)

