package com.example.data

import android.graphics.Bitmap

/**
 * Informações completas sobre a faixa e estado da sessão de mídia ativa.
 */
data class MediaTrack(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val packageName: String = "",
    val appName: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val artwork: Bitmap? = null,
    val canSkipNext: Boolean = true,
    val canSkipPrevious: Boolean = true,
    val canSeek: Boolean = true,
    val lastUpdateEpoch: Long = System.currentTimeMillis()
) {
    val displayTitle: String
        get() = if (title.isNotBlank()) title else "Sem faixa"

    val displayArtist: String
        get() = if (artist.isNotBlank()) artist else (if (appName.isNotBlank()) appName else "Player de música")

    val formattedPosition: String
        get() = formatTime(positionMs)

    val formattedDuration: String
        get() = formatTime(durationMs)

    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    private fun formatTime(millis: Long): String {
        if (millis <= 0) return "0:00"
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes >= 60) {
            val hours = minutes / 60
            val remMinutes = minutes % 60
            String.format("%d:%02d:%02d", hours, remMinutes, seconds)
        } else {
            String.format("%d:%02d", minutes, seconds)
        }
    }
}
