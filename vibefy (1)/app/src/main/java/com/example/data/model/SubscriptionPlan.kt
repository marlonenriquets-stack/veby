package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubscriptionPlan(
    @Json(name = "id") val id: Long = 0L,
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "precio") val precio: Double = 0.0,
    @Json(name = "duracion_dias") val duracionDias: Int = 30,
    @Json(name = "quita_anuncios") val quitaAnuncios: Boolean = false,
    @Json(name = "permite_descargas") val permiteDescargas: Boolean = false,
    @Json(name = "descripcion") val descripcion: String? = null,
    @Json(name = "beneficios") val beneficios: List<String>? = emptyList(),
    @Json(name = "google_play_product_id") val googlePlayProductId: String? = null,
    @Json(name = "periodo") val periodo: String? = "mes"
) {
    val formattedPrecio: String
        get() = if (precio == 0.0) "Gratis" else String.format("$%.2f", precio)

    val displayGooglePlayProductId: String
        get() = if (googlePlayProductId.isNullOrEmpty()) "sin SKU configurado" else googlePlayProductId

    val hasSkuConfigured: Boolean
        get() = !googlePlayProductId.isNullOrEmpty()
}

