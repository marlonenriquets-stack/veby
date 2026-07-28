package com.example.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ArtistAlbum
import com.example.data.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun AlbumDetailScreen(
    album: ArtistAlbum,
    artistName: String,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    permiteDescargas: Boolean,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val songs = album.songList

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KinemaxBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                KinemaxAccent.copy(alpha = 0.35f),
                                KinemaxBackground
                            )
                        )
                    )
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier.testTag("album_detail_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, KinemaxAccent, RoundedCornerShape(16.dp))
                            .background(KinemaxSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!album.portadaUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = album.portadaUrl,
                                contentDescription = "Portada de ${album.nombre}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Album,
                                contentDescription = "Álbum",
                                tint = KinemaxAccent,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = album.nombre,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("album_detail_title")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val count = songs.size
                    Text(
                        text = "$artistName • ${if (count == 1) "1 canción" else "$count canciones"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KinemaxTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (songs.isNotEmpty()) {
                                onSongClick(songs.first(), songs)
                            }
                        },
                        enabled = songs.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("play_album_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Reproducir Álbum (${songs.size})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (songs.isEmpty()) {
            item {
                Text(
                    text = "Este álbum no tiene canciones disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KinemaxTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        } else {
            items(songs) { song ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                        permiteDescargas = permiteDescargas,
                        onSongClick = { onSongClick(song, songs) },
                        onFavoriteClick = onFavoriteClick,
                        onDownloadClick = onDownloadClick,
                        onAddToPlaylistClick = onAddToPlaylistClick
                    )
                }
            }
        }
    }
}
