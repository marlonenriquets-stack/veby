package com.example.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.Song
import com.example.data.model.User
import com.example.ui.components.SongListItem
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxAccentGlow
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxGold
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxSurfaceVariant
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun HomeScreen(
    user: User?,
    topSongs: List<Song>,
    catalog: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    selectedPeriod: String,
    dataInfoMessage: String? = null,
    onPeriodSelected: (String) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: ((Song) -> Unit)? = null,
    onArtistClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val periods = listOf(
        "semana" to "Semana",
        "hoy" to "Hoy",
        "mes" to "Mes",
        "todo" to "Todo"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(KinemaxBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // User Greeting Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "¡Hola, ${user?.nombre?.ifEmpty { "Melómano" } ?: "Bienvenido"}!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Escucha tus canciones favoritas en Kinemax",
                        style = MaterialTheme.typography.bodySmall,
                        color = KinemaxTextSecondary
                    )
                }

                // Subscription Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (user?.esPremium == true) KinemaxGold.copy(alpha = 0.2f) else KinemaxSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (user?.esPremium == true) Icons.Default.Star else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (user?.esPremium == true) KinemaxGold else KinemaxTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (user?.esPremium == true) "PREMIUM" else "FREE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (user?.esPremium == true) KinemaxGold else KinemaxTextSecondary
                        )
                    }
                }
            }
        }

        // Data Load Log Banner (Visible UI indicator)
        if (!dataInfoMessage.isNullOrEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KinemaxAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = KinemaxAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dataInfoMessage,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        // Hero Featured Banner (Immersive UI)
        item {
            val heroSong = topSongs.firstOrNull() ?: catalog.firstOrNull()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF13131D),
                                Color(0xFF0D0D12)
                            )
                        )
                    )
                    .border(
                        border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
                    .testTag("hero_featured_banner")
            ) {
                Column {
                    Text(
                        text = "MÁS ESCUCHADA ESTA SEMANA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            fontSize = 10.sp
                        ),
                        color = KinemaxAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = heroSong?.titulo ?: "Vuelo Estelar",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${heroSong?.artista ?: "Kinemax Originals"} • 2.4M Reproducciones",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KinemaxTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (heroSong != null) {
                                onSongClick(heroSong, topSongs.ifEmpty { catalog })
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KinemaxAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("hero_listen_now_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Escuchar ahora",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Section: "Más escuchadas"
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = KinemaxAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Más escuchadas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Period Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(periods) { (key, label) ->
                        val isSelected = selectedPeriod == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPeriodSelected(key) },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = KinemaxAccent,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = KinemaxSurface,
                                labelColor = KinemaxTextSecondary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("period_chip_$key")
                        )
                    }
                }

                // Top Songs Carousel
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(topSongs) { song ->
                        TopSongCard(
                            song = song,
                            isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                            onClick = { onSongClick(song, topSongs) }
                        )
                    }
                }
            }
        }

        // Section: Catálogo
        item {
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Catálogo Kinemax",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        items(catalog) { song ->
            SongListItem(
                song = song,
                isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                permiteDescargas = user?.permiteDescargas ?: false,
                onSongClick = { onSongClick(song, catalog) },
                onFavoriteClick = onFavoriteClick,
                onDownloadClick = onDownloadClick,
                onAddToPlaylistClick = onAddToPlaylistClick,
                onArtistClick = onArtistClick,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TopSongCard(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(KinemaxSurface)
            .border(
                border = BorderStroke(1.dp, if (isPlaying) KinemaxAccent else Color(0x1AFFFFFF)),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("top_song_card_${song.id}")
    ) {
        Box(
            modifier = Modifier
                .size(124.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.portadaUrl,
                contentDescription = song.titulo,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(KinemaxAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = song.titulo,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
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
}
