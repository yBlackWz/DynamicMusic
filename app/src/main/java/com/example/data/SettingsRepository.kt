package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "island_settings")

/**
 * Repositório responsável pela persistência das configurações da Ilha Dinâmica.
 */
class SettingsRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val POSITION_PRESET = stringPreferencesKey("position_preset")
        val OFFSET_X = intPreferencesKey("offset_x")
        val OFFSET_Y = intPreferencesKey("offset_y")
        val ALLOW_DRAG_REPOSITION = booleanPreferencesKey("allow_drag_reposition")

        val COMPACT_SCALE = floatPreferencesKey("compact_scale")
        val COMPACT_WIDTH_DP = intPreferencesKey("compact_width_dp")
        val COMPACT_HEIGHT_DP = intPreferencesKey("compact_height_dp")
        val EXPANDED_WIDTH_DP = intPreferencesKey("expanded_width_dp")
        val CORNER_RADIUS_DP = intPreferencesKey("corner_radius_dp")
        val TRANSPARENCY_ALPHA = floatPreferencesKey("transparency_alpha")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val TEXT_SIZE_MULTIPLIER = floatPreferencesKey("text_size_multiplier")

        val SHOW_ALBUM_ART = booleanPreferencesKey("show_album_art")
        val SHOW_ARTIST = booleanPreferencesKey("show_artist")
        val SHOW_TITLE = booleanPreferencesKey("show_title")
        val SHOW_ALBUM = booleanPreferencesKey("show_album")
        val SHOW_PROGRESS = booleanPreferencesKey("show_progress")
        val SHOW_VISUALIZER = booleanPreferencesKey("show_visualizer")
        val SHOW_CONTROLS = booleanPreferencesKey("show_controls")

        val EXPAND_ON_TAP = booleanPreferencesKey("expand_on_tap")
        val AUTO_COLLAPSE_SEC = intPreferencesKey("auto_collapse_sec")
        val ENABLE_SWIPE_GESTURES = booleanPreferencesKey("enable_swipe_gestures")
        val VIBRATE_ON_TOUCH = booleanPreferencesKey("vibrate_on_touch")
        val AUTO_START_ON_BOOT = booleanPreferencesKey("auto_start_on_boot")
        val SIMULATION_MODE = booleanPreferencesKey("simulation_mode")

        val ALLOWED_PACKAGES = stringSetPreferencesKey("allowed_packages")
    }

    val settingsFlow: Flow<IslandSettings> = context.dataStore.data.map { prefs ->
        val presetStr = prefs[PreferencesKeys.POSITION_PRESET] ?: IslandPositionPreset.TOP_LEFT.name
        val themeStr = prefs[PreferencesKeys.THEME_MODE] ?: IslandThemeMode.OLED_BLACK.name

        val preset = runCatching { IslandPositionPreset.valueOf(presetStr) }
            .getOrDefault(IslandPositionPreset.TOP_LEFT)
        val themeMode = runCatching { IslandThemeMode.valueOf(themeStr) }
            .getOrDefault(IslandThemeMode.OLED_BLACK)

        IslandSettings(
            isEnabled = prefs[PreferencesKeys.IS_ENABLED] ?: true,
            positionPreset = preset,
            offsetX = prefs[PreferencesKeys.OFFSET_X] ?: 16,
            offsetY = prefs[PreferencesKeys.OFFSET_Y] ?: 28,
            allowDragReposition = prefs[PreferencesKeys.ALLOW_DRAG_REPOSITION] ?: true,

            compactScale = prefs[PreferencesKeys.COMPACT_SCALE] ?: 1.0f,
            compactWidthDp = prefs[PreferencesKeys.COMPACT_WIDTH_DP] ?: 210,
            compactHeightDp = prefs[PreferencesKeys.COMPACT_HEIGHT_DP] ?: 42,
            expandedWidthDp = prefs[PreferencesKeys.EXPANDED_WIDTH_DP] ?: 340,
            cornerRadiusDp = prefs[PreferencesKeys.CORNER_RADIUS_DP] ?: 24,
            transparencyAlpha = prefs[PreferencesKeys.TRANSPARENCY_ALPHA] ?: 0.95f,
            themeMode = themeMode,
            textSizeMultiplier = prefs[PreferencesKeys.TEXT_SIZE_MULTIPLIER] ?: 1.0f,

            showAlbumArt = prefs[PreferencesKeys.SHOW_ALBUM_ART] ?: true,
            showArtist = prefs[PreferencesKeys.SHOW_ARTIST] ?: true,
            showTitle = prefs[PreferencesKeys.SHOW_TITLE] ?: true,
            showAlbum = prefs[PreferencesKeys.SHOW_ALBUM] ?: true,
            showProgress = prefs[PreferencesKeys.SHOW_PROGRESS] ?: true,
            showVisualizer = prefs[PreferencesKeys.SHOW_VISUALIZER] ?: true,
            showControls = prefs[PreferencesKeys.SHOW_CONTROLS] ?: true,

            expandOnTap = prefs[PreferencesKeys.EXPAND_ON_TAP] ?: true,
            autoCollapseDurationSeconds = prefs[PreferencesKeys.AUTO_COLLAPSE_SEC] ?: 6,
            enableSwipeGestures = prefs[PreferencesKeys.ENABLE_SWIPE_GESTURES] ?: true,
            vibrateOnTouch = prefs[PreferencesKeys.VIBRATE_ON_TOUCH] ?: true,
            autoStartOnBoot = prefs[PreferencesKeys.AUTO_START_ON_BOOT] ?: true,
            simulationMode = prefs[PreferencesKeys.SIMULATION_MODE] ?: false,

            allowedPackages = prefs[PreferencesKeys.ALLOWED_PACKAGES] ?: emptySet()
        )
    }

    suspend fun updateSettings(update: (IslandSettings) -> IslandSettings) {
        context.dataStore.edit { prefs ->
            val currentPresetStr = prefs[PreferencesKeys.POSITION_PRESET] ?: IslandPositionPreset.TOP_LEFT.name
            val currentThemeStr = prefs[PreferencesKeys.THEME_MODE] ?: IslandThemeMode.OLED_BLACK.name
            val preset = runCatching { IslandPositionPreset.valueOf(currentPresetStr) }
                .getOrDefault(IslandPositionPreset.TOP_LEFT)
            val themeMode = runCatching { IslandThemeMode.valueOf(currentThemeStr) }
                .getOrDefault(IslandThemeMode.OLED_BLACK)

            val current = IslandSettings(
                isEnabled = prefs[PreferencesKeys.IS_ENABLED] ?: true,
                positionPreset = preset,
                offsetX = prefs[PreferencesKeys.OFFSET_X] ?: 16,
                offsetY = prefs[PreferencesKeys.OFFSET_Y] ?: 28,
                allowDragReposition = prefs[PreferencesKeys.ALLOW_DRAG_REPOSITION] ?: true,
                compactScale = prefs[PreferencesKeys.COMPACT_SCALE] ?: 1.0f,
                compactWidthDp = prefs[PreferencesKeys.COMPACT_WIDTH_DP] ?: 210,
                compactHeightDp = prefs[PreferencesKeys.COMPACT_HEIGHT_DP] ?: 42,
                expandedWidthDp = prefs[PreferencesKeys.EXPANDED_WIDTH_DP] ?: 340,
                cornerRadiusDp = prefs[PreferencesKeys.CORNER_RADIUS_DP] ?: 24,
                transparencyAlpha = prefs[PreferencesKeys.TRANSPARENCY_ALPHA] ?: 0.95f,
                themeMode = themeMode,
                textSizeMultiplier = prefs[PreferencesKeys.TEXT_SIZE_MULTIPLIER] ?: 1.0f,
                showAlbumArt = prefs[PreferencesKeys.SHOW_ALBUM_ART] ?: true,
                showArtist = prefs[PreferencesKeys.SHOW_ARTIST] ?: true,
                showTitle = prefs[PreferencesKeys.SHOW_TITLE] ?: true,
                showAlbum = prefs[PreferencesKeys.SHOW_ALBUM] ?: true,
                showProgress = prefs[PreferencesKeys.SHOW_PROGRESS] ?: true,
                showVisualizer = prefs[PreferencesKeys.SHOW_VISUALIZER] ?: true,
                showControls = prefs[PreferencesKeys.SHOW_CONTROLS] ?: true,
                expandOnTap = prefs[PreferencesKeys.EXPAND_ON_TAP] ?: true,
                autoCollapseDurationSeconds = prefs[PreferencesKeys.AUTO_COLLAPSE_SEC] ?: 6,
                enableSwipeGestures = prefs[PreferencesKeys.ENABLE_SWIPE_GESTURES] ?: true,
                vibrateOnTouch = prefs[PreferencesKeys.VIBRATE_ON_TOUCH] ?: true,
                autoStartOnBoot = prefs[PreferencesKeys.AUTO_START_ON_BOOT] ?: true,
                simulationMode = prefs[PreferencesKeys.SIMULATION_MODE] ?: false,
                allowedPackages = prefs[PreferencesKeys.ALLOWED_PACKAGES] ?: emptySet()
            )

            val updated = update(current)

            prefs[PreferencesKeys.IS_ENABLED] = updated.isEnabled
            prefs[PreferencesKeys.POSITION_PRESET] = updated.positionPreset.name
            prefs[PreferencesKeys.OFFSET_X] = updated.offsetX
            prefs[PreferencesKeys.OFFSET_Y] = updated.offsetY
            prefs[PreferencesKeys.ALLOW_DRAG_REPOSITION] = updated.allowDragReposition
            prefs[PreferencesKeys.COMPACT_SCALE] = updated.compactScale
            prefs[PreferencesKeys.COMPACT_WIDTH_DP] = updated.compactWidthDp
            prefs[PreferencesKeys.COMPACT_HEIGHT_DP] = updated.compactHeightDp
            prefs[PreferencesKeys.EXPANDED_WIDTH_DP] = updated.expandedWidthDp
            prefs[PreferencesKeys.CORNER_RADIUS_DP] = updated.cornerRadiusDp
            prefs[PreferencesKeys.TRANSPARENCY_ALPHA] = updated.transparencyAlpha
            prefs[PreferencesKeys.THEME_MODE] = updated.themeMode.name
            prefs[PreferencesKeys.TEXT_SIZE_MULTIPLIER] = updated.textSizeMultiplier
            prefs[PreferencesKeys.SHOW_ALBUM_ART] = updated.showAlbumArt
            prefs[PreferencesKeys.SHOW_ARTIST] = updated.showArtist
            prefs[PreferencesKeys.SHOW_TITLE] = updated.showTitle
            prefs[PreferencesKeys.SHOW_ALBUM] = updated.showAlbum
            prefs[PreferencesKeys.SHOW_PROGRESS] = updated.showProgress
            prefs[PreferencesKeys.SHOW_VISUALIZER] = updated.showVisualizer
            prefs[PreferencesKeys.SHOW_CONTROLS] = updated.showControls
            prefs[PreferencesKeys.EXPAND_ON_TAP] = updated.expandOnTap
            prefs[PreferencesKeys.AUTO_COLLAPSE_SEC] = updated.autoCollapseDurationSeconds
            prefs[PreferencesKeys.ENABLE_SWIPE_GESTURES] = updated.enableSwipeGestures
            prefs[PreferencesKeys.VIBRATE_ON_TOUCH] = updated.vibrateOnTouch
            prefs[PreferencesKeys.AUTO_START_ON_BOOT] = updated.autoStartOnBoot
            prefs[PreferencesKeys.SIMULATION_MODE] = updated.simulationMode
            prefs[PreferencesKeys.ALLOWED_PACKAGES] = updated.allowedPackages
        }
    }
}
