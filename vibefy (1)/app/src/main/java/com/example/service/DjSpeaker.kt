package com.example.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

class DjSpeaker(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        initTts()
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val result = tts?.setLanguage(Locale("es", "ES"))
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            tts?.language = Locale.getDefault()
                        }
                        isInitialized = true
                    } catch (e: Exception) {
                        Log.e("DjSpeaker", "Error setting language in TTS", e)
                        isInitialized = false
                    }
                } else {
                    Log.e("DjSpeaker", "TTS init failed with status: $status")
                    isInitialized = false
                }
            }
        } catch (e: Exception) {
            Log.e("DjSpeaker", "Failed to create TextToSpeech instance", e)
            tts = null
            isInitialized = false
        }
    }

    fun setDjStyle(styleName: String) {
        val (pitch, rate) = when (styleName.lowercase()) {
            "dj de club & mezclas", "club" -> 1.12f to 1.20f
            "locutor clásico & seductor", "clasico" -> 0.90f to 0.95f
            "chill & acoustic", "chill" -> 0.98f to 1.00f
            else -> 1.05f to 1.10f // Radio FM Enérgico (Default)
        }
        try {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(rate)
        } catch (e: Exception) {
            Log.e("DjSpeaker", "Error setting pitch/rate", e)
        }
    }

    fun speak(text: String, djStyle: String = "Radio FM Enérgico", onSpeechFinished: () -> Unit) {
        setDjStyle(djStyle)
        val hasFinished = AtomicBoolean(false)

        fun notifyFinished() {
            if (hasFinished.compareAndSet(false, true)) {
                mainHandler.post {
                    try {
                        onSpeechFinished()
                    } catch (e: Exception) {
                        Log.e("DjSpeaker", "Error in onSpeechFinished callback", e)
                    }
                }
            }
        }

        // Safety timeout of 8 seconds for speech intro
        mainHandler.postDelayed({
            if (!hasFinished.get()) {
                Log.w("DjSpeaker", "TTS speech timeout reached, forcing playback resume")
                stop()
                notifyFinished()
            }
        }, 8000L)

        if (!isInitialized || tts == null) {
            notifyFinished()
            return
        }

        try {
            val utteranceId = "VIBEFY_DJ_${System.currentTimeMillis()}"

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    notifyFinished()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    notifyFinished()
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    notifyFinished()
                }
            })

            val params = android.os.Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)

            if (result != TextToSpeech.SUCCESS) {
                Log.w("DjSpeaker", "TTS speak returned non-success code: $result")
                notifyFinished()
            }
        } catch (e: Exception) {
            Log.e("DjSpeaker", "Exception calling TTS speak", e)
            notifyFinished()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("DjSpeaker", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("DjSpeaker", "Error shutting down TTS", e)
        } finally {
            tts = null
            isInitialized = false
        }
    }
}

