package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Song

@Entity(tableName = "downloaded_songs")
data class DownloadedSongEntity(
    @PrimaryKey val id: Long,
    val titulo: String,
    val artista: String,
    val album: String,
    val duracionSeconds: Int,
    val portadaUrl: String,
    val originalAudioUrl: String,
    val localAudioPath: String,
    val localPortadaPath: String? = null,
    val genero: String = "General",
    val downloadedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song {
        return Song(
            id = id,
            titulo = titulo,
            artistaPrincipal = artista,
            album = album,
            duracionSegundos = duracionSeconds,
            portadaUrl = portadaUrl,
            audioUrl = localAudioPath, // Point ExoPlayer to local file path
            genero = com.example.data.model.Genre(nombre = genero),
            isDownloaded = true,
            localAudioPath = localAudioPath
        )
    }
}

@Entity(tableName = "favorite_songs")
data class FavoriteSongEntity(
    @PrimaryKey val id: Long,
    val titulo: String,
    val artista: String,
    val album: String,
    val duracionSeconds: Int,
    val portadaUrl: String,
    val audioUrl: String,
    val genero: String = "General",
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toSong(): Song {
        return Song(
            id = id,
            titulo = titulo,
            artistaPrincipal = artista,
            album = album,
            duracionSegundos = duracionSeconds,
            portadaUrl = portadaUrl,
            audioUrl = audioUrl,
            genero = com.example.data.model.Genre(nombre = genero),
            isFavorite = true
        )
    }
}

@Entity(tableName = "local_playlists")
data class LocalPlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val portadaUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
