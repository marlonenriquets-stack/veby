package com.example.ui.screens.library

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.User
import com.example.ui.components.SongListItem
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxGold
import com.example.ui.theme.KinemaxRed
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun LibraryScreen(
    user: User?,
    playlists: List<Playlist>,
    favoriteSongs: List<Song>,
    downloadedSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onRemoveSongFromPlaylist: (Long, Long) -> Unit = { _, _ -> },
    onSongClick: (Song, List<Song>) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onShowPremiumUpgrade: () -> Unit,
    onArtistClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Playlists", "Favoritos", "Descargas")
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KinemaxBackground)
            .padding(top = 24.dp)
    ) {
        // Top Title Row + Add Playlist Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tu Biblioteca",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (selectedTabIndex == 0) {
                IconButton(
                    onClick = { showCreatePlaylistDialog = true },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(KinemaxAccent)
                        .testTag("add_playlist_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear Playlist",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // 3 Tabs Header: Playlists, Favoritos, Descargas
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = KinemaxBackground,
            contentColor = KinemaxAccent,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = KinemaxAccent
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 15.sp
                        )
                    },
                    selectedContentColor = KinemaxAccent,
                    unselectedContentColor = KinemaxTextSecondary,
                    modifier = Modifier.testTag("library_tab_$index")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> PlaylistsTab(
                playlists = playlists,
                onDeletePlaylist = onDeletePlaylist,
                onSongClick = onSongClick,
                onRemoveSongFromPlaylist = onRemoveSongFromPlaylist
            )
            1 -> FavoritesTab(
                favoriteSongs = favoriteSongs,
                currentPlayingSong = currentPlayingSong,
                isPlaying = isPlaying,
                permiteDescargas = user?.permiteDescargas ?: false,
                onSongClick = { song -> onSongClick(song, favoriteSongs) },
                onFavoriteClick = onFavoriteClick,
                onDownloadClick = onDownloadClick,
                onArtistClick = onArtistClick
            )
            2 -> DownloadsTab(
                user = user,
                downloadedSongs = downloadedSongs,
                currentPlayingSong = currentPlayingSong,
                isPlaying = isPlaying,
                onSongClick = { song -> onSongClick(song, downloadedSongs) },
                onFavoriteClick = onFavoriteClick,
                onShowPremiumUpgrade = onShowPremiumUpgrade,
                onArtistClick = onArtistClick
            )
        }
    }

    // Dialog for creating a new Playlist
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { nombre ->
                onCreatePlaylist(nombre)
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
private fun PlaylistsTab(
    playlists: List<Playlist>,
    onDeletePlaylist: (Long) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onRemoveSongFromPlaylist: (Long, Long) -> Unit
) {
    var selectedPlaylistId by remember { mutableStateOf<Long?>(null) }
    val currentP = playlists.find { it.id == selectedPlaylistId }

    if (selectedPlaylistId != null && currentP != null) {
        AlertDialog(
            onDismissRequest = { selectedPlaylistId = null },
            containerColor = KinemaxSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentP.nombre,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!currentP.descripcion.isNullOrEmpty()) {
                            Text(
                                text = currentP.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                color = KinemaxTextSecondary
                            )
                        }
                    }
                    IconButton(onClick = {
                        onDeletePlaylist(currentP.id)
                        selectedPlaylistId = null
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = KinemaxRed)
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val songs = currentP.canciones ?: emptyList()
                    if (songs.isNotEmpty()) {
                        Button(
                            onClick = {
                                onSongClick(songs.first(), songs)
                                selectedPlaylistId = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text("Reproducir Playlist (${songs.size})")
                        }

                        LazyColumn(
                            modifier = Modifier.height(240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(songs) { song ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSongClick(song, songs)
                                            selectedPlaylistId = null
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = song.titulo,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = song.artista,
                                                fontSize = 12.sp,
                                                color = KinemaxTextSecondary
                                            )
                                        }
                                        IconButton(onClick = {
                                            onRemoveSongFromPlaylist(currentP.id, song.id)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Quitar canción",
                                                tint = KinemaxRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Esta playlist no tiene canciones aún. Agrega canciones desde el menú '+' de cada canción.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KinemaxTextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedPlaylistId = null }) {
                    Text("Cerrar", color = KinemaxTextSecondary)
                }
            }
        )
    }

    if (playlists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.QueueMusic,
                    contentDescription = null,
                    tint = KinemaxTextSecondary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aún no tienes playlists",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Crea tu primera playlist con el botón '+' superior.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KinemaxTextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(playlists) { playlist ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlaylistId = playlist.id },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = KinemaxSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(KinemaxAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueueMusic,
                                contentDescription = null,
                                tint = KinemaxAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.nombre,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${playlist.canciones?.size ?: playlist.cancionesCount ?: 0} canciones${if (!playlist.descripcion.isNullOrEmpty()) " • ${playlist.descripcion}" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = KinemaxTextSecondary
                            )
                        }
                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar playlist",
                                tint = KinemaxRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritesTab(
    favoriteSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    permiteDescargas: Boolean,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onArtistClick: ((Long) -> Unit)? = null
) {
    if (favoriteSongs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = KinemaxRed.copy(alpha = 0.5f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Sin canciones favoritas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Marca canciones con el corazón para guardarlas aquí.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KinemaxTextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 120.dp)
        ) {
            items(favoriteSongs) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                    permiteDescargas = permiteDescargas,
                    onSongClick = onSongClick,
                    onFavoriteClick = onFavoriteClick,
                    onDownloadClick = onDownloadClick,
                    onArtistClick = onArtistClick,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DownloadsTab(
    user: User?,
    downloadedSongs: List<Song>,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onShowPremiumUpgrade: () -> Unit,
    onArtistClick: ((Long) -> Unit)? = null
) {
    val permiteDescargas = user?.permiteDescargas ?: false

    if (!permiteDescargas) {
        // Free user indicator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = KinemaxGold,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Descargas Offline es una función Premium",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Actualiza tu plan Kinemax a Premium para descargar canciones y escucharlas sin conexión.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KinemaxTextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onShowPremiumUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent)
                ) {
                    Text("Obtener Kinemax Premium")
                }
            }
        }
    } else if (downloadedSongs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = KinemaxAccent,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No tienes canciones descargadas",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toca el ícono de descarga en cualquier canción para guardarla localmente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KinemaxTextSecondary
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 120.dp)
        ) {
            items(downloadedSongs) { song ->
                SongListItem(
                    song = song,
                    isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                    permiteDescargas = true,
                    onSongClick = onSongClick,
                    onFavoriteClick = onFavoriteClick,
                    onDownloadClick = null,
                    onArtistClick = onArtistClick,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = KinemaxSurface,
        title = { Text("Nueva Playlist", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la playlist") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KinemaxAccent,
                    unfocusedBorderColor = KinemaxTextSecondary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (nombre.isNotBlank()) onCreate(nombre) },
                colors = ButtonDefaults.buttonColors(containerColor = KinemaxAccent)
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = KinemaxTextSecondary)
            }
        }
    )
}
