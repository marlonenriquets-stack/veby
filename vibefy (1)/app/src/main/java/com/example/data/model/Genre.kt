package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Genre(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "imagen_url") val imagenUrl: String? = null,
    @Json(name = "num_canciones") val numCanciones: Int? = 0
)

