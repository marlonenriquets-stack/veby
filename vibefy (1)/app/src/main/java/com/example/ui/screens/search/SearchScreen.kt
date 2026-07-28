package com.example.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Genre
import com.example.data.model.Song
import com.example.data.model.User
import com.example.ui.components.SongListItem
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@Composable
fun SearchScreen(
    catalog: List<Song>,
    generos: List<Genre> = emptyList(),
    selectedGenreId: Long? = null,
    user: User?,
    currentPlayingSong: Song?,
    isPlaying: Boolean,
    onGenreSelected: (Long?) -> Unit = {},
    onSongClick: (Song, List<Song>) -> Unit,
    onFavoriteClick: (Song) -> Unit,
    onDownloadClick: (Song) -> Unit,
    onAddToPlaylistClick: ((Song) -> Unit)? = null,
    onArtistClick: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = catalog.filter { song ->
        searchQuery.isEmpty() ||
                song.titulo.contains(searchQuery, ignoreCase = true) ||
                song.artista.contains(searchQuery, ignoreCase = true) ||
                (song.album?.contains(searchQuery, ignoreCase = true) == true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KinemaxBackground)
            .padding(top = 24.dp)
    ) {
        Text(
            text = "Buscar en Kinemax",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 16.dp)
        )

        // Search Text Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Canciones, artistas o álbumes") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KinemaxTextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = KinemaxTextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("search_input_field"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = KinemaxSurface,
                unfocusedContainerColor = KinemaxSurface,
                focusedBorderColor = KinemaxAccent,
                unfocusedBorderColor = KinemaxSurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Dynamic Genre Filter Chips
        if (generos.isEmpty()) {
            Text(
                text = "No hay géneros disponibles",
                style = MaterialTheme.typography.bodySmall,
                color = KinemaxTextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "Todos" chip
                item {
                    val isSelected = (selectedGenreId == null)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onGenreSelected(null) },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KinemaxAccent,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = KinemaxSurface,
                            labelColor = KinemaxTextSecondary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("genre_chip_todos")
                    )
                }

                items(generos, key = { it.id }) { genre ->
                    val isSelected = (selectedGenreId == genre.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                onGenreSelected(null)
                            } else {
                                onGenreSelected(genre.id)
                            }
                        },
                        label = {
                            val countStr = if (genre.numCanciones != null && genre.numCanciones > 0) " (${genre.numCanciones})" else ""
                            Text("${genre.nombre}$countStr")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = KinemaxAccent,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = KinemaxSurface,
                            labelColor = KinemaxTextSecondary
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("genre_chip_${genre.id}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (filteredSongs.isEmpty()) {
                item {
                    Text(
                        text = "No se encontraron canciones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = KinemaxTextSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)
                    )
                }
            } else {
                items(filteredSongs) { song ->
                    SongListItem(
                        song = song,
                        isPlaying = isPlaying && currentPlayingSong?.id == song.id,
                        permiteDescargas = user?.permiteDescargas ?: false,
                        onSongClick = { onSongClick(song, filteredSongs) },
                        onFavoriteClick = onFavoriteClick,
                        onDownloadClick = onDownloadClick,
                        onAddToPlaylistClick = onAddToPlaylistClick,
                        onArtistClick = onArtistClick,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
