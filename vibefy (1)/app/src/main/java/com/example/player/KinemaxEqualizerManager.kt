package com.example.player

import android.media.audiofx.Equalizer
import android.util.Log

object KinemaxEqualizerManager {

    private var equalizer: Equalizer? = null
    private var currentSessionId: Int = -1

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == currentSessionId) return
        release()
        try {
            currentSessionId = audioSessionId
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            Log.d("KinemaxEqualizer", "Equalizer attached to session $audioSessionId with ${equalizer?.numberOfBands} bands")
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Failed to attach Equalizer", e)
            equalizer = null
        }
    }

    fun release() {
        try {
            equalizer?.enabled = false
            equalizer?.release()
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error releasing Equalizer", e)
        } finally {
            equalizer = null
            currentSessionId = -1
        }
    }

    fun getBandCount(): Int {
        return try {
            equalizer?.numberOfBands?.toInt() ?: 5
        } catch (e: Exception) {
            5
        }
    }

    fun getBandRange(): Pair<Int, Int> {
        return try {
            val range = equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
            Pair(range[0].toInt(), range[1].toInt())
        } catch (e: Exception) {
            Pair(-1500, 1500)
        }
    }

    fun getCenterFreqs(): List<Int> {
        val count = getBandCount()
        val freqs = mutableListOf<Int>()
        try {
            val eq = equalizer
            if (eq != null) {
                for (i in 0 until count) {
                    freqs.add(eq.getCenterFreq(i.toShort()) / 1000) // Convert to Hz
                }
            } else {
                return listOf(60, 230, 910, 3600, 14000)
            }
        } catch (e: Exception) {
            return listOf(60, 230, 910, 3600, 14000)
        }
        return freqs
    }

    fun setBandLevel(band: Int, levelInMb: Int) {
        try {
            equalizer?.setBandLevel(band.toShort(), levelInMb.toShort())
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error setting band level", e)
        }
    }

    fun applyPreset(presetName: String): List<Int> {
        val count = getBandCount()
        // Define preset relative millibels (-1500 to +1500)
        val levels = when (presetName.lowercase()) {
            "pop" -> listOf(100, 200, 400, 200, -100)
            "rock" -> listOf(500, 300, -100, 300, 600)
            "bass boost" -> listOf(800, 600, 200, 0, -200)
            "vocal" -> listOf(-200, 100, 600, 500, 100)
            else -> listOf(0, 0, 0, 0, 0) // Plano
        }

        for (i in 0 until count) {
            val level = levels.getOrElse(i) { 0 }
            setBandLevel(i, level)
        }
        return levels
    }
}
