package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.DynamicIslandApplication
import com.example.permissions.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as? DynamicIslandApplication ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val settings = app.settingsRepository.settingsFlow.first()
                if (settings.isEnabled && settings.autoStartOnBoot) {
                    if (PermissionUtils.hasOverlayPermission(context)) {
                        DynamicIslandOverlayService.start(context)
                    }
                }
            }
        }
    }
}
