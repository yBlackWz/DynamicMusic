package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DynamicIslandApplication
import com.example.data.IslandPositionPreset
import com.example.data.IslandSettings
import com.example.data.IslandThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as DynamicIslandApplication
    val settingsRepo = app.settingsRepository
    val settings by settingsRepo.settingsFlow.collectAsState(initial = IslandSettings())
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Aparência", "Posição", "Música", "Comportamento")

    fun updateSettings(block: (IslandSettings) -> IslandSettings) {
        scope.launch {
            settingsRepo.updateSettings(block)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalização", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> { // Aparência
                        item {
                            AppearanceSection(
                                settings = settings,
                                onUpdate = ::updateSettings
                            )
                        }
                    }
                    1 -> { // Posição
                        item {
                            PositionSection(
                                settings = settings,
                                onUpdate = ::updateSettings
                            )
                        }
                    }
                    2 -> { // Música
                        item {
                            MusicSection(
                                settings = settings,
                                onUpdate = ::updateSettings
                            )
                        }
                    }
                    3 -> { // Comportamento
                        item {
                            BehaviorSection(
                                settings = settings,
                                onUpdate = ::updateSettings
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun AppearanceSection(
    settings: IslandSettings,
    onUpdate: ((IslandSettings) -> IslandSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Estilo do Tema
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tema e Cores da Cápsula", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeChip(
                        label = "OLED Preto",
                        isSelected = settings.themeMode == IslandThemeMode.OLED_BLACK,
                        onClick = { onUpdate { it.copy(themeMode = IslandThemeMode.OLED_BLACK) } }
                    )
                    ThemeChip(
                        label = "Escuro",
                        isSelected = settings.themeMode == IslandThemeMode.DARK,
                        onClick = { onUpdate { it.copy(themeMode = IslandThemeMode.DARK) } }
                    )
                    ThemeChip(
                        label = "Claro",
                        isSelected = settings.themeMode == IslandThemeMode.LIGHT,
                        onClick = { onUpdate { it.copy(themeMode = IslandThemeMode.LIGHT) } }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeChip(
                        label = "Material You",
                        isSelected = settings.themeMode == IslandThemeMode.MATERIAL_YOU,
                        onClick = { onUpdate { it.copy(themeMode = IslandThemeMode.MATERIAL_YOU) } }
                    )
                    ThemeChip(
                        label = "Cor da Capa",
                        isSelected = settings.themeMode == IslandThemeMode.ALBUM_ART_TINT,
                        onClick = { onUpdate { it.copy(themeMode = IslandThemeMode.ALBUM_ART_TINT) } }
                    )
                }
            }
        }

        // Sliders de Tamanho e Escala
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dimensões da Cápsula", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(12.dp))
                SliderWithLabel(
                    label = "Escala da Cápsula Compacta: ${(settings.compactScale * 100).toInt()}%",
                    value = settings.compactScale,
                    valueRange = 0.8f..1.3f,
                    onValueChange = { scale -> onUpdate { it.copy(compactScale = scale) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SliderWithLabel(
                    label = "Largura Compacta: ${settings.compactWidthDp} dp",
                    value = settings.compactWidthDp.toFloat(),
                    valueRange = 160f..260f,
                    onValueChange = { w -> onUpdate { it.copy(compactWidthDp = w.toInt()) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SliderWithLabel(
                    label = "Largura Expandida: ${settings.expandedWidthDp} dp",
                    value = settings.expandedWidthDp.toFloat(),
                    valueRange = 290f..370f,
                    onValueChange = { w -> onUpdate { it.copy(expandedWidthDp = w.toInt()) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SliderWithLabel(
                    label = "Arredondamento das Bordas: ${settings.cornerRadiusDp} dp",
                    value = settings.cornerRadiusDp.toFloat(),
                    valueRange = 14f..32f,
                    onValueChange = { r -> onUpdate { it.copy(cornerRadiusDp = r.toInt()) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SliderWithLabel(
                    label = "Opacidade / Transparência: ${(settings.transparencyAlpha * 100).toInt()}%",
                    value = settings.transparencyAlpha,
                    valueRange = 0.65f..1.0f,
                    onValueChange = { a -> onUpdate { it.copy(transparencyAlpha = a) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SliderWithLabel(
                    label = "Tamanho dos Textos: ${(settings.textSizeMultiplier * 100).toInt()}%",
                    value = settings.textSizeMultiplier,
                    valueRange = 0.8f..1.3f,
                    onValueChange = { s -> onUpdate { it.copy(textSizeMultiplier = s) } }
                )
            }
        }
    }
}

@Composable
private fun PositionSection(
    settings: IslandSettings,
    onUpdate: ((IslandSettings) -> IslandSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Presets de posição
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Posicionamento na Tela", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeChip(
                        label = "Canto Esquerdo",
                        isSelected = settings.positionPreset == IslandPositionPreset.TOP_LEFT,
                        onClick = { onUpdate { it.copy(positionPreset = IslandPositionPreset.TOP_LEFT, offsetX = 16) } }
                    )
                    ThemeChip(
                        label = "Centro",
                        isSelected = settings.positionPreset == IslandPositionPreset.TOP_CENTER,
                        onClick = { onUpdate { it.copy(positionPreset = IslandPositionPreset.TOP_CENTER, offsetX = 0) } }
                    )
                    ThemeChip(
                        label = "Canto Direito",
                        isSelected = settings.positionPreset == IslandPositionPreset.TOP_RIGHT,
                        onClick = { onUpdate { it.copy(positionPreset = IslandPositionPreset.TOP_RIGHT, offsetX = 16) } }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ThemeChip(
                    label = "Posição Personalizada Manual",
                    isSelected = settings.positionPreset == IslandPositionPreset.CUSTOM,
                    onClick = { onUpdate { it.copy(positionPreset = IslandPositionPreset.CUSTOM) } }
                )
            }
        }

        // Sliders X e Y
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ajuste Fino de Posição", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(12.dp))
                SliderWithLabel(
                    label = "Distância da Borda (X): ${settings.offsetX} dp",
                    value = settings.offsetX.toFloat(),
                    valueRange = 0f..180f,
                    onValueChange = { x -> onUpdate { it.copy(offsetX = x.toInt()) } }
                )

                Spacer(modifier = Modifier.height(10.dp))
                SliderWithLabel(
                    label = "Distância do Topo (Y): ${settings.offsetY} dp",
                    value = settings.offsetY.toFloat(),
                    valueRange = 0f..120f,
                    onValueChange = { y -> onUpdate { it.copy(offsetY = y.toInt()) } }
                )
            }
        }

        // Permitir reposicionar arrastando
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reposicionar ao Arrastar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        "Permite arrastar a cápsula diretamente na tela para a posição que você preferir.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.allowDragReposition,
                    onCheckedChange = { checked -> onUpdate { it.copy(allowDragReposition = checked) } }
                )
            }
        }
    }
}

@Composable
private fun MusicSection(
    settings: IslandSettings,
    onUpdate: ((IslandSettings) -> IslandSettings) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Elementos Visíveis da Música", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))

            SettingSwitchRow("Mostrar capa do álbum", settings.showAlbumArt) {
                onUpdate { s -> s.copy(showAlbumArt = it) }
            }
            SettingSwitchRow("Mostrar título da música", settings.showTitle) {
                onUpdate { s -> s.copy(showTitle = it) }
            }
            SettingSwitchRow("Mostrar nome do artista", settings.showArtist) {
                onUpdate { s -> s.copy(showArtist = it) }
            }
            SettingSwitchRow("Mostrar nome do álbum no expandido", settings.showAlbum) {
                onUpdate { s -> s.copy(showAlbum = it) }
            }
            SettingSwitchRow("Mostrar barra de progresso e tempo", settings.showProgress) {
                onUpdate { s -> s.copy(showProgress = it) }
            }
            SettingSwitchRow("Mostrar visualizador de áudio animado", settings.showVisualizer) {
                onUpdate { s -> s.copy(showVisualizer = it) }
            }
            SettingSwitchRow("Mostrar botões de controle de mídia", settings.showControls) {
                onUpdate { s -> s.copy(showControls = it) }
            }
        }
    }
}

@Composable
private fun BehaviorSection(
    settings: IslandSettings,
    onUpdate: ((IslandSettings) -> IslandSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Interações e Gestos", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitchRow("Expandir e recolher ao tocar", settings.expandOnTap) {
                    onUpdate { s -> s.copy(expandOnTap = it) }
                }

                SettingSwitchRow("Gestos de deslize (swipe para trocar)", settings.enableSwipeGestures) {
                    onUpdate { s -> s.copy(enableSwipeGestures = it) }
                }

                SettingSwitchRow("Feedback tátil (vibração)", settings.vibrateOnTouch) {
                    onUpdate { s -> s.copy(vibrateOnTouch = it) }
                }

                SettingSwitchRow("Iniciar ao ligar o telefone", settings.autoStartOnBoot) {
                    onUpdate { s -> s.copy(autoStartOnBoot = it) }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Recolhimento Automático", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                val sec = settings.autoCollapseDurationSeconds
                val label = if (sec == 0) "Desativado (não recolhe sozinho)" else "Recolher após $sec segundos"

                SliderWithLabel(
                    label = label,
                    value = sec.toFloat(),
                    valueRange = 0f..15f,
                    onValueChange = { v -> onUpdate { it.copy(autoCollapseDurationSeconds = v.toInt()) } }
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
private fun ThemeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
