package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.Song
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    currentSong: Song?,
    queue: List<Song>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPlayFromQueue: (Song) -> Unit,
    onRemoveFromQueue: (Song) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KinemaxSurface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(
                text = "Cola de reproducción",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (currentSong != null) {
                Text(
                    text = "Reproduciendo ahora",
                    style = MaterialTheme.typography.labelMedium,
                    color = KinemaxTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )
                QueueRow(
                    song = currentSong,
                    isCurrent = true,
                    onClick = {},
                    onRemove = null
                )
            }

            if (queue.isNotEmpty()) {
                Text(
                    text = "A continuación",
                    style = MaterialTheme.typography.labelMedium,
                    color = KinemaxTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, top = 12.dp, bottom = 6.dp)
                )
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    items(queue) { song ->
                        QueueRow(
                            song = song,
                            isCurrent = false,
                            onClick = { onPlayFromQueue(song) },
                            onRemove = { onRemoveFromQueue(song) }
                        )
                    }
                }
            } else {
                Text(
                    text = "No hay más canciones en la cola. Agrega algunas desde el catálogo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = KinemaxTextSecondary,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isCurrent) { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = song.portadaUrl,
                contentDescription = song.titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp)
            )
            if (isCurrent) {
                Box(modifier = Modifier.size(48.dp).background(Color.Black.copy(alpha = 0.4f)))
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Sonando ahora",
                    tint = KinemaxAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ✅ AQUÍ ESTABA EL ERROR: Se cambió Spacer(Modifier.padding(...)) por Spacer(Modifier.width(12.dp))
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.titulo,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal),
                color = if (isCurrent) KinemaxAccent else MaterialTheme.colorScheme.onBackground,
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

        if (onRemove != null) {
            IconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Quitar de la cola", tint = KinemaxTextSecondary)
            }
        } else {
            Icon(imageVector = Icons.Default.DragHandle, contentDescription = null, tint = KinemaxTextSecondary.copy(alpha = 0.3f))
        }
    }
}
