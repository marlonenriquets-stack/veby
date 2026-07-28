package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FirebaseLoginResponse(
    @Json(name = "status") val status: String? = "success",
    @Json(name = "message") val message: String? = null,
    @Json(name = "user") val user: User? = null,
    @Json(name = "usuario") val usuario: User? = null
) {
    val activeUser: User?
        get() = usuario ?: user
}

@JsonClass(generateAdapter = true)
data class RegisterPlaybackRequest(
    @Json(name = "cancion_id") val cancionId: Long
)

@JsonClass(generateAdapter = true)
data class SimpleStatusResponse(
    @Json(name = "ok") val ok: Boolean? = true,
    @Json(name = "status") val status: String? = "success",
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class AppConfigResponse(
    @Json(name = "anuncios_intervalo") val anunciosIntervalo: Int = 4,
    @Json(name = "onesignal_app_id") val onesignalAppId: String? = "YOUR-ONESIGNAL-APP-ID"
)

@JsonClass(generateAdapter = true)
data class PlaylistsResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "playlists") val playlists: List<Playlist>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class PlaylistActionRequest(
    @Json(name = "accion") val accion: String,
    @Json(name = "nombre") val nombre: String? = null,
    @Json(name = "descripcion") val descripcion: String? = null,
    @Json(name = "playlist_id") val playlistId: Long? = null,
    @Json(name = "cancion_id") val cancionId: Long? = null
)

@JsonClass(generateAdapter = true)
data class FavoritosResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "favoritos") val favoritos: List<Song>? = null,
    @Json(name = "canciones") val canciones: List<Song>? = null
) {
    val songs: List<Song>
        get() = favoritos ?: canciones ?: emptyList()
}

@JsonClass(generateAdapter = true)
data class FavoritoActionRequest(
    @Json(name = "accion") val accion: String,
    @Json(name = "cancion_id") val cancionId: Long
)

@JsonClass(generateAdapter = true)
data class ConfirmPurchaseRequest(
    @Json(name = "purchase_token") val purchaseToken: String,
    @Json(name = "product_id") val productId: String,
    @Json(name = "package_name") val packageName: String
)

@JsonClass(generateAdapter = true)
data class CatalogoResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "total") val total: Int = 0,
    @Json(name = "canciones") val canciones: List<Song>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class PlanesResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "planes") val planes: List<SubscriptionPlan>? = emptyList()
)

@JsonClass(generateAdapter = true)
data class GenerosResponse(
    @Json(name = "ok") val ok: Boolean = false,
    @Json(name = "generos") val generos: List<Genre>? = emptyList()
)


