package com.example.overlay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.MainActivity
import com.example.data.IslandSettings
import com.example.data.IslandState
import com.example.data.MediaStateRepository
import com.example.data.MediaTrack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Cápsula principal que alterna suavemente entre modo Compacto e Expandido,
 * gerenciando todos os gestos (toque, swipe, long press, arrastar).
 */
@Composable
fun DynamicIslandCapsule(
    track: MediaTrack?,
    settings: IslandSettings,
    islandState: IslandState,
    onStateChange: (IslandState) -> Unit,
    onDragDelta: (dx: Float, dy: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null || islandState == IslandState.HIDDEN || !settings.isEnabled) {
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Temporizador para auto-recolher após X segundos no modo expandido
    LaunchedEffect(islandState, track.lastUpdateEpoch) {
        if (islandState == IslandState.EXPANDED && settings.autoCollapseDurationSeconds > 0) {
            delay(settings.autoCollapseDurationSeconds * 1000L)
            onStateChange(IslandState.COMPACT)
        }
    }

    fun triggerVibration() {
        if (!settings.vibrateOnTouch) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (e: Exception) {
            // Ignora se não houver permissão ou suporte
        }
    }

    fun openSettingsScreen() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("route", "settings")
        }
        context.startActivity(intent)
    }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .wrapContentSize()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .pointerInput(settings, islandState) {
                // Detecção de gestos de toque, toque longo e swipes
                detectTapGestures(
                    onTap = {
                        triggerVibration()
                        if (settings.expandOnTap) {
                            if (islandState == IslandState.COMPACT) {
                                onStateChange(IslandState.EXPANDED)
                            } else {
                                onStateChange(IslandState.COMPACT)
                            }
                        }
                    },
                    onLongPress = {
                        triggerVibration()
                        openSettingsScreen()
                    }
                )
            }
            .pointerInput(settings, islandState) {
                // Detecção de arraste e gestos de deslize
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        // Verifica gestos de deslize (swipe)
                        if (settings.enableSwipeGestures) {
                            val threshold = 70f
                            if (abs(totalDragX) > abs(totalDragY) && abs(totalDragX) > threshold) {
                                triggerVibration()
                                if (totalDragX > 0) {
                                    MediaStateRepository.onSkipNext()
                                } else {
                                    MediaStateRepository.onSkipPrevious()
                                }
                            } else if (totalDragY < -threshold) {
                                // Deslizar para cima: ocultar temporariamente
                                triggerVibration()
                                onStateChange(IslandState.HIDDEN)
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y

                        if (settings.allowDragReposition) {
                            onDragDelta(dragAmount.x, dragAmount.y)
                        }
                    }
                )
            }
    ) {
        AnimatedContent(
            targetState = islandState,
            transitionSpec = {
                fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)) togetherWith
                        fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
            },
            label = "island_mode"
        ) { mode ->
            when (mode) {
                IslandState.COMPACT -> {
                    DynamicIslandCompactView(
                        track = track,
                        settings = settings,
                        modifier = Modifier
                            .widthIn(min = (settings.compactWidthDp * settings.compactScale).dp)
                            .wrapContentHeight()
                    )
                }
                IslandState.EXPANDED -> {
                    DynamicIslandExpandedView(
                        track = track,
                        settings = settings,
                        onCollapse = {
                            triggerVibration()
                            onStateChange(IslandState.COMPACT)
                        },
                        modifier = Modifier
                            .width(settings.expandedWidthDp.dp)
                            .wrapContentHeight()
                    )
                }
                IslandState.HIDDEN -> {
                    // Invisível
                }
            }
        }
    }
}
