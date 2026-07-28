package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DownloadedSongEntity
import com.example.data.local.entity.FavoriteSongEntity
import com.example.data.local.entity.LocalPlaylistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadedAt DESC")
    fun getAllDownloadedSongs(): Flow<List<DownloadedSongEntity>>

    @Query("SELECT * FROM downloaded_songs WHERE id = :id LIMIT 1")
    suspend fun getDownloadedSongById(id: Long): DownloadedSongEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadedSong(song: DownloadedSongEntity)

    @Query("DELETE FROM downloaded_songs WHERE id = :id")
    suspend fun deleteDownloadedSong(id: Long)
}

@Dao
interface FavoriteSongDao {
    @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
    fun getAllFavoriteSongs(): Flow<List<FavoriteSongEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_songs WHERE id = :id)")
    fun isFavorite(id: Long): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteSong(song: FavoriteSongEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFavorites(songs: List<FavoriteSongEntity>)

    @Query("DELETE FROM favorite_songs WHERE id = :id")
    suspend fun deleteFavoriteSong(id: Long)

    @Query("DELETE FROM favorite_songs")
    suspend fun clearAllFavorites()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM local_playlists ORDER BY createdAt DESC")
    fun getAllLocalPlaylists(): Flow<List<LocalPlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: LocalPlaylistEntity): Long

    @Query("DELETE FROM local_playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)
}
