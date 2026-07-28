package com.example.service

import android.util.Log
import com.example.data.model.Song
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object RemoteConfigManager {

    private const val DEFAULT_BASE_URL = "https://premios.kinemax.store/api/"
    private const val DEFAULT_API_KEY = "01c8fade6b906522bb6a3dc5f7e125985f7a3a37e3345496"

    private var _serverUrl: String = DEFAULT_BASE_URL
    private var _apiKey: String = DEFAULT_API_KEY

    val serverUrl: String
        get() = _serverUrl

    val apiKey: String
        get() = _apiKey

    fun normalizeServerUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (url.isEmpty()) return DEFAULT_BASE_URL
        if (!url.endsWith("/")) {
            url = "$url/"
        }
        while (url.contains("/api/api/")) {
            url = url.replace("/api/api/", "/api/")
        }
        if (!url.endsWith("/api/")) {
            url = "${url}api/"
        }
        return url
    }

    fun init(onComplete: (() -> Unit)? = null) {
        try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0L
            }
            remoteConfig.setConfigSettingsAsync(configSettings)

            val defaults = mapOf(
                "API_SERVER_URL" to DEFAULT_BASE_URL,
                "API_KEY" to DEFAULT_API_KEY,
                "REMOTE_SONGS_JSON" to ""
            )
            remoteConfig.setDefaultsAsync(defaults)

            remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fetchedUrl = remoteConfig.getString("API_SERVER_URL")
                    val fetchedKey = remoteConfig.getString("API_KEY")

                    if (fetchedUrl.isNotEmpty()) {
                        _serverUrl = normalizeServerUrl(fetchedUrl)
                    }
                    if (fetchedKey.isNotEmpty()) {
                        _apiKey = fetchedKey
                    }
                    Log.d("RemoteConfigManager", "Remote config fetched successfully: URL=$_serverUrl, KEY=$_apiKey")
                } else {
                    Log.w("RemoteConfigManager", "Remote config fetch failed, using default values: URL=$_serverUrl, KEY=$_apiKey")
                }
                onComplete?.invoke()
            }
        } catch (e: Exception) {
            Log.e("RemoteConfigManager", "Error initializing RemoteConfig", e)
            onComplete?.invoke()
        }
    }

    fun getRemoteSongs(): List<Song> {
        return try {
            val remoteConfig = FirebaseRemoteConfig.getInstance()
            val json = remoteConfig.getString("REMOTE_SONGS_JSON").ifEmpty {
                remoteConfig.getString("CATALOG_JSON")
            }
            if (json.isNotEmpty()) {
                val moshi = Moshi.Builder()
                    .add(com.example.data.model.GenreAdapter())
                    .add(KotlinJsonAdapterFactory())
                    .build()
                val type = Types.newParameterizedType(List::class.java, Song::class.java)
                val adapter = moshi.adapter<List<Song>>(type)
                adapter.fromJson(json) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("RemoteConfigManager", "Error parsing remote songs JSON", e)
            emptyList()
        }
    }
}

