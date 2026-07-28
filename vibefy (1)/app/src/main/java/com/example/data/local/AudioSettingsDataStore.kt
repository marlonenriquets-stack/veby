package com.example.data.local

import android.content.Context
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

    fun getCrossfadeSeconds(context: Context): Flow<Int> {
        return context.audioSettingsDataStore.data.map { prefs ->
            prefs[CROSSFADE_SECONDS] ?: 0
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
}
