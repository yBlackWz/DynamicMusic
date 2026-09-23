package com.example.media

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.MediaCommandListener
import com.example.data.MediaStateRepository
import com.example.data.MediaTrack
import com.example.service.IslandNotificationListenerService

/**
 * Gerencia a detecção de sessões ativas de mídia usando MediaSessionManager e MediaController.
 */
class MediaSessionHelper(
    private val context: Context,
    private val onSessionsChangedListener: (() -> Unit)? = null
) : MediaCommandListener {

    private val mediaSessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val componentName = ComponentName(context, IslandNotificationListenerService::class.java)

    private var activeControllers: List<MediaController> = emptyList()
    private var currentActiveController: MediaController? = null
    private val handler = Handler(Looper.getMainLooper())

    private var allowedPackages: Set<String> = emptySet()

    private val sessionsChangedListener =
        MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
            handler.post {
                updateControllers(controllers)
                onSessionsChangedListener?.invoke()
            }
        }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            handler.post {
                evaluateActiveSession()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            handler.post {
                evaluateActiveSession()
            }
        }

        override fun onSessionDestroyed() {
            handler.post {
                refreshSessions()
            }
        }
    }

    fun startListening(allowed: Set<String> = emptySet()) {
        this.allowedPackages = allowed
        MediaStateRepository.registerCommandListener(this)
        try {
            mediaSessionManager.addOnActiveSessionsChangedListener(
                sessionsChangedListener,
                componentName
            )
            refreshSessions()
        } catch (e: SecurityException) {
            Log.w("MediaSessionHelper", "Acesso a notificações não concedido ainda para MediaSessionManager", e)
        }
    }

    fun stopListening() {
        MediaStateRepository.registerCommandListener(null)
        try {
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener)
        } catch (e: Exception) {
            Log.e("MediaSessionHelper", "Erro ao remover ouvinte de sessões", e)
        }
        unregisterCurrentCallback()
    }

    fun setAllowedPackages(packages: Set<String>) {
        this.allowedPackages = packages
        evaluateActiveSession()
    }

    fun refreshSessions() {
        try {
            val controllers = mediaSessionManager.getActiveSessions(componentName)
            updateControllers(controllers)
        } catch (e: SecurityException) {
            Log.w("MediaSessionHelper", "Permissão necessária para obter sessões ativas", e)
        }
    }

    private fun updateControllers(controllers: List<MediaController>?) {
        unregisterCurrentCallback()
        activeControllers = controllers ?: emptyList()
        evaluateActiveSession()
    }

    private fun evaluateActiveSession() {
        if (activeControllers.isEmpty()) {
            MediaStateRepository.updateTrack(null)
            currentActiveController = null
            return
        }

        // Filtra apps permitidos caso a lista não esteja vazia
        val candidates = if (allowedPackages.isNotEmpty()) {
            activeControllers.filter { allowedPackages.contains(it.packageName) }
        } else {
            activeControllers
        }

        if (candidates.isEmpty()) {
            MediaStateRepository.updateTrack(null)
            currentActiveController = null
            return
        }

        // Prioriza a sessão que estiver tocando (STATE_PLAYING)
        val selectedController = candidates.firstOrNull {
            val state = it.playbackState?.state
            state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
        } ?: candidates.firstOrNull {
            // Em seguida, o que estiver pausado com metadados
            val state = it.playbackState?.state
            state == PlaybackState.STATE_PAUSED
        } ?: candidates.first()

        if (currentActiveController?.sessionToken != selectedController.sessionToken) {
            unregisterCurrentCallback()
            currentActiveController = selectedController
            try {
                selectedController.registerCallback(controllerCallback, handler)
            } catch (e: Exception) {
                Log.e("MediaSessionHelper", "Falha ao registrar callback do controlador", e)
            }
        }

        extractTrackFromController(selectedController)
    }

    private fun extractTrackFromController(controller: MediaController) {
        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING ||
                playbackState?.state == PlaybackState.STATE_BUFFERING

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: ""

        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_AUTHOR)
            ?: ""

        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""

        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val position = playbackState?.position ?: 0L

        val artwork: Bitmap? = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val appName = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(controller.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            controller.packageName
        }

        val actions = playbackState?.actions ?: 0L
        val canSkipNext = (actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L
        val canSkipPrevious = (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L
        val canSeek = (actions and PlaybackState.ACTION_SEEK_TO) != 0L

        // Se não houver título nem artista e o estado for parado/none, não exibe
        if (title.isBlank() && artist.isBlank() && !isPlaying) {
            MediaStateRepository.updateTrack(null)
            return
        }

        val track = MediaTrack(
            title = title,
            artist = artist,
            album = album,
            packageName = controller.packageName,
            appName = appName,
            durationMs = duration,
            positionMs = position,
            isPlaying = isPlaying,
            artwork = artwork,
            canSkipNext = canSkipNext || true, // manter acionável para o transporte
            canSkipPrevious = canSkipPrevious || true,
            canSeek = canSeek,
            lastUpdateEpoch = System.currentTimeMillis()
        )

        MediaStateRepository.updateTrack(track)
    }

    private fun unregisterCurrentCallback() {
        try {
            currentActiveController?.unregisterCallback(controllerCallback)
        } catch (e: Exception) {
            // Ignora se já estiver cancelado
        }
    }

    // MediaCommandListener implementado
    override fun play() {
        currentActiveController?.transportControls?.play()
    }

    override fun pause() {
        currentActiveController?.transportControls?.pause()
    }

    override fun skipToNext() {
        currentActiveController?.transportControls?.skipToNext()
    }

    override fun skipToPrevious() {
        currentActiveController?.transportControls?.skipToPrevious()
    }

    override fun seekTo(positionMs: Long) {
        currentActiveController?.transportControls?.seekTo(positionMs)
    }

    override fun openPlayerApp() {
        val pkg = currentActiveController?.packageName ?: return
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e("MediaSessionHelper", "Não foi possível abrir o player", e)
        }
    }
}
