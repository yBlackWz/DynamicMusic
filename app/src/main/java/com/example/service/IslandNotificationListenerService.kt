package com.example.service

import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.media.MediaSessionHelper

/**
 * Serviço de escuta de notificações do Android que concede acesso
 * às sessões ativas de mídia pelo MediaSessionManager.
 */
class IslandNotificationListenerService : NotificationListenerService() {

    private var mediaSessionHelper: MediaSessionHelper? = null

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListener conectado com sucesso")
        instance = this
        mediaSessionHelper = MediaSessionHelper(this) {
            // Callback quando sessões mudam
        }.also {
            it.startListening()
        }

        // Inicia o OverlayService para exibir a cápsula caso o app esteja ativado
        DynamicIslandOverlayService.start(this)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListener desconectado")
        instance = null
        mediaSessionHelper?.stopListening()
        mediaSessionHelper = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        // Notificações de mídia frequentemente atualizam metadados da sessão
        mediaSessionHelper?.refreshSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        mediaSessionHelper?.refreshSessions()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        mediaSessionHelper?.stopListening()
        mediaSessionHelper = null
    }

    companion object {
        private const val TAG = "IslandNotificationListener"
        var instance: IslandNotificationListenerService? = null
            private set
    }
}
