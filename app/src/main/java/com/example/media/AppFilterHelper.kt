package com.example.media

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledMusicApp(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isKnownMusicApp: Boolean
)

object AppFilterHelper {

    private val KNOWN_MUSIC_PACKAGES = setOf(
        "com.spotify.music",
        "com.google.android.apps.youtube.music",
        "deezer.android.app",
        "com.amazon.mp3",
        "com.apple.android.music",
        "com.soundcloud.android",
        "com.aspiro.tidal",
        "com.maxmpz.audioplayer", // Poweramp
        "in.krosbits.musicolet", // Musicolet
        "com.sec.android.app.music", // Samsung Music
        "org.videolan.vlc",
        "com.stellio.player",
        "com.extreamsd.usbaudioplayerpro",
        "com.jrtstudio.AnotherMusicPlayer"
    )

    fun getInstalledMediaApps(context: Context): List<InstalledMusicApp> {
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val result = mutableListOf<InstalledMusicApp>()
        val seen = mutableSetOf<String>()

        // 1. Procura primeiro os apps conhecidos
        for (app in installedApps) {
            if (KNOWN_MUSIC_PACKAGES.contains(app.packageName) && seen.add(app.packageName)) {
                val name = pm.getApplicationLabel(app).toString()
                val icon = pm.getApplicationIcon(app)
                result.add(InstalledMusicApp(app.packageName, name, icon, isKnownMusicApp = true))
            }
        }

        // 2. Busca apps que respondem ao intent de reprodução de áudio
        val audioIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(android.net.Uri.parse("file:///dummy.mp3"), "audio/*")
        }
        val resolveInfos = pm.queryIntentActivities(audioIntent, 0)
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (seen.add(pkg) && pkg != context.packageName) {
                try {
                    val appInfo = pm.getApplicationInfo(pkg, 0)
                    val name = pm.getApplicationLabel(appInfo).toString()
                    val icon = pm.getApplicationIcon(appInfo)
                    result.add(InstalledMusicApp(pkg, name, icon, isKnownMusicApp = false))
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        // Se nenhum app conhecido estiver instalado (ex: emulador), inclui os principais como sugestão visual
        if (result.isEmpty()) {
            result.add(InstalledMusicApp("com.spotify.music", "Spotify", null, true))
            result.add(InstalledMusicApp("com.google.android.apps.youtube.music", "YouTube Music", null, true))
            result.add(InstalledMusicApp("in.krosbits.musicolet", "Musicolet", null, true))
            result.add(InstalledMusicApp("com.maxmpz.audioplayer", "Poweramp", null, true))
            result.add(InstalledMusicApp("com.sec.android.app.music", "Samsung Music", null, true))
        }

        return result.sortedByDescending { it.isKnownMusicApp }
    }
}
