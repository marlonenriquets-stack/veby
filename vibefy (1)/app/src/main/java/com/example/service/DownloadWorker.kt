package com.example.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.KinemaxDatabase
import com.example.data.local.entity.DownloadedSongEntity
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val songId = inputData.getLong("song_id", -1L)
        val titulo = inputData.getString("titulo") ?: "Canción"
        val artista = inputData.getString("artista") ?: "Artista"
        val album = inputData.getString("album") ?: "Álbum"
        val duracion = inputData.getInt("duracion", 180)
        val portadaUrl = inputData.getString("portada_url") ?: ""
        val audioUrl = inputData.getString("audio_url") ?: ""
        val genero = inputData.getString("genero") ?: "General"

        if (songId == -1L || audioUrl.isEmpty()) {
            return Result.failure()
        }

        return try {
            val downloadsDir = File(appContext.filesDir, "downloads")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val destinationFile = File(downloadsDir, "song_$songId.mp3")

            // Download using OkHttp
            val client = OkHttpClient()
            val request = Request.Builder().url(audioUrl).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful || response.body == null) {
                return Result.failure()
            }

            response.body!!.byteStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            val dao = KinemaxDatabase.getDatabase(appContext).downloadedSongDao()
            val downloadedEntity = DownloadedSongEntity(
                id = songId,
                titulo = titulo,
                artista = artista,
                album = album,
                duracionSeconds = duracion,
                portadaUrl = portadaUrl,
                originalAudioUrl = audioUrl,
                localAudioPath = destinationFile.absolutePath,
                genero = genero
            )
            dao.insertDownloadedSong(downloadedEntity)

            Log.d("DownloadWorker", "Song $titulo downloaded successfully to ${destinationFile.absolutePath}")
            Result.success()
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Error downloading song $songId", e)
            Result.retry()
        }
    }
}
