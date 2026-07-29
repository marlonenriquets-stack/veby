package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.audioSettingsDataStore by preferencesDataStore(name = "audio_settings")

object AudioSettingsDataStore {

    private val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
    private val EQ_PRESET = stringPreferencesKey("eq_preset")
    private val EQ_BAND_LEVELS = stringPreferencesKey("eq_band_levels") // comma-separated levels
    private val AUDIO_QUALITY = stringPreferencesKey("audio_quality")
    private val VOLUME_NORMALIZATION = booleanPreferencesKey("volume_normalization")
    private val VOLUME_LEVEL = stringPreferencesKey("volume_level") // Bajo, Medio, Alto
    private val BASS_BOOST = intPreferencesKey("bass_boost") // 0 to 1000
    private val VIRTUALIZER = intPreferencesKey("virtualizer") // 0 to 1000

    fun getCrossfadeSeconds(context: Context): Flow<Int> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[CROSSFADE_SECONDS] ?: 3
        }
    }

    suspend fun saveCrossfadeSeconds(context: Context, seconds: Int) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[CROSSFADE_SECONDS] = seconds.coerceIn(0, 12)
        }
    }

    fun getEqPreset(context: Context): Flow<String> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[EQ_PRESET] ?: "Plano"
        }
    }

    suspend fun saveEqPreset(context: Context, preset: String) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[EQ_PRESET] = preset
        }
    }

    fun getEqBandLevels(context: Context): Flow<List<Int>> {
        return context.audioSettingsDataStore.data.map { prefs ->
            val raw = prefs[EQ_BAND_LEVELS] ?: ""
            if (raw.isEmpty()) {
                emptyList()
            } else {
                raw.split(",").mapNotNull { it.toIntOrNull() }
            }
        }
    }

    suspend fun saveEqBandLevels(context: Context, levels: List<Int>) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[EQ_BAND_LEVELS] = levels.joinToString(",")
        }
    }

    fun getAudioQuality(context: Context): Flow<String> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[AUDIO_QUALITY] ?: "Alta (320 kbps)"
        }
    }

    suspend fun saveAudioQuality(context: Context, quality: String) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[AUDIO_QUALITY] = quality
        }
    }

    fun getVolumeNormalization(context: Context): Flow<Boolean> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[VOLUME_NORMALIZATION] ?: true
        }
    }

    suspend fun saveVolumeNormalization(context: Context, enabled: Boolean) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[VOLUME_NORMALIZATION] = enabled
        }
    }

    fun getVolumeLevel(context: Context): Flow<String> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[VOLUME_LEVEL] ?: "Medio"
        }
    }

    suspend fun saveVolumeLevel(context: Context, level: String) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[VOLUME_LEVEL] = level
        }
    }

    fun getBassBoost(context: Context): Flow<Int> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[BASS_BOOST] ?: 0
        }
    }

    suspend fun saveBassBoost(context: Context, strength: Int) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[BASS_BOOST] = strength.coerceIn(0, 1000)
        }
    }

    fun getVirtualizer(context: Context): Flow<Int> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[VIRTUALIZER] ?: 0
        }
    }

    suspend fun saveVirtualizer(context: Context, strength: Int) {
        context.audioSettingsDataStore.edit { prefs ->
            prefs[VIRTUALIZER] = strength.coerceIn(0, 1000)
        }
    }
}

