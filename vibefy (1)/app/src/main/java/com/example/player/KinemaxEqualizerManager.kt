package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log

object KinemaxEqualizerManager {

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var currentSessionId: Int = -1

    private var isNormalizationEnabled: Boolean = true
    private var volumeLevel: String = "Medio"
    private var bassBoostStrength: Int = 0
    private var virtualizerStrength: Int = 0

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

        // Attach BassBoost
        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = bassBoostStrength > 0
                if (strengthSupported) {
                    setStrength(bassBoostStrength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Failed to attach BassBoost", e)
            bassBoost = null
        }

        // Attach Virtualizer
        try {
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = virtualizerStrength > 0
                if (strengthSupported) {
                    setStrength(virtualizerStrength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Failed to attach Virtualizer", e)
            virtualizer = null
        }

        // Attach LoudnessEnhancer for Volume Normalization and Volume Level
        try {
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = isNormalizationEnabled
                val gainMb = getTargetGainMb(volumeLevel)
                setTargetGain(gainMb)
            }
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Failed to attach LoudnessEnhancer", e)
            loudnessEnhancer = null
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
        }

        try {
            bassBoost?.enabled = false
            bassBoost?.release()
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error releasing BassBoost", e)
        } finally {
            bassBoost = null
        }

        try {
            virtualizer?.enabled = false
            virtualizer?.release()
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error releasing Virtualizer", e)
        } finally {
            virtualizer = null
        }

        try {
            loudnessEnhancer?.enabled = false
            loudnessEnhancer?.release()
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error releasing LoudnessEnhancer", e)
        } finally {
            loudnessEnhancer = null
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
        val levels = when (presetName.lowercase()) {
            "pop" -> listOf(100, 200, 400, 200, -100)
            "rock" -> listOf(500, 300, -100, 300, 600)
            "bass boost" -> listOf(800, 600, 200, 0, -200)
            "vocal" -> listOf(-200, 100, 600, 500, 100)
            "jazz" -> listOf(400, 200, -100, 200, 500)
            "clásica" -> listOf(500, 300, -200, 400, 400)
            "electrónica" -> listOf(600, 400, 0, 200, 500)
            else -> listOf(0, 0, 0, 0, 0) // Plano
        }

        for (i in 0 until count) {
            val level = levels.getOrElse(i) { 0 }
            setBandLevel(i, level)
        }
        return levels
    }

    fun setBassBoost(strength: Int) {
        this.bassBoostStrength = strength.coerceIn(0, 1000)
        try {
            bassBoost?.let {
                it.enabled = strength > 0
                if (it.strengthSupported) {
                    it.setStrength(strength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error updating BassBoost", e)
        }
    }

    fun setVirtualizer(strength: Int) {
        this.virtualizerStrength = strength.coerceIn(0, 1000)
        try {
            virtualizer?.let {
                it.enabled = strength > 0
                if (it.strengthSupported) {
                    it.setStrength(strength.toShort())
                }
            }
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error updating Virtualizer", e)
        }
    }

    fun setVolumeNormalization(enabled: Boolean) {
        this.isNormalizationEnabled = enabled
        try {
            loudnessEnhancer?.let {
                it.enabled = enabled
                if (enabled) {
                    it.setTargetGain(getTargetGainMb(volumeLevel))
                }
            }
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error setting Volume Normalization", e)
        }
    }

    fun setVolumeLevel(level: String) {
        this.volumeLevel = level
        try {
            loudnessEnhancer?.let {
                if (isNormalizationEnabled) {
                    it.setTargetGain(getTargetGainMb(level))
                }
            }
        } catch (e: Exception) {
            Log.e("KinemaxEqualizer", "Error setting Volume Level", e)
        }
    }

    private fun getTargetGainMb(level: String): Int {
        return when (level.lowercase()) {
            "bajo" -> 0 // 0 mB (Normal/Suave)
            "alto" -> 800 // +8 dB gain boost
            else -> 400 // Medio: +4 dB gain boost (Equilibrado)
        }
    }
}

