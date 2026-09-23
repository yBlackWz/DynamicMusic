package com.example.data

/**
 * Posições pré-definidas para a cápsula flutuante da Ilha Dinâmica.
 */
enum class IslandPositionPreset {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CUSTOM
}

/**
 * Esquema de cores e estilo visual da cápsula flutuante.
 */
enum class IslandThemeMode {
    DARK,
    OLED_BLACK,
    LIGHT,
    MATERIAL_YOU,
    ALBUM_ART_TINT
}

/**
 * Estado de exibição da Ilha Dinâmica.
 */
enum class IslandState {
    HIDDEN,
    COMPACT,
    EXPANDED
}

/**
 * Modelo de preferências completo da Ilha Dinâmica salvo no DataStore.
 */
data class IslandSettings(
    // Status mestre
    val isEnabled: Boolean = true,

    // Posição
    val positionPreset: IslandPositionPreset = IslandPositionPreset.TOP_LEFT,
    val offsetX: Int = 16, // dp a partir da borda esquerda/definida
    val offsetY: Int = 28, // dp a partir do topo (abaixo da barra de status)
    val allowDragReposition: Boolean = true,

    // Dimensões e aparência
    val compactScale: Float = 1.0f,
    val compactWidthDp: Int = 210,
    val compactHeightDp: Int = 42,
    val expandedWidthDp: Int = 340,
    val cornerRadiusDp: Int = 24,
    val transparencyAlpha: Float = 0.95f,
    val themeMode: IslandThemeMode = IslandThemeMode.OLED_BLACK,
    val textSizeMultiplier: Float = 1.0f,

    // Exibição de elementos musicais
    val showAlbumArt: Boolean = true,
    val showArtist: Boolean = true,
    val showTitle: Boolean = true,
    val showAlbum: Boolean = true,
    val showProgress: Boolean = true,
    val showVisualizer: Boolean = true,
    val showControls: Boolean = true,

    // Comportamento & Gestos
    val expandOnTap: Boolean = true,
    val autoCollapseDurationSeconds: Int = 6, // 0 = desativado
    val enableSwipeGestures: Boolean = true,
    val vibrateOnTouch: Boolean = true,
    val autoStartOnBoot: Boolean = true,

    // Modo de demonstração / simulação para testes rápidos
    val simulationMode: Boolean = false,

    // Aplicativos permitidos (vazio = todos com MediaSession ativa)
    val allowedPackages: Set<String> = emptySet()
)
