package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelo de datos del Usuario retornado por POST /api/firebase_login.php.
 *
 * TODO backend: El backend devuelve los permisos del usuario después de validar el ID Token de Firebase.
 * Ejemplo JSON de respuesta de `firebase_login.php`:
 * {
 *   "status": "success",
 *   "user": {
 *     "id": "usr_12345",
 *     "nombre": "Marlon TS",
 *     "email": "marlon@example.com",
 *     "es_premium": true,
 *     "quita_anuncios": true,
 *     "permite_descargas": true
 *   }
 * }
 */
@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: String = "",
    @Json(name = "nombre") val nombre: String = "",
    @Json(name = "email") val email: String = "",
    @Json(name = "es_premium") val esPremium: Boolean = false,
    @Json(name = "quita_anuncios") val quitaAnuncios: Boolean = false,
    @Json(name = "permite_descargas") val permiteDescargas: Boolean = false,
    @Json(name = "plan") val plan: String? = "Gratuito"
)
