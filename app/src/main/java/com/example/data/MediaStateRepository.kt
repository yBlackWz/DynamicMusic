package com.example.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Interface para envio de comandos de transporte de mídia (play/pause/skip/seek).
 */
interface MediaCommandListener {
    fun play()
    fun pause()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(positionMs: Long)
    fun openPlayerApp()
}

/**
 * Repositório central de estado de mídia compartilhado entre o serviço, a ilha flutuante e a UI.
 */
object MediaStateRepository {

    private val _currentTrack = MutableStateFlow<MediaTrack?>(null)
    val currentTrack: StateFlow<MediaTrack?> = _currentTrack.asStateFlow()

    private val _islandState = MutableStateFlow(IslandState.COMPACT)
    val islandState: StateFlow<IslandState> = _islandState.asStateFlow()

    private var commandListener: MediaCommandListener? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private var simulationProgressJob: Job? = null

    // Track de simulação para testes e apresentação
    private var simulationTrackIndex = 0
    private val simulationPlaylist = listOf(
        MediaTrack(
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            packageName = "com.spotify.music",
            appName = "Spotify",
            durationMs = 230000L,
            positionMs = 45000L,
            isPlaying = true,
            artwork = createColoredArtwork(0xFF1DB954.toInt(), "Starboy")
        ),
        MediaTrack(
            title = "Blinding Lights",
            artist = "The Weeknd",
            album = "After Hours",
            packageName = "com.spotify.music",
            appName = "Spotify",
            durationMs = 200000L,
            positionMs = 12000L,
            isPlaying = true,
            artwork = createColoredArtwork(0xFFE50914.toInt(), "Lights")
        ),
        MediaTrack(
            title = "Levitating",
            artist = "Dua Lipa",
            album = "Future Nostalgia",
            packageName = "com.google.android.apps.youtube.music",
            appName = "YouTube Music",
            durationMs = 203000L,
            positionMs = 78000L,
            isPlaying = true,
            artwork = createColoredArtwork(0xFF6C5CE7.toInt(), "Dua")
        )
    )

    fun registerCommandListener(listener: MediaCommandListener?) {
        this.commandListener = listener
    }

    fun updateTrack(track: MediaTrack?) {
        _currentTrack.value = track
    }

    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long) {
        val curr = _currentTrack.value ?: return
        _currentTrack.value = curr.copy(
            isPlaying = isPlaying,
            positionMs = positionMs,
            lastUpdateEpoch = System.currentTimeMillis()
        )
    }

    fun setIslandState(state: IslandState) {
        _islandState.value = state
    }

    // Comandos disparados pela UI ou cápsula
    fun onPlay() {
        if (isSimulationActive()) {
            val curr = _currentTrack.value ?: return
            _currentTrack.value = curr.copy(isPlaying = true)
            startSimulationTicker()
        } else {
            commandListener?.play()
        }
    }

    fun onPause() {
        if (isSimulationActive()) {
            val curr = _currentTrack.value ?: return
            _currentTrack.value = curr.copy(isPlaying = false)
            stopSimulationTicker()
        } else {
            commandListener?.pause()
        }
    }

    fun onTogglePlayPause() {
        val curr = _currentTrack.value ?: return
        if (curr.isPlaying) {
            onPause()
        } else {
            onPlay()
        }
    }

    fun onSkipNext() {
        if (isSimulationActive()) {
            simulationTrackIndex = (simulationTrackIndex + 1) % simulationPlaylist.size
            _currentTrack.value = simulationPlaylist[simulationTrackIndex].copy(
                positionMs = 0L,
                isPlaying = true
            )
            startSimulationTicker()
        } else {
            commandListener?.skipToNext()
        }
    }

    fun onSkipPrevious() {
        if (isSimulationActive()) {
            simulationTrackIndex = if (simulationTrackIndex - 1 < 0) simulationPlaylist.size - 1 else simulationTrackIndex - 1
            _currentTrack.value = simulationPlaylist[simulationTrackIndex].copy(
                positionMs = 0L,
                isPlaying = true
            )
            startSimulationTicker()
        } else {
            commandListener?.skipToPrevious()
        }
    }

    fun onSeekTo(positionMs: Long) {
        if (isSimulationActive()) {
            val curr = _currentTrack.value ?: return
            _currentTrack.value = curr.copy(positionMs = positionMs.coerceIn(0L, curr.durationMs))
        } else {
            commandListener?.seekTo(positionMs)
        }
    }

    fun onOpenPlayerApp() {
        commandListener?.openPlayerApp()
    }

    // Métodos para suporte ao Modo Simulação / Teste Rápido
    fun startSimulation() {
        val track = simulationPlaylist[simulationTrackIndex].copy(isPlaying = true)
        _currentTrack.value = track
        _islandState.value = IslandState.COMPACT
        startSimulationTicker()
    }

    fun stopSimulation() {
        stopSimulationTicker()
        _currentTrack.value = null
    }

    private fun isSimulationActive(): Boolean {
        return _currentTrack.value?.appName == "Spotify" || _currentTrack.value?.appName == "YouTube Music" && commandListener == null
    }

    private fun startSimulationTicker() {
        simulationProgressJob?.cancel()
        simulationProgressJob = scope.launch {
            while (isActive) {
                delay(1000L)
                val curr = _currentTrack.value
                if (curr != null && curr.isPlaying) {
                    val nextPos = curr.positionMs + 1000L
                    if (nextPos >= curr.durationMs) {
                        onSkipNext()
                    } else {
                        _currentTrack.value = curr.copy(positionMs = nextPos)
                    }
                }
            }
        }
    }

    private fun stopSimulationTicker() {
        simulationProgressJob?.cancel()
        simulationProgressJob = null
    }

    private fun createColoredArtwork(bgColor: Int, label: String): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        paint.color = bgColor
        canvas.drawRoundRect(RectF(0f, 0f, size.toFloat(), size.toFloat()), 32f, 32f, paint)

        paint.color = Color.WHITE
        paint.textSize = 36f
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true
        canvas.drawText(label.take(6), size / 2f, size / 2f + 12f, paint)

        return bitmap
    }
}
