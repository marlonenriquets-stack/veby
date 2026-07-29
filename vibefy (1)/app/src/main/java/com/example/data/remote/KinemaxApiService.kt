package com.example.data.remote

import com.example.data.model.AppConfigResponse
import com.example.data.model.AlbumDetailApiResponse
import com.example.data.model.AlbumesResponse
import com.example.data.model.ArtistResponse
import com.example.data.model.CatalogoResponse
import com.example.data.model.ConfirmPurchaseRequest
import com.example.data.model.FavoritoActionRequest
import com.example.data.model.FavoritosResponse
import com.example.data.model.FirebaseLoginResponse
import com.example.data.model.Genre
import com.example.data.model.PlanesResponse
import com.example.data.model.Playlist
import com.example.data.model.PlaylistActionRequest
import com.example.data.model.PlaylistsResponse
import com.example.data.model.RegisterPlaybackRequest
import com.example.data.model.SimpleStatusResponse
import com.example.data.model.Song
import com.example.data.model.SubscriptionPlan
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

import com.example.data.model.GenerosResponse

interface KinemaxApiService {

    /**
     * Sincroniza la autenticación de Firebase con la base de datos PHP/MySQL.
     * Envía Header Authorization: Bearer <idToken>
     */
    @POST("firebase_login.php")
    suspend fun firebaseLogin(): Response<FirebaseLoginResponse>

    /**
     * Catálogo completo de canciones.
     */
    @GET("catalogo.php")
    suspend fun getCatalogo(
        @Query("busqueda") busqueda: String? = null,
        @Query("genero_id") generoId: String? = null,
        @Query("limit") limit: Int? = 50,
        @Query("offset") offset: Int? = 0
    ): Response<CatalogoResponse>

    /**
     * Perfil de artista con sus canciones.
     */
    @GET("artistas.php")
    suspend fun getArtistProfile(
        @Query("id") artistId: Long
    ): Response<ArtistResponse>

    /**
     * Lista de álbumes para la fila de Inicio.
     */
    @GET("albumes.php")
    suspend fun getAlbumes(
        @Query("limit") limit: Int? = 20
    ): Response<AlbumesResponse>

    /**
     * Detalle de un álbum específico con su lista completa de canciones.
     */
    @GET("album_canciones.php")
    suspend fun getAlbumCanciones(
        @Query("id") albumId: Long
    ): Response<AlbumDetailApiResponse>

    /**
     * Lista de géneros para filtros.
     */
    @GET("generos.php")
    suspend fun getGeneros(): Response<GenerosResponse>

    /**
     * Canciones más escuchadas por período (hoy | semana | mes | todo).
     */
    @GET("top_canciones.php")
    suspend fun getTopCanciones(
        @Query("periodo") periodo: String = "semana"
    ): Response<CatalogoResponse>

    /**
     * Registra cada reproducción cuando la canción empieza a sonar.
     */
    @POST("registrar_reproduccion.php")
    suspend fun registrarReproduccion(
        @Body request: RegisterPlaybackRequest
    ): Response<SimpleStatusResponse>

    /**
     * Planes de suscripción para el paywall.
     */
    @GET("planes.php")
    suspend fun getPlanes(): Response<PlanesResponse>

    /**
     * Confirma la compra en Google Play contra el backend.
     */
    @POST("confirmar_compra.php")
    suspend fun confirmarCompra(
        @Body request: ConfirmPurchaseRequest
    ): Response<FirebaseLoginResponse>

    /**
     * Configuración de la aplicación (intervalo de anuncios AdMob y OneSignal App ID).
     */
    @GET("config.php")
    suspend fun getConfig(): Response<AppConfigResponse>

    /**
     * Obtener las playlists del usuario autenticado.
     */
    @GET("playlists.php")
    suspend fun getPlaylists(): Response<PlaylistsResponse>

    /**
     * Acciones de playlist: crear, agregar_cancion, quitar_cancion, eliminar.
     */
    @POST("playlists.php")
    suspend fun postPlaylistAction(
        @Body request: PlaylistActionRequest
    ): Response<SimpleStatusResponse>

    /**
     * Obtener las canciones favoritas del usuario.
     */
    @GET("favoritos.php")
    suspend fun getFavoritos(): Response<FavoritosResponse>

    /**
     * Acciones de favorito: agregar, quitar.
     */
    @POST("favoritos.php")
    suspend fun postFavoritoAction(
        @Body request: FavoritoActionRequest
    ): Response<SimpleStatusResponse>
}

