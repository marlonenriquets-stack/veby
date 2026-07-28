package com.example.data.repository

import android.content.Context
import com.example.data.local.KinemaxDatabase
import com.example.data.local.entity.FavoriteSongEntity
import com.example.data.local.entity.LocalPlaylistEntity
import com.example.data.model.Genre
import com.example.data.model.Playlist
import com.example.data.model.RegisterPlaybackRequest
import com.example.data.model.Song
import com.example.data.model.User
import com.example.data.remote.RetrofitClient
import com.example.service.RemoteConfigManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(private val context: Context) {

    private val api = RetrofitClient.apiService
    private val db = KinemaxDatabase.getDatabase(context)
    private val downloadedSongDao = db.downloadedSongDao()
    private val favoriteSongDao = db.favoriteSongDao()
    private val playlistDao = db.playlistDao()

    suspend fun syncFirebaseLogin(): User? {
        return try {
            val res = api.firebaseLogin()
            if (res.isSuccessful) {
                res.body()?.activeUser
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTopSongs(periodo: String = "semana"): List<Song> {
        return try {
            val response = api.getTopCanciones(periodo)
            if (response.isSuccessful) {
                val songs = response.body()?.canciones
                if (!songs.isNullOrEmpty()) {
                    songs
                } else {
                    RemoteConfigManager.getRemoteSongs()
                }
            } else {
                val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
                throw Exception("top_canciones.php: $err")
            }
        } catch (e: Exception) {
            if (e is java.io.IOException || e is com.squareup.moshi.JsonDataException) {
                throw e
            }
            RemoteConfigManager.getRemoteSongs()
        }
    }

    suspend fun getCatalog(search: String? = null, generoId: Long? = null): List<Song> {
        val generoParam = if (generoId != null && generoId > 0L) generoId.toString() else null
        val response = api.getCatalogo(busqueda = search, generoId = generoParam)
        return if (response.isSuccessful) {
            response.body()?.canciones ?: emptyList()
        } else {
            val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
            throw Exception("catalogo.php: $err")
        }
    }

    suspend fun getArtistProfile(artistId: Long): com.example.data.model.FullArtistProfile? {
        return try {
            val res = api.getArtistProfile(artistId)
            if (res.isSuccessful) res.body()?.artista else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getGeneros(): List<Genre> {
        return try {
            val res = api.getGeneros()
            if (res.isSuccessful) {
                res.body()?.generos ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPlanes(): List<com.example.data.model.SubscriptionPlan> {
        val response = api.getPlanes()
        if (response.isSuccessful) {
            return response.body()?.planes ?: emptyList()
        } else {
            val err = response.errorBody()?.string() ?: "HTTP ${response.code()}"
            throw Exception("planes.php: $err")
        }
    }

    suspend fun confirmarCompra(purchaseToken: String, productId: String, packageName: String): User? {
        return try {
            val req = com.example.data.model.ConfirmPurchaseRequest(
                purchaseToken = purchaseToken,
                productId = productId,
                packageName = packageName
            )
            val res = api.confirmarCompra(req)
            if (res.isSuccessful && res.body()?.status == "success") {
                res.body()?.user
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registrarReproduccion(songId: Long) {
        try {
            api.registrarReproduccion(RegisterPlaybackRequest(songId))
        } catch (e: Exception) {
            // Silence network errors on registration
        }
    }

    // Favorites (Room + Remote)
    val favoriteSongsFlow: Flow<List<Song>> = favoriteSongDao.getAllFavoriteSongs()
        .map { list -> list.map { it.toSong() } }

    fun isFavorite(songId: Long): Flow<Boolean> = favoriteSongDao.isFavorite(songId)

    suspend fun getFavoritosRemote(): List<Song> {
        return try {
            val response = api.getFavoritos()
            if (response.isSuccessful) {
                val songs = response.body()?.songs ?: emptyList()
                val entities = songs.map { song ->
                    FavoriteSongEntity(
                        id = song.id,
                        titulo = song.titulo,
                        artista = song.artista,
                        album = song.album ?: "Single",
                        duracionSeconds = song.duracionSeconds,
                        portadaUrl = song.portadaUrl,
                        audioUrl = song.audioUrl,
                        genero = song.generoNombre
                    )
                }
                favoriteSongDao.clearAllFavorites()
                favoriteSongDao.insertAllFavorites(entities)
                songs
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun toggleFavorite(song: Song, isCurrentlyFav: Boolean = song.isFavorite) {
        val entity = FavoriteSongEntity(
            id = song.id,
            titulo = song.titulo,
            artista = song.artista,
            album = song.album ?: "Single",
            duracionSeconds = song.duracionSeconds,
            portadaUrl = song.portadaUrl,
            audioUrl = song.audioUrl,
            genero = song.generoNombre
        )

        if (isCurrentlyFav) {
            favoriteSongDao.deleteFavoriteSong(song.id)
            try {
                api.postFavoritoAction(com.example.data.model.FavoritoActionRequest("quitar", song.id))
            } catch (_: Exception) {}
        } else {
            favoriteSongDao.insertFavoriteSong(entity)
            try {
                api.postFavoritoAction(com.example.data.model.FavoritoActionRequest("agregar", song.id))
            } catch (_: Exception) {}
        }
    }

    // Downloaded Songs (Offline)
    val downloadedSongsFlow: Flow<List<Song>> = downloadedSongDao.getAllDownloadedSongs()
        .map { list -> list.map { it.toSong() } }

    suspend fun deleteDownloadedSong(songId: Long) {
        downloadedSongDao.deleteDownloadedSong(songId)
    }

    // Playlists (Remote + Local)
    val localPlaylistsFlow: Flow<List<Playlist>> = playlistDao.getAllLocalPlaylists()
        .map { list ->
            list.map { entity ->
                Playlist(
                    id = entity.id,
                    nombre = entity.nombre,
                    portadaUrl = entity.portadaUrl,
                    cancionesCount = 0,
                    canciones = emptyList()
                )
            }
        }

    suspend fun getPlaylistsRemote(): List<Playlist> {
        return try {
            val response = api.getPlaylists()
            if (response.isSuccessful) {
                val remoteList = response.body()?.playlists ?: emptyList()
                // Sync remote playlists to room cache
                for (p in remoteList) {
                    playlistDao.insertPlaylist(LocalPlaylistEntity(id = p.id, nombre = p.nombre, portadaUrl = p.portadaUrl ?: ""))
                }
                remoteList
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createPlaylistRemote(nombre: String, descripcion: String? = null): Boolean {
        return try {
            val req = com.example.data.model.PlaylistActionRequest(accion = "crear", nombre = nombre, descripcion = descripcion)
            val res = api.postPlaylistAction(req)
            res.isSuccessful && (res.body()?.ok == true || res.body()?.status == "success")
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addSongToPlaylistRemote(playlistId: Long, cancionId: Long): Boolean {
        return try {
            val req = com.example.data.model.PlaylistActionRequest(accion = "agregar_cancion", playlistId = playlistId, cancionId = cancionId)
            val res = api.postPlaylistAction(req)
            res.isSuccessful && (res.body()?.ok == true || res.body()?.status == "success")
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeSongFromPlaylistRemote(playlistId: Long, cancionId: Long): Boolean {
        return try {
            val req = com.example.data.model.PlaylistActionRequest(accion = "quitar_cancion", playlistId = playlistId, cancionId = cancionId)
            val res = api.postPlaylistAction(req)
            res.isSuccessful && (res.body()?.ok == true || res.body()?.status == "success")
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deletePlaylistRemote(playlistId: Long): Boolean {
        return try {
            playlistDao.deletePlaylist(playlistId)
            val req = com.example.data.model.PlaylistActionRequest(accion = "eliminar", playlistId = playlistId)
            val res = api.postPlaylistAction(req)
            res.isSuccessful && (res.body()?.ok == true || res.body()?.status == "success")
        } catch (e: Exception) {
            false
        }
    }
}
