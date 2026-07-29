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

data class CrossfadeTransitionInfo(
    val title: String,
    val description: String,
    val durationSeconds: Int,
    val isDj: Boolean = false,
    val fromSong: Song? = null,
    val toSong: Song? = null,
    val timestamp: Long = System.currentTimeMillis()
)

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

    private val _djStyle = MutableStateFlow("Radio FM Enérgico")
    val djStyle: StateFlow<String> = _djStyle.asStateFlow()

    var crossfadeSeconds: Int = 0

    private val _crossfadeTransition = MutableStateFlow<CrossfadeTransitionInfo?>(null)
    val crossfadeTransition: StateFlow<CrossfadeTransitionInfo?> = _crossfadeTransition.asStateFlow()

    private val _crossfadeProgress = MutableStateFlow(1f)
    val crossfadeProgress: StateFlow<Float> = _crossfadeProgress.asStateFlow()

    private var hasTriggeredAutoTransition = false
    private var isCrossfading = false

    private var playlist: List<Song> = emptyList()
    private var currentIndex: Int = -1

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    /** Canciones que siguen después de la que está sonando ahora ("A continuación"). */
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private fun refreshQueueState() {
        _queue.value = if (currentIndex in playlist.indices) {
            playlist.subList(currentIndex + 1, playlist.size)
        } else emptyList()
    }

    /** Agrega una canción justo después de la que está sonando (para que suene a continuación). */
    fun addToQueue(song: Song) {
        if (currentIndex < 0) {
            playSong(song, listOf(song))
            return
        }
        val insertAt = (currentIndex + 1).coerceAtMost(playlist.size)
        playlist = playlist.toMutableList().apply { add(insertAt, song) }
        try {
            primaryPlayer.addMediaItem(insertAt, songToMediaItem(song))
        } catch (e: Exception) {
            Log.e("KinemaxAudioPlayer", "No se pudo sincronizar addToQueue con ExoPlayer", e)
        }
        refreshQueueState()
    }

    /** Quita una canción de la cola (no afecta la que está sonando). */
    fun removeFromQueue(song: Song) {
        val index = playlist.withIndex().firstOrNull { (idx, s) -> idx > currentIndex && s.id == song.id }?.index
        if (index != null) {
            playlist = playlist.toMutableList().apply { removeAt(index) }
            try {
                primaryPlayer.removeMediaItem(index)
            } catch (e: Exception) {
                Log.e("KinemaxAudioPlayer", "No se pudo sincronizar removeFromQueue con ExoPlayer", e)
            }
            refreshQueueState()
        }
    }

    /** Salta directo a una canción específica de la cola. */
    fun playFromQueue(song: Song) {
        val index = playlist.indexOfFirst { it.id == song.id }
        if (index != -1) {
            currentIndex = index
            _currentSong.value = song
            refreshQueueState()
            executePlaySongInternal(song)
        }
    }

    private fun songToMediaItem(s: Song): MediaItem {
        val uri = if (s.isDownloaded && !s.localAudioPath.isNullOrEmpty()) Uri.parse(s.localAudioPath) else Uri.parse(s.audioUrl)
        return MediaItem.Builder()
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
                            _progressMs.value = primaryPlayer.currentPosition.coerceAtLeast(0L)
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
                        _progressMs.value = 0L
                        _durationMs.value = (song.duracionSeconds * 1000L).coerceAtLeast(1L)
                        refreshQueueState()
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

    fun setDjStyle(style: String) {
        _djStyle.value = style
    }

    fun playSong(song: Song, newPlaylist: List<Song> = emptyList(), isAutoTransition: Boolean = false) {
        // Solo reseteamos la bandera en llamadas MANUALES (usuario tocó una canción,
        // le dio a siguiente/anterior). Si viene del propio sistema de auto-transición
        // (crossfade que arranca antes de que termine la canción), NO la reseteamos aquí
        // — si no, se borra a sí misma antes de cumplir su función, y el evento natural
        // de "canción terminada" que llega segundos después puede disparar OTRO next(),
        // saltándose canciones de golpe. Se resetea correctamente en onTrackEnded().
        if (!isAutoTransition) {
            hasTriggeredAutoTransition = false
        }

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
        _progressMs.value = 0L
        _durationMs.value = (song.duracionSeconds * 1000L).coerceAtLeast(1L)
        refreshQueueState()

        if (_isDjModeActive.value) {
            // Modo DJ IA activo: en vez de pausar en seco, baja el volumen de la
            // canción anterior (ducking, como haría un DJ de radio real) mientras
            // habla encima, y luego MEZCLA hacia la nueva canción con crossfade.
            scope.launch {
                try {
                    _isSpeakingDj.value = true

                    val estabaSonando = primaryPlayer.isPlaying
                    if (estabaSonando) {
                        duckVolume(primaryPlayer, hasta = 0.15f, durationMs = 500L)
                    }

                    val nextSongCandidate = playlist.getOrNull(currentIndex + 1)
                    val currentStyle = _djStyle.value

                    val introRes = try {
                        GeminiAiService.generateSmartDjCommentary(
                            currentSong = song,
                            nextSong = nextSongCandidate,
                            djStyle = currentStyle
                        )
                    } catch (e: Exception) {
                        Log.e("KinemaxAudioPlayer", "Gemini DJ intro generation failed", e)
                        Result.failure(e)
                    }

                    val introText = introRes.getOrDefault("¡Hola vibers! A continuación escucharemos ${song.titulo} de ${song.artista}.")

                    if (djSpeaker == null) {
                        djSpeaker = DjSpeaker(context)
                    }

                    djSpeaker?.speak(introText, djStyle = currentStyle) {
                        _isSpeakingDj.value = false
                        // Mezcla hacia la nueva canción — usa el crossfade manual del usuario
                        // si es mayor, o un mínimo de 3s propio del modo DJ para que siempre mezcle.
                        val crossfadeDjSegundos = maxOf(crossfadeSeconds, 3)
                        executePlaySongInternal(song, forceCrossfadeSeconds = if (estabaSonando) crossfadeDjSegundos else 0)
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

    /** Baja el volumen de un reproductor de forma suave (ducking), sin pausarlo. */
    private suspend fun duckVolume(player: ExoPlayer, hasta: Float, durationMs: Long) {
        val steps = 10
        val delayMs = durationMs / steps
        val volumenInicial = player.volume
        for (i in 1..steps) {
            player.volume = volumenInicial - (volumenInicial - hasta) * (i.toFloat() / steps)
            delay(delayMs)
        }
    }

    private fun executePlaySongInternal(song: Song, forceCrossfadeSeconds: Int? = null) {
        try {
            val previousSong = _currentSong.value
            _currentSong.value = song
            _progressMs.value = 0L
            _durationMs.value = (song.duracionSeconds * 1000L).coerceAtLeast(1L)

            val targetMediaItem = songToMediaItem(song)
            val mediaItems = playlist.map { songToMediaItem(it) }
            val efectiveCrossfadeSeconds = forceCrossfadeSeconds ?: crossfadeSeconds

            if (efectiveCrossfadeSeconds > 0 && primaryPlayer.isPlaying) {
                isCrossfading = true
                _crossfadeProgress.value = 0f
                val isDj = _isDjModeActive.value
                val transitionTitle = if (isDj) "🎧 Transición DJ en Vivo" else "🎛️ Mezclando (Crossfade ${efectiveCrossfadeSeconds}s)"
                val transitionDesc = if (previousSong != null && previousSong.id != song.id) {
                    "De ${previousSong.titulo} ➔ ${song.titulo}"
                } else {
                    "Mezclando entrada: ${song.titulo}"
                }

                _crossfadeTransition.value = CrossfadeTransitionInfo(
                    title = transitionTitle,
                    description = transitionDesc,
                    durationSeconds = efectiveCrossfadeSeconds,
                    isDj = isDj,
                    fromSong = previousSong,
                    toSong = song
                )

                // Perform Crossfade between primaryPlayer and secondaryPlayer
                secondaryPlayer.setMediaItems(mediaItems, currentIndex, 0L)
                secondaryPlayer.prepare()
                secondaryPlayer.volume = 0f
                secondaryPlayer.play()

                _isPlaying.value = true
                startProgressTracker()

                val durationMs = efectiveCrossfadeSeconds * 1000L
                val steps = 30
                val delayMs = durationMs / steps

                scope.launch {
                    val volumenInicialPrimary = primaryPlayer.volume
                    for (i in 1..steps) {
                        val progreso = i.toFloat() / steps
                        primaryPlayer.volume = volumenInicialPrimary * (1f - progreso)
                        secondaryPlayer.volume = progreso
                        _crossfadeProgress.value = progreso
                        _progressMs.value = secondaryPlayer.currentPosition.coerceAtLeast(0L)
                        delay(delayMs)
                    }
                    primaryPlayer.stop()
                    primaryPlayer.volume = 1f
                    secondaryPlayer.volume = 1f

                    // Swap primary and secondary
                    val temp = primaryPlayer
                    primaryPlayer = secondaryPlayer
                    secondaryPlayer = temp

                    isCrossfading = false
                    _crossfadeProgress.value = 1f
                    _isPlaying.value = primaryPlayer.isPlaying
                    _progressMs.value = primaryPlayer.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = primaryPlayer.duration.coerceAtLeast(1L)

                    // primaryPlayer.stop() (arriba) no dispara STATE_ENDED de forma natural,
                    // así que onTrackEnded() nunca llega a "consumir" la bandera en este caso.
                    // La reseteamos aquí para que la SIGUIENTE canción sí pueda auto-transicionar
                    // — si no, después de la primera mezcla automática, todas las siguientes
                    // quedarían bloqueadas para siempre por !hasTriggeredAutoTransition.
                    hasTriggeredAutoTransition = false

                    mediaSession?.player = primaryPlayer
                    KinemaxEqualizerManager.attachToSession(primaryPlayer.audioSessionId)
                    startProgressTracker()

                    // Keep transition banner briefly visible after crossfade completes
                    delay(2500L)
                    if (_crossfadeTransition.value?.toSong?.id == song.id) {
                        _crossfadeTransition.value = null
                    }
                }
            } else {
                isCrossfading = false
                primaryPlayer.volume = 1f
                primaryPlayer.setMediaItems(mediaItems, currentIndex, 0L)
                primaryPlayer.prepare()
                primaryPlayer.play()
                _isPlaying.value = true
                startProgressTracker()
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

    fun next(isAutoTransition: Boolean = false) {
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
            playSong(nextSong, isAutoTransition = isAutoTransition)
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
        if (hasTriggeredAutoTransition) {
            hasTriggeredAutoTransition = false
            return
        }
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
                val activePlayer = if (isCrossfading) secondaryPlayer else primaryPlayer
                val currentPos = activePlayer.currentPosition
                val duration = activePlayer.duration
                if (currentPos >= 0) {
                    _progressMs.value = currentPos
                }
                if (duration > 0) {
                    _durationMs.value = duration
                }

                // Check auto-transition / crossfade near the end of track
                val effectiveCrossfadeSec = if (_isDjModeActive.value) maxOf(crossfadeSeconds, 4) else crossfadeSeconds
                val effectiveCrossfadeMs = effectiveCrossfadeSec * 1000L

                if (effectiveCrossfadeMs > 0 &&
                    !hasTriggeredAutoTransition &&
                    !isCrossfading &&
                    _repeatMode.value != RepeatMode.ONE &&
                    duration > 5000L &&
                    currentPos > 0L &&
                    (duration - currentPos) in 1L..effectiveCrossfadeMs
                ) {
                    if (currentIndex < playlist.size - 1 || _repeatMode.value == RepeatMode.ALL || _isShuffle.value) {
                        hasTriggeredAutoTransition = true
                        Log.d("KinemaxAudioPlayer", "Triggering automatic DJ transition/crossfade near track end")
                        next(isAutoTransition = true)
                    }
                }

                delay(200)
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

