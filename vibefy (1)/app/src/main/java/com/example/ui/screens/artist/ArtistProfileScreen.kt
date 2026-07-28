package com.example.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ArtistAlbum
import com.example.data.model.FullArtistProfile
import com.example.data.model.Song
import com.example.ui.components.SongListItem
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun ArtistProfileScreen(
    artistProfile: FullArtistProfile?,
    isLoading: Boolean,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    permiteDescargas: Boolean,
    onBackClick: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onAlbumClick: (ArtistAlbum) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KinemaxBackground)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KinemaxAccent)
            }
        } else if (artistProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No se encontró la información del artista",
                        color = KinemaxTextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = KinemaxAccent
                        )
                    }
                }
            }
        } else {
            val topSongs = artistProfile.topCanciones?.takeIf { it.isNotEmpty() }
                ?: artistProfile.canciones
                ?: emptyList()
            val albumes = artistProfile.albumes ?: emptyList()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Top Header with Back Button and Large Circular Avatar
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
                            // Back Button row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                IconButton(
                                    onClick = onBackClick,
                                    modifier = Modifier.testTag("artist_profile_back_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Large Circular Profile Image
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, KinemaxAccent, CircleShape)
                                    .background(KinemaxSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!artistProfile.fotoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = artistProfile.fotoUrl,
                                        contentDescription = "Foto de ${artistProfile.nombre}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Artista",
                                        tint = KinemaxAccent,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Artist Name
                            Text(
                                text = artistProfile.nombre,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag("artist_name_title")
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Formatted Plays
                            Text(
                                text = artistProfile.formattedPlays,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = KinemaxAccent
                            )

                            // Bio if available
                            artistProfile.displayBio?.takeIf { it.isNotBlank() }?.let { bio ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = bio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KinemaxTextSecondary,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                        }
                    }
                }

                // Section: Albums (if available)
                if (albumes.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Álbumes",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                            )

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(albumes) { album ->
                                    ArtistAlbumCard(
                                        album = album,
                                        onClick = { onAlbumClick(album) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Top Canciones
                item {
                    Text(
                        text = "Top canciones",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 12.dp)
                    )
                }

                if (topSongs.isEmpty()) {
                    item {
                        Text(
                            text = "No hay canciones disponibles para este artista.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = KinemaxTextSecondary,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    items(topSongs) { song ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                            SongListItem(
                                song = song,
                                isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                                permiteDescargas = permiteDescargas,
                                onSongClick = { onSongClick(song, topSongs) },
                                onFavoriteClick = onFavoriteClick,
                                onDownloadClick = onDownloadClick,
                                onAddToPlaylistClick = onAddToPlaylistClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistAlbumCard(
    album: ArtistAlbum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KinemaxSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KinemaxAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (!album.portadaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = album.portadaUrl,
                        contentDescription = album.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        tint = KinemaxAccent,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = album.nombre,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val count = album.numCanciones ?: 0
            Text(
                text = if (count == 1) "1 canción" else "$count canciones",
                style = MaterialTheme.typography.bodySmall,
                color = KinemaxTextSecondary
            )
        }
    }
}
