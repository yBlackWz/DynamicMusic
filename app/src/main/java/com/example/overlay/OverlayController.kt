package com.example.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.data.IslandPositionPreset
import com.example.data.IslandSettings
import com.example.data.IslandState
import com.example.data.MediaStateRepository
import com.example.data.MediaTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Gerencia a janela de sobreposição do sistema (WindowManager Overlay),
 * o ciclo de vida do ComposeView e o posicionamento da Ilha Dinâmica.
 */
class OverlayController(
    private val context: Context,
    private val onDragFinished: ((newX: Int, newY: Int) -> Unit)? = null
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private val lifecycleOwner = OverlayLifecycleOwner()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var stateJob: Job? = null

    private var currentSettings = IslandSettings()
    private var currentTrack: MediaTrack? = null
    private var currentIslandState: IslandState = IslandState.COMPACT

    private var isViewAdded = false

    fun start(settingsFlow: kotlinx.coroutines.flow.Flow<IslandSettings>) {
        if (composeView == null) {
            setupComposeView()
        }

        lifecycleOwner.start()

        stateJob?.cancel()
        stateJob = scope.launch {
            combine(
                settingsFlow,
                MediaStateRepository.currentTrack,
                MediaStateRepository.islandState
            ) { settings, track, islandState ->
                Triple(settings, track, islandState)
            }.collect { (settings, track, islandState) ->
                currentSettings = settings
                currentTrack = track
                currentIslandState = islandState
                updateOverlay()
            }
        }
    }

    private fun setupComposeView() {
        val view = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)

            setContent {
                DynamicIslandCapsule(
                    track = currentTrack,
                    settings = currentSettings,
                    islandState = currentIslandState,
                    onStateChange = { newState ->
                        MediaStateRepository.setIslandState(newState)
                    },
                    onDragDelta = { dx, dy ->
                        handleDragDelta(dx, dy)
                    }
                )
            }
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = calculateGravity(currentSettings.positionPreset)
            x = dpToPx(currentSettings.offsetX)
            y = dpToPx(currentSettings.offsetY)
        }

        this.composeView = view
        this.layoutParams = params
    }

    private fun updateOverlay() {
        val view = composeView ?: return
        val params = layoutParams ?: return

        val shouldShow = currentSettings.isEnabled &&
                currentTrack != null &&
                currentIslandState != IslandState.HIDDEN

        if (shouldShow) {
            params.gravity = calculateGravity(currentSettings.positionPreset)
            params.x = dpToPx(currentSettings.offsetX)
            params.y = dpToPx(currentSettings.offsetY)

            if (!isViewAdded) {
                try {
                    windowManager.addView(view, params)
                    isViewAdded = true
                } catch (e: Exception) {
                    // Trata caso de concorrência ou permissão pendente
                }
            } else {
                try {
                    view.visibility = View.VISIBLE
                    windowManager.updateViewLayout(view, params)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        } else {
            if (isViewAdded) {
                try {
                    view.visibility = View.GONE
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    private fun handleDragDelta(dx: Float, dy: Float) {
        val params = layoutParams ?: return
        val view = composeView ?: return

        params.x = (params.x + dx.toInt()).coerceAtLeast(0)
        params.y = (params.y + dy.toInt()).coerceAtLeast(0)

        try {
            windowManager.updateViewLayout(view, params)
        } catch (e: Exception) {
            // Ignore
        }

        val newXDp = pxToDp(params.x)
        val newYDp = pxToDp(params.y)
        onDragFinished?.invoke(newXDp, newYDp)
    }

    private fun calculateGravity(preset: IslandPositionPreset): Int {
        return when (preset) {
            IslandPositionPreset.TOP_LEFT -> Gravity.TOP or Gravity.START
            IslandPositionPreset.TOP_CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            IslandPositionPreset.TOP_RIGHT -> Gravity.TOP or Gravity.END
            IslandPositionPreset.CUSTOM -> Gravity.TOP or Gravity.START
        }
    }

    private fun dpToPx(dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun pxToDp(px: Int): Int {
        val density = context.resources.displayMetrics.density
        return if (density > 0) (px / density).toInt() else px
    }

    fun stop() {
        stateJob?.cancel()
        stateJob = null
        if (isViewAdded && composeView != null) {
            try {
                windowManager.removeView(composeView)
            } catch (e: Exception) {
                // Ignore
            }
            isViewAdded = false
        }
        lifecycleOwner.destroy()
        composeView = null
        layoutParams = null
    }
}
