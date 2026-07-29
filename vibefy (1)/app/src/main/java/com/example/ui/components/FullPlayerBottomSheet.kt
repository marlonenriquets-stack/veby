package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
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
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(
                        onClick = { showExplainSheet = true },
                        label = { Text("Explicar Canción", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = KinemaxAccent, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = KinemaxSurface)
                    )

                    AssistChip(
                        onClick = onToggleDjMode,
                        label = {
                            Text(
                                if (isSpeakingDj) "DJ Hablando..." else if (isDjModeActive) "Modo DJ: ON" else "Modo DJ IA",
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = if (isDjModeActive) KinemaxAccent else KinemaxTextSecondary, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isDjModeActive) KinemaxAccent.copy(alpha = 0.2f) else KinemaxSurface
                        )
                    )

                    AssistChip(
                        onClick = onRecommendationsClick,
                        label = { Text("Mix IA", fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.QueueMusic, contentDescription = null, tint = KinemaxAccent, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = KinemaxSurface)
                    )
                }

                // Artwork
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = song.portadaUrl,
                        contentDescription = "Portada de ${song.titulo}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Song Info Row (Title, Artist, Favorite)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = song.titulo,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        ArtistCreditsText(
                            song = song,
                            style = MaterialTheme.typography.titleMedium,
                            onArtistClick = onArtistClick?.let { callback ->
                                { id -> onDismiss(); callback(id) }
                            }
                        )
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

                // Progress Slider & Timers
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (isUserSeeking) sliderPosition else progressMs.toFloat(),
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
                            thumbColor = KinemaxAccent,
                            activeTrackColor = KinemaxAccent,
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
                            imageVector = Icons.Default.QueueMusic,
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

