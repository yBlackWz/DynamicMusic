package com.example.overlay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IslandSettings
import com.example.data.IslandThemeMode
import com.example.data.MediaTrack

/**
 * Interface compacta da Ilha Dinâmica exibida no topo da tela.
 * Exemplo conceitual: [ capa ] Música — Artista  ▮▮▮
 */
@Composable
fun DynamicIslandCompactView(
    track: MediaTrack,
    settings: IslandSettings,
    modifier: Modifier = Modifier
) {
    // Cores de fundo e texto adaptadas ao tema selecionado
    val backgroundColor = when (settings.themeMode) {
        IslandThemeMode.OLED_BLACK -> Color(0xFF000000).copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.DARK -> Color(0xFF16181D).copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.LIGHT -> Color(0xFFF1F3F9).copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.MATERIAL_YOU -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.ALBUM_ART_TINT -> Color(0xFF12141A).copy(alpha = settings.transparencyAlpha)
    }

    val contentColor = when (settings.themeMode) {
        IslandThemeMode.LIGHT -> Color(0xFF1E2024)
        else -> Color.White
    }

    val subtitleColor = contentColor.copy(alpha = 0.72f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(settings.cornerRadiusDp.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Capa do Álbum
        if (settings.showAlbumArt) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF262A34)),
                contentAlignment = Alignment.Center
            ) {
                if (track.artwork != null) {
                    Image(
                        bitmap = track.artwork.asImageBitmap(),
                        contentDescription = "Capa do álbum",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Música",
                        tint = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Título e Artista animados ao trocar de música
        AnimatedContent(
            targetState = track.displayTitle to track.displayArtist,
            transitionSpec = {
                (slideInHorizontally { width -> width / 2 } + fadeIn()) togetherWith
                        (slideOutHorizontally { width -> -width / 2 } + fadeOut())
            },
            modifier = Modifier.weight(1f, fill = false),
            label = "track_title_artist"
        ) { (title, artist) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
            ) {
                if (settings.showTitle) {
                    Text(
                        text = title,
                        color = contentColor,
                        fontSize = (12.sp * settings.textSizeMultiplier),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (settings.showTitle && settings.showArtist && artist.isNotBlank()) {
                    Text(
                        text = " — ",
                        color = subtitleColor,
                        fontSize = (11.sp * settings.textSizeMultiplier),
                        fontWeight = FontWeight.Normal
                    )
                }

                if (settings.showArtist && artist.isNotBlank()) {
                    Text(
                        text = artist,
                        color = subtitleColor,
                        fontSize = (11.sp * settings.textSizeMultiplier),
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Visualizador de áudio animado
        if (settings.showVisualizer) {
            Spacer(modifier = Modifier.width(8.dp))
            WaveAudioVisualizer(
                isPlaying = track.isPlaying,
                barCount = 3,
                barColor = if (settings.themeMode == IslandThemeMode.LIGHT) Color(0xFF0F9D58) else Color(0xFF38EF7D),
                maxHeight = 14.dp,
                barWidth = 2.5.dp,
                spacing = 2.dp
            )
        }
    }
}
