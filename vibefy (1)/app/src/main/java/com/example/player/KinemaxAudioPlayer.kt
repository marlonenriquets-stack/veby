package com.example.player

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.MainActivity
import com.example.data.model.Song
import com.example.data.repository.MusicRepository
import com.example.service.DjSpeaker
import com.example.service.GeminiAiService
import com.example.service.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RepeatMode {
    OFF, ALL, ONE
}

@OptIn(UnstableApi::class)
class KinemaxAudioPlayer(
    private val context: Context,
    private val repository: MusicRepository
) {

    private var primaryPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private var secondaryPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    private var mediaSession: MediaSession? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null
    private var djSpeaker: DjSpeaker? = DjSpeaker(context)

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progressMs = MutableStateFlow(0L)
    val progressMs: StateFlow<Long> = _progressMs.asStateFlow()

    private val _durationMs = MutableStateFlow(1L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _isDjModeActive = MutableStateFlow(false)
    val isDjModeActive: StateFlow<Boolean> = _isDjModeActive.asStateFlow()

    private val _isSpeakingDj = MutableStateFlow(false)
    val isSpeakingDj: StateFlow<Boolean> = _isSpeakingDj.asStateFlow()

    var crossfadeSeconds: Int = 0

    private var playlist: List<Song> = emptyList()
    private var currentIndex: Int = -1

    var onSongStartedCallback: ((Song) -> Unit)? = null

    init {
        // Initialize MediaSession for system media notification & background playback
        try {
            val sessionActivityIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            mediaSession = MediaSession.Builder(context, primaryPlayer)
                .setSessionActivity(sessionActivityIntent)
                .build()

            PlaybackService.activeMediaSession = mediaSession
        } catch (e: Exception) {
            Log.e("KinemaxAudioPlayer", "Error initializing MediaSession", e)
        }

        setupPlayerListeners(primaryPlayer)
        setupPlayerListeners(secondaryPlayer)

        // Attach Equalizer
        try {
            KinemaxEqualizerManager.attachToSession(primaryPlayer.audioSessionId)
        } catch (e: Exception) {
            Log.e("KinemaxAudioPlayer", "Error attaching Equalizer", e)
        }
    }

    private fun setupPlayerListeners(player: ExoPlayer) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (player == primaryPlayer) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startProgressTracker()
                    } else {
                        stopProgressTracker()
                    }
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (player == primaryPlayer) {
                    when (state) {
                        Player.STATE_READY -> {
                            _durationMs.value = primaryPlayer.duration.coerceAtLeast(1L)
                            try {
                                KinemaxEqualizerManager.attachToSession(primaryPlayer.audioSessionId)
                            } catch (_: Exception) {}
                        }
                        Player.STATE_ENDED -> {
                            onTrackEnded()
                        }
                        else -> {}
                    }
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (player == primaryPlayer) {
                    val newIndex = primaryPlayer.currentMediaItemIndex
                    if (newIndex in playlist.indices && newIndex != currentIndex) {
                        currentIndex = newIndex
                        val song = playlist[currentIndex]
                        _currentSong.value = song
                        onSongStartedCallback?.invoke(song)
                        scope.launch(Dispatchers.IO) {
                            repository.registrarReproduccion(song.id)
                        }
                    }
                }
            }
        })
    }

    fun toggleDjMode() {
        _isDjModeActive.value = !_isDjModeActive.value
        if (!_isDjModeActive.value) {
            djSpeaker?.stop()
            _isSpeakingDj.value = false
        }
    }

    fun playSong(song: Song, newPlaylist: List<Song> = emptyList()) {
        if (newPlaylist.isNotEmpty()) {
            playlist = newPlaylist
            currentIndex = playlist.indexOfFirst { it.id == song.id }
        } else if (playlist.isEmpty()) {
            playlist = listOf(song)
            currentIndex = 0
        } else {
            currentIndex = playlist.indexOfFirst { it.id == song.id }
            if (currentIndex == -1) {
                playlist = playlist + song
                currentIndex = playlist.size - 1
            }
        }

        if (currentIndex < 0) currentIndex = 0
        _currentSong.value = song

        if (_isDjModeActive.value) {
            // Modo DJ IA active: Speak intro before starting track
            scope.launch {
                try {
                    _isSpeakingDj.value = true
                    try { primaryPlayer.pause() } catch (_: Exception) {}

                    val introRes = try {
                        GeminiAiService.generateDjIntro(song.titulo, song.artista, song.generoNombre)
                    } catch (e: Exception) {
                        Log.e("KinemaxAudioPlayer", "Gemini DJ intro generation failed", e)
                        Result.failure(e)
                    }

                    val introText = introRes.getOrDefault("¡Hola! Disfruta de ${song.titulo} de ${song.artista}.")

                    if (djSpeaker == null) {
                        djSpeaker = DjSpeaker(context)
                    }

                    djSpeaker?.speak(introText) {
                        _isSpeakingDj.value = false
                        executePlaySongInternal(song)
                    }
                } catch (e: Exception) {
                    Log.e("KinemaxAudioPlayer", "Error during DJ mode flow", e)
                    _isSpeakingDj.value = false
                    executePlaySongInternal(song)
                }
            }
        } else {
            executePlaySongInternal(song)
        }
    }

    private fun executePlaySongInternal(song: Song) {
        try {
            val mediaUri = if (song.isDownloaded && !song.localAudioPath.isNullOrEmpty()) {
                Uri.parse(song.localAudioPath)
            } else {
                Uri.parse(song.audioUrl)
            }

            val targetMediaItem = MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(mediaUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.titulo)
                        .setArtist(song.artista)
                        .setAlbumTitle(song.album ?: "")
                        .setArtworkUri(if (!song.portadaUrl.isNullOrEmpty()) Uri.parse(song.portadaUrl) else null)
                        .build()
                )
                .build()

            val mediaItems = playlist.map { s ->
                val uri = if (s.isDownloaded && !s.localAudioPath.isNullOrEmpty()) Uri.parse(s.localAudioPath) else Uri.parse(s.audioUrl)
                MediaItem.Builder()
                    .setMediaId(s.id.toString())
                    .setUri(uri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(s.titulo)
                            .setArtist(s.artista)
                            .setAlbumTitle(s.album ?: "")
                            .setArtworkUri(if (!s.portadaUrl.isNullOrEmpty()) Uri.parse(s.portadaUrl) else null)
                            .build()
                    )
                    .build()
            }

            if (crossfadeSeconds > 0 && primaryPlayer.isPlaying) {
                // Perform Crossfade between primaryPlayer and secondaryPlayer
                secondaryPlayer.setMediaItems(mediaItems, currentIndex, 0L)
                secondaryPlayer.prepare()
                secondaryPlayer.volume = 0f
                secondaryPlayer.play()

                val durationMs = crossfadeSeconds * 1000L
                val steps = 20
                val delayMs = durationMs / steps

                scope.launch {
                    for (i in 1..steps) {
                        val fadeOut = 1f - (i.toFloat() / steps)
                        val fadeIn = i.toFloat() / steps
                        primaryPlayer.volume = fadeOut
                        secondaryPlayer.volume = fadeIn
                        delay(delayMs)
                    }
                    primaryPlayer.stop()
                    primaryPlayer.volume = 1f
                    secondaryPlayer.volume = 1f

                    // Swap primary and secondary
                    val temp = primaryPlayer
                    primaryPlayer = secondaryPlayer
                    secondaryPlayer = temp

                    mediaSession?.player = primaryPlayer
                    KinemaxEqualizerManager.attachToSession(primaryPlayer.audioSessionId)
                }
            } else {
                primaryPlayer.volume = 1f
                primaryPlayer.setMediaItems(mediaItems, currentIndex, 0L)
                primaryPlayer.prepare()
                primaryPlayer.play()
            }

            // Start PlaybackService so system media session is active
            try {
                val serviceIntent = Intent(context, PlaybackService::class.java)
                context.startService(serviceIntent)
            } catch (e: Exception) {
                Log.e("KinemaxAudioPlayer", "Error starting PlaybackService", e)
            }

            // Register playback with backend
            scope.launch(Dispatchers.IO) {
                repository.registrarReproduccion(song.id)
            }

            onSongStartedCallback?.invoke(song)

        } catch (e: Exception) {
            Log.e("KinemaxAudioPlayer", "Error playing song: ${song.titulo}", e)
        }
    }

    fun togglePlayPause() {
        if (primaryPlayer.isPlaying) {
            primaryPlayer.pause()
        } else {
            if (_currentSong.value != null) {
                primaryPlayer.play()
            }
        }
    }

    fun next() {
        if (playlist.isEmpty()) return
        if (_repeatMode.value == RepeatMode.ONE) {
            seekTo(0)
            primaryPlayer.play()
            return
        }

        val nextIndex = if (_isShuffle.value) {
            (0 until playlist.size).random()
        } else {
            if (currentIndex < playlist.size - 1) currentIndex + 1 else 0
        }

        val nextSong = playlist.getOrNull(nextIndex)
        if (nextSong != null) {
            playSong(nextSong)
        }
    }

    fun previous() {
        if (playlist.isEmpty()) return
        if (primaryPlayer.currentPosition > 3000) {
            seekTo(0)
            return
        }
        val prevIndex = if (currentIndex > 0) currentIndex - 1 else playlist.size - 1
        val prevSong = playlist.getOrNull(prevIndex)
        if (prevSong != null) {
            playSong(prevSong)
        }
    }

    fun seekTo(positionMs: Long) {
        primaryPlayer.seekTo(positionMs)
        _progressMs.value = positionMs
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
        primaryPlayer.shuffleModeEnabled = _isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        primaryPlayer.repeatMode = when (_repeatMode.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    private fun onTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                primaryPlayer.play()
            }
            RepeatMode.ALL -> {
                next()
            }
            RepeatMode.OFF -> {
                if (currentIndex < playlist.size - 1) {
                    next()
                } else {
                    _isPlaying.value = false
                }
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = scope.launch {
            while (true) {
                _progressMs.value = primaryPlayer.currentPosition
                _durationMs.value = primaryPlayer.duration.coerceAtLeast(1L)
                delay(500)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        djSpeaker?.shutdown()
        djSpeaker = null
        try {
            KinemaxEqualizerManager.release()
            mediaSession?.release()
            mediaSession = null
            PlaybackService.activeMediaSession = null
        } catch (e: Exception) {
            Log.e("KinemaxAudioPlayer", "Error releasing MediaSession", e)
        }
        primaryPlayer.release()
        secondaryPlayer.release()
    }
}

