package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.example.data.model.Song
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxTextSecondary

private const val ARTIST_TAG = "ARTIST_ID"

/**
 * Muestra los créditos de artistas de una canción (principal + colaboradores) donde
 * CADA nombre es clicable a SU PROPIO perfil — a diferencia de un texto plano que
 * siempre navegaba al artista principal sin importar en qué nombre se tocara.
 *
 * Si la canción no trae el detalle de artistas (`song.artistas` vacío), cae de
 * vuelta a un solo bloque clicable hacia el artista principal.
 */
@Composable
fun ArtistCreditsText(
    song: Song,
    suffix: String? = null,
    style: TextStyle = MaterialTheme.typography.bodySmall,
    maxLines: Int = 1,
    onArtistClick: ((Long) -> Unit)? = null
) {
    val artistas = song.artistas?.filter { it.nombre.isNotBlank() } ?: emptyList()

    if (artistas.isEmpty()) {
        val plainText = if (!suffix.isNullOrBlank()) "${song.displayArtist} • $suffix" else song.displayArtist
        val artistId = song.mainArtistId
        val clickable = artistId != null && onArtistClick != null
        Text(
            text = plainText,
            style = style,
            color = if (clickable) KinemaxAccent else KinemaxTextSecondary,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = if (clickable) Modifier.clickable { onArtistClick!!(artistId!!) } else Modifier
        )
        return
    }

    val principal = artistas.find { it.rol == "principal" } ?: artistas.first()
    val colaboradores = artistas.filter { it !== principal }

    val annotated = buildAnnotatedString {
        pushStringAnnotation(tag = ARTIST_TAG, annotation = principal.id.toString())
        withStyle(SpanStyle(color = KinemaxAccent)) { append(principal.nombre) }
        pop()

        if (colaboradores.isNotEmpty()) {
            append(" ft. ")
            colaboradores.forEachIndexed { index, artista ->
                pushStringAnnotation(tag = ARTIST_TAG, annotation = artista.id.toString())
                withStyle(SpanStyle(color = KinemaxAccent)) { append(artista.nombre) }
                pop()
                if (index < colaboradores.size - 1) append(", ")
            }
        }

        if (!suffix.isNullOrBlank()) {
            withStyle(SpanStyle(color = KinemaxTextSecondary)) { append(" • $suffix") }
        }
    }

    ClickableText(
        text = annotated,
        style = style.copy(color = KinemaxTextSecondary),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = ARTIST_TAG, start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onArtistClick?.invoke(annotation.item.toLongOrNull() ?: return@let)
                }
        }
    )
}
