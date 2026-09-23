package com.example.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.MediaStateRepository
import com.example.data.MediaTrack

/**
 * Visualização expandida da Ilha Dinâmica com capa grande, controles de reprodução,
 * barra de progresso com seek interativo e atalho para o player.
 */
@Composable
fun DynamicIslandExpandedView(
    track: MediaTrack,
    settings: IslandSettings,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (settings.themeMode) {
        IslandThemeMode.OLED_BLACK -> Color(0xFF000000).copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.DARK -> Color(0xFF16181D).copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.LIGHT -> Color(0xFFFFFFFF).copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.MATERIAL_YOU -> MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = settings.transparencyAlpha)
        IslandThemeMode.ALBUM_ART_TINT -> Color(0xFF141720).copy(alpha = settings.transparencyAlpha)
    }

    val contentColor = when (settings.themeMode) {
        IslandThemeMode.LIGHT -> Color(0xFF1C1E22)
        else -> Color.White
    }

    val secondaryTextColor = contentColor.copy(alpha = 0.65f)

    var isUserSeeking by remember { mutableStateOf(false) }
    var seekFraction by remember { mutableFloatStateOf(0f) }

    val currentFraction = if (isUserSeeking) seekFraction else track.progressFraction

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(settings.cornerRadiusDp.dp))
            .background(backgroundColor)
            .padding(14.dp)
    ) {
        // Cabeçalho: Capa, Título, Artista, Álbum e Botão Fechar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Capa grande
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF232732)),
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
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Textos informativos
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = track.displayTitle,
                    color = contentColor,
                    fontSize = (15.sp * settings.textSizeMultiplier),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                )

                Text(
                    text = track.displayArtist,
                    color = secondaryTextColor,
                    fontSize = (13.sp * settings.textSizeMultiplier),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (settings.showAlbum && track.album.isNotBlank()) {
                    Text(
                        text = track.album,
                        color = secondaryTextColor.copy(alpha = 0.5f),
                        fontSize = (11.sp * settings.textSizeMultiplier),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Botão recolher
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.12f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onCollapse() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Recolher ilha",
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Barra de progresso com seek interativo
        if (settings.showProgress && track.durationMs > 0) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = currentFraction,
                    onValueChange = {
                        isUserSeeking = true
                        seekFraction = it
                    },
                    onValueChangeFinished = {
                        isUserSeeking = false
                        val targetMs = (seekFraction * track.durationMs).toLong()
                        MediaStateRepository.onSeekTo(targetMs)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = contentColor,
                        activeTrackColor = contentColor,
                        inactiveTrackColor = contentColor.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val displayPosMs = if (isUserSeeking) {
                        (seekFraction * track.durationMs).toLong()
                    } else {
                        track.positionMs
                    }

                    Text(
                        text = formatMs(displayPosMs),
                        color = secondaryTextColor,
                        fontSize = 11.sp
                    )

                    Text(
                        text = track.formattedDuration,
                        color = secondaryTextColor,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Linha de controles de reprodução e atalho do player
        if (settings.showControls) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Indicador do app / botão abrir aplicativo
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(contentColor.copy(alpha = 0.1f))
                        .clickable { MediaStateRepository.onOpenPlayerApp() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Abrir aplicativo",
                        tint = contentColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (track.appName.isNotBlank()) track.appName else "Abrir",
                        color = contentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Controles de transporte: Anterior, Play/Pause, Próximo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Anterior
                    IconButton(
                        onClick = { MediaStateRepository.onSkipPrevious() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Música anterior",
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Play / Pause em botão circular destacado estilo One UI
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(contentColor)
                            .clickable { MediaStateRepository.onTogglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (track.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (track.isPlaying) "Pausar" else "Reproduzir",
                            tint = if (settings.themeMode == IslandThemeMode.LIGHT) Color.White else Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Próximo
                    IconButton(
                        onClick = { MediaStateRepository.onSkipNext() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Próxima música",
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMs(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
