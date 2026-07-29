package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.player.RepeatMode
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxGold
import com.example.ui.theme.KinemaxRed
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerBottomSheet(
    isVisible: Boolean,
    song: Song?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isDjModeActive: Boolean = false,
    isSpeakingDj: Boolean = false,
    crossfadeTransition: com.example.player.CrossfadeTransitionInfo? = null,
    crossfadeProgress: Float = 1f,
    permiteDescargas: Boolean,
    onDismiss: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onToggleDjMode: () -> Unit = {},
    onExplainSongClick: () -> Unit = {},
    onRecommendationsClick: () -> Unit = {},
    onOpenAiChat: () -> Unit = {},
    onArtistClick: ((Long) -> Unit)? = null,
    onQueueClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showExplainSheet by remember { mutableStateOf(false) }
    val explainSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    AnimatedVisibility(
        visible = isVisible && song != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        if (song == null) return@AnimatedVisibility

        var isUserSeeking by remember { mutableStateOf(false) }
        var sliderPosition by remember { mutableFloatStateOf(0f) }

        androidx.compose.runtime.LaunchedEffect(song.id) {
            isUserSeeking = false
            sliderPosition = 0f
        }

        val currentProgressMs = if (isUserSeeking) sliderPosition.toLong() else progressMs
        val formattedElapsed = formatMs(currentProgressMs)
        val formattedDuration = formatMs(durationMs)

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(KinemaxBackground)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .testTag("full_player_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar: Chevron Down + Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("full_player_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Cerrar reproductor",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "REPRODUCIENDO EN VIBEFY",
                            style = MaterialTheme.typography.labelSmall,
                            color = KinemaxTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = song.album ?: "Vibefy Music",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(
                        onClick = { onDownloadClick(song) },
                        modifier = Modifier.testTag("full_player_download")
                    ) {
                        if (!permiteDescargas) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Función Premium (Candado)",
                                tint = KinemaxGold
                            )
                        } else {
                            Icon(
                                imageVector = if (song.isDownloaded) Icons.Default.Download else Icons.Outlined.Download,
                                contentDescription = "Descargar",
                                tint = if (song.isDownloaded) KinemaxAccent else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // AI Tools Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = onOpenAiChat,
                        label = { Text("Hablar con IA", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = KinemaxAccent, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = KinemaxAccent.copy(alpha = 0.15f)),
                        modifier = Modifier.weight(1f)
                    )

                    AssistChip(
                        onClick = onToggleDjMode,
                        label = {
                            Text(
                                if (isSpeakingDj) "DJ..." else if (isDjModeActive) "Modo DJ: ON" else "Modo DJ IA",
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = if (isDjModeActive) KinemaxAccent else KinemaxTextSecondary, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isDjModeActive) KinemaxAccent.copy(alpha = 0.2f) else KinemaxSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    AssistChip(
                        onClick = onRecommendationsClick,
                        label = { Text("Mix IA", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = null, tint = KinemaxAccent, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = KinemaxSurface),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Artwork with smooth crossfade animation
                val fromSong = crossfadeTransition?.fromSong
                val isTransitioning = crossfadeTransition != null && fromSong != null && fromSong.id != song.id && crossfadeProgress < 1f

                val artworkBorderModifier = if (isTransitioning) {
                    Modifier.border(
                        width = 2.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(KinemaxAccent, Color(0xFFFF4081), Color(0xFF00E5FF), KinemaxAccent)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                } else Modifier

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .then(artworkBorderModifier)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (isTransitioning && fromSong != null) {
                        // Outgoing cover image fading out and scaling slightly
                        AsyncImage(
                            model = fromSong.portadaUrl,
                            contentDescription = "Portada de ${fromSong.titulo}",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = (1f - crossfadeProgress).coerceIn(0f, 1f)
                                    scaleX = 1f + (crossfadeProgress * 0.08f)
                                    scaleY = 1f + (crossfadeProgress * 0.08f)
                                },
                            contentScale = ContentScale.Crop
                        )
                        // Incoming cover image fading in and scaling into place
                        AsyncImage(
                            model = song.portadaUrl,
                            contentDescription = "Portada de ${song.titulo}",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = crossfadeProgress.coerceIn(0f, 1f)
                                    scaleX = 0.92f + (crossfadeProgress * 0.08f)
                                    scaleY = 0.92f + (crossfadeProgress * 0.08f)
                                },
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = song.portadaUrl,
                            contentDescription = "Portada de ${song.titulo}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Song Info Row (Title, Artist, Favorite) with smooth crossfade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        AnimatedContent(
                            targetState = song,
                            transitionSpec = {
                                val durationMs = ((crossfadeTransition?.durationSeconds ?: 3) * 1000).coerceAtLeast(300)
                                fadeIn(animationSpec = tween(durationMillis = durationMs)) togetherWith
                                        fadeOut(animationSpec = tween(durationMillis = durationMs))
                            },
                            label = "song_info_crossfade"
                        ) { currentSong ->
                            Column {
                                Text(
                                    text = currentSong.titulo,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                ArtistCreditsText(
                                    song = currentSong,
                                    style = MaterialTheme.typography.titleMedium,
                                    onArtistClick = onArtistClick?.let { callback ->
                                        { id -> onDismiss(); callback(id) }
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { onFavoriteClick(song) },
                        modifier = Modifier.testTag("full_player_favorite")
                    ) {
                        Icon(
                            imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (song.isFavorite) KinemaxRed else KinemaxTextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress Slider & Timers with smooth transition animation
                val targetSliderVal = if (isUserSeeking) sliderPosition else progressMs.toFloat()
                val animatedSliderVal by animateFloatAsState(
                    targetValue = targetSliderVal,
                    animationSpec = tween(durationMillis = if (isTransitioning) 350 else 150, easing = LinearEasing),
                    label = "full_player_slider_anim"
                )

                val activeTrackColor by animateColorAsState(
                    targetValue = if (isTransitioning) Color(0xFFFF4081) else KinemaxAccent,
                    animationSpec = tween(500),
                    label = "slider_active_color"
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (isUserSeeking) sliderPosition else animatedSliderVal.coerceIn(0f, durationMs.toFloat().coerceAtLeast(1f)),
                        onValueChange = {
                            isUserSeeking = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            isUserSeeking = false
                            onSeekTo(sliderPosition.toLong())
                        },
                        valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = activeTrackColor,
                            activeTrackColor = activeTrackColor,
                            inactiveTrackColor = KinemaxTextSecondary.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("full_player_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formattedElapsed,
                            style = MaterialTheme.typography.bodySmall,
                            color = KinemaxTextSecondary
                        )
                        Text(
                            text = formattedDuration,
                            style = MaterialTheme.typography.bodySmall,
                            color = KinemaxTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Playback Control Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = onShuffleClick) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Aleatorio",
                            tint = if (isShuffle) KinemaxAccent else KinemaxTextSecondary
                        )
                    }

                    // Previous
                    IconButton(onClick = onPreviousClick) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Anterior",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play/Pause Big Circle
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(KinemaxAccent)
                            .testTag("full_player_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Next
                    IconButton(onClick = onNextClick) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Siguiente",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Repeat Mode
                    IconButton(onClick = onRepeatClick) {
                        Icon(
                            imageVector = when (repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repetir",
                            tint = if (repeatMode != RepeatMode.OFF) KinemaxAccent else KinemaxTextSecondary
                        )
                    }
                }

                if (onQueueClick != null) {
                    androidx.compose.material3.TextButton(
                        onClick = onQueueClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Cola de reproducción",
                            tint = KinemaxTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "Ver cola de reproducción",
                            style = MaterialTheme.typography.labelMedium,
                            color = KinemaxTextSecondary
                        )
                    }
                }
            }
        }
    }

    if (showExplainSheet) {
        ExplainSongBottomSheet(
            song = song,
            sheetState = explainSheetState,
            onDismiss = { showExplainSheet = false }
        )
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

