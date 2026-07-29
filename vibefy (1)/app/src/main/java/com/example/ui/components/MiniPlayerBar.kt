package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxSurfaceVariant
import com.example.ui.theme.KinemaxTextSecondary

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.example.player.CrossfadeTransitionInfo
import com.example.ui.theme.KinemaxSurface

/**
 * Mini-reproductor estilo Spotify: barra sólida y plana a todo el ancho
 * (no una tarjeta flotante con degradado), con una línea de progreso
 * delgada pegada al borde superior — el detalle característico de Spotify.
 */
@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    crossfadeTransition: CrossfadeTransitionInfo? = null,
    crossfadeProgress: Float = 1f,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fromSong = crossfadeTransition?.fromSong
    val isTransitioning = crossfadeTransition != null && fromSong != null && fromSong.id != song.id && crossfadeProgress < 1f

    val targetFraction = if (durationMs > 0) (progressMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgressFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = if (isTransitioning) 400 else 200, easing = LinearEasing),
        label = "mini_progress_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(KinemaxSurfaceVariant)
            .clickable { onBarClick() }
            .testTag("mini_player_bar")
    ) {
        // Línea de progreso animada pegada arriba
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            val progressBrush = if (isTransitioning) {
                Brush.horizontalGradient(
                    colors = listOf(
                        KinemaxAccent,
                        Color(0xFFFF4081),
                        KinemaxAccent
                    )
                )
            } else {
                Brush.horizontalGradient(listOf(KinemaxAccent, KinemaxAccent))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgressFraction)
                    .height(2.5.dp)
                    .background(progressBrush)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(KinemaxSurface),
                contentAlignment = Alignment.Center
            ) {
                if (isTransitioning && fromSong != null) {
                    AsyncImage(
                        model = fromSong.portadaUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = (1f - crossfadeProgress).coerceIn(0f, 1f)
                                scaleX = 1f + (crossfadeProgress * 0.08f)
                                scaleY = 1f + (crossfadeProgress * 0.08f)
                            },
                        contentScale = ContentScale.Crop
                    )
                    AsyncImage(
                        model = song.portadaUrl,
                        contentDescription = null,
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
                        contentDescription = "Portada mini player",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.titulo,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artista,
                    style = MaterialTheme.typography.bodySmall,
                    color = KinemaxTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.testTag("mini_player_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onNextClick,
                modifier = Modifier.testTag("mini_player_next")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Siguiente",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
