package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.DownloadedSongDao
import com.example.data.local.dao.FavoriteSongDao
import com.example.data.local.dao.PlaylistDao
import com.example.data.local.entity.DownloadedSongEntity
import com.example.data.local.entity.FavoriteSongEntity
import com.example.data.local.entity.LocalPlaylistEntity

@Database(
    entities = [
        DownloadedSongEntity::class,
        FavoriteSongEntity::class,
        LocalPlaylistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KinemaxDatabase : RoomDatabase() {

    abstract fun downloadedSongDao(): DownloadedSongDao
    abstract fun favoriteSongDao(): FavoriteSongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: KinemaxDatabase? = null

        fun getDatabase(context: Context): KinemaxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KinemaxDatabase::class.java,
                    "kinemax_music_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
