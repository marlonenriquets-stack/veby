package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelo de datos para Playlist de un usuario.
 *
 * TODO backend: Estructura esperada para GET /api/playlists.php
 * Ejemplo JSON:
 * [
 *   {
 *     "id": 10,
 *     "nombre": "Mis Favoritas de Verano",
 *     "portada_url": "https://premios.kinemax.store/uploads/playlists/playlist_10.jpg",
 *     "canciones_count": 12,
 *     "canciones": [ ...lista de canciones... ]
 *   }
 * ]
 */
@JsonClass(generateAdapter = true)
data class Playlist(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "descripcion") val descripcion: String? = null,
    @Json(name = "portada_url") val portadaUrl: String? = "",
    @Json(name = "canciones_count") val cancionesCount: Int = 0,
    @Json(name = "canciones") val canciones: List<Song> = emptyList()
)
