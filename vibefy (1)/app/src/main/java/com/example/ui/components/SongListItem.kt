package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.KinemaxGold
import com.example.ui.theme.KinemaxRed
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun SongListItem(
    song: Song,
    isPlaying: Boolean = false,
    permiteDescargas: Boolean = false,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: ((Song) -> Unit)? = null,
    onDownloadClick: ((Song) -> Unit)? = null,
    onAddToPlaylistClick: ((Song) -> Unit)? = null,
    onArtistClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KinemaxSurface)
            .border(
                border = BorderStroke(1.dp, if (isPlaying) KinemaxAccent.copy(alpha = 0.5f) else Color(0x1AFFFFFF)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSongClick(song) }
            .padding(12.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cover Image with play indicator overlay
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.portadaUrl,
                contentDescription = "Portada de ${song.titulo}",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(KinemaxAccent.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Reproduciendo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.titulo,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = if (isPlaying) KinemaxAccent else MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ArtistCreditsText(
                song = song,
                suffix = song.album ?: "Single",
                style = MaterialTheme.typography.bodySmall,
                onArtistClick = onArtistClick
            )
        }

        // Add To Playlist Button
        if (onAddToPlaylistClick != null) {
            IconButton(
                onClick = { onAddToPlaylistClick(song) },
                modifier = Modifier.testTag("add_to_playlist_btn_${song.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "Agregar a Playlist",
                    tint = KinemaxTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Favorite Toggle Button
        if (onFavoriteClick != null) {
            IconButton(
                onClick = { onFavoriteClick(song) },
                modifier = Modifier.testTag("favorite_btn_${song.id}")
            ) {
                Icon(
                    imageVector = if (song.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (song.isFavorite) KinemaxRed else KinemaxTextSecondary
                )
            }
        }

        // Download Button (with Lock badge if free user)
        if (onDownloadClick != null) {
            IconButton(
                onClick = { onDownloadClick(song) },
                modifier = Modifier.testTag("download_btn_${song.id}")
            ) {
                if (!permiteDescargas) {
                    // Lock icon for free users
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Función Premium (Candado)",
                        tint = KinemaxGold,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = if (song.isDownloaded) Icons.Default.Download else Icons.Outlined.Download,
                        contentDescription = "Descargar",
                        tint = if (song.isDownloaded) KinemaxAccent else KinemaxTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
