package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.KinemaxEqualizerManager
import com.example.ui.theme.KinemaxAccent
import com.example.ui.theme.KinemaxBackground
import com.example.ui.theme.KinemaxSurface
import com.example.ui.theme.KinemaxTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSettingsScreen(
    crossfadeSeconds: Int = 0,
    selectedPreset: String = "Plano",
    audioQuality: String = "Alta (320 kbps)",
    volumeNormalizationEnabled: Boolean = true,
    volumeLevel: String = "Medio",
    bassBoostStrength: Int = 0,
    virtualizerStrength: Int = 0,
    djStyle: String = "Radio FM Enérgico",
    onCrossfadeChanged: (Int) -> Unit = {},
    onPresetSelected: (String) -> Unit = {},
    onAudioQualitySelected: (String) -> Unit = {},
    onVolumeNormalizationChanged: (Boolean) -> Unit = {},
    onVolumeLevelSelected: (String) -> Unit = {},
    onBassBoostChanged: (Int) -> Unit = {},
    onVirtualizerChanged: (Int) -> Unit = {},
    onDjStyleSelected: (String) -> Unit = {},
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var crossfadeVal by remember(crossfadeSeconds) { mutableFloatStateOf(crossfadeSeconds.toFloat()) }
    var currentPreset by remember(selectedPreset) { mutableStateOf(selectedPreset) }
    var currentQuality by remember(audioQuality) { mutableStateOf(audioQuality) }
    var isNormalizationActive by remember(volumeNormalizationEnabled) { mutableStateOf(volumeNormalizationEnabled) }
    var currentVolumeLevel by remember(volumeLevel) { mutableStateOf(volumeLevel) }
    var bassVal by remember(bassBoostStrength) { mutableFloatStateOf(bassBoostStrength.toFloat()) }
    var virtualizerVal by remember(virtualizerStrength) { mutableFloatStateOf(virtualizerStrength.toFloat()) }

    val presets = listOf("Plano", "Pop", "Rock", "Bass Boost", "Vocal", "Jazz", "Clásica", "Electrónica")
    val qualities = listOf(
        QualityOption("Baja (96 kbps)", "Menor uso de datos en móviles"),
        QualityOption("Normal (160 kbps)", "Balance óptimo entre datos y sonido"),
        QualityOption("Alta (320 kbps)", "Calidad HQ para audífonos y bocinas"),
        QualityOption("Muy Alta (FLAC)", "Audio de alta fidelidad sin pérdidas")
    )
    val volumeLevels = listOf("Bajo", "Medio", "Alto")
    val freqs = remember { KinemaxEqualizerManager.getCenterFreqs() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KinemaxBackground,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de Audio y Ecualizador", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KinemaxSurface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Calidad de Audio
            Card(
                colors = CardDefaults.cardColors(containerColor = KinemaxSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HighQuality, contentDescription = null, tint = KinemaxAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Calidad de Audio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selecciona la tasa de bits preferida para la reproducción y transmisión de canciones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KinemaxTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    qualities.forEach { option ->
                        val isSelected = currentQuality.equals(option.title, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) KinemaxAccent.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) KinemaxAccent else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    currentQuality = option.title
                                    onAudioQualitySelected(option.title)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isSelected) KinemaxAccent else MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KinemaxTextSecondary
                                )
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(KinemaxAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Igualar Volumen de Canciones & Nivel de Volumen
            Card(
                colors = CardDefaults.cardColors(containerColor = KinemaxSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = KinemaxAccent)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Igualar volumen de canciones",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Normaliza el volumen automáticamente para todas las pistas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = KinemaxTextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = isNormalizationActive,
                            onCheckedChange = {
                                isNormalizationActive = it
                                onVolumeNormalizationChanged(it)
                                KinemaxEqualizerManager.setVolumeNormalization(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = KinemaxAccent
                            )
                        )
                    }

                    if (isNormalizationActive) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nivel de volumen objetivo",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ajusta la ganancia deseada para adaptarse a tu entorno de escucha.",
                            style = MaterialTheme.typography.bodySmall,
                            color = KinemaxTextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            volumeLevels.forEach { lvl ->
                                val isSelected = currentVolumeLevel.equals(lvl, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        currentVolumeLevel = lvl
                                        onVolumeLevelSelected(lvl)
                                        KinemaxEqualizerManager.setVolumeLevel(lvl)
                                    },
                                    label = {
                                        Text(
                                            text = when (lvl.lowercase()) {
                                                "bajo" -> "🔉 Bajo"
                                                "alto" -> "🔊 Alto"
                                                else -> "🎵 Medio"
                                            },
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = KinemaxAccent,
                                        selectedLabelColor = Color.Black,
                                        containerColor = KinemaxBackground,
                                        labelColor = MaterialTheme.colorScheme.onBackground
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Ecualizador de Audio
            Card(
                colors = CardDefaults.cardColors(containerColor = KinemaxSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Equalizer, contentDescription = null, tint = KinemaxAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Ecualizador de Audio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ajusta las frecuencias de sonido usando presets temáticos o de forma libre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KinemaxTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Presets de Ecualizador", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(presets) { preset ->
                            val isSelected = currentPreset.equals(preset, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    currentPreset = preset
                                    onPresetSelected(preset)
                                    KinemaxEqualizerManager.applyPreset(preset)
                                },
                                label = { Text(preset) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = KinemaxAccent,
                                    selectedLabelColor = Color.Black,
                                    containerColor = KinemaxBackground,
                                    labelColor = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Bandas de Frecuencia (dB)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    freqs.forEachIndexed { index, freq ->
                        var sliderVal by remember(currentPreset) { mutableFloatStateOf(0f) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (freq >= 1000) "${freq / 1000} kHz" else "$freq Hz",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(60.dp)
                            )
                            Slider(
                                value = sliderVal,
                                onValueChange = { valMb ->
                                    sliderVal = valMb
                                    currentPreset = "Personalizado"
                                    KinemaxEqualizerManager.setBandLevel(index, (valMb * 100).toInt())
                                },
                                valueRange = -15f..15f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = KinemaxAccent,
                                    activeTrackColor = KinemaxAccent
                                )
                            )
                            Text(
                                text = "${sliderVal.toInt()} dB",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = KinemaxTextSecondary,
                                modifier = Modifier.width(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Efectos de Sonido Adicionales", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Refuerzo de Graves (Bass Boost)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = KinemaxAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Refuerzo de Graves (Bass Boost)", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "${(bassVal / 10).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = KinemaxAccent
                            )
                        }
                        Slider(
                            value = bassVal,
                            onValueChange = {
                                bassVal = it
                                onBassBoostChanged(it.toInt())
                                KinemaxEqualizerManager.setBassBoost(it.toInt())
                            },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = KinemaxAccent,
                                activeTrackColor = KinemaxAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sonido Envolvente (Virtualizer 3D)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SurroundSound, contentDescription = null, tint = KinemaxAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Sonido Envolvente (Virtualizer 3D)", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                text = "${(virtualizerVal / 10).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = KinemaxAccent
                            )
                        }
                        Slider(
                            value = virtualizerVal,
                            onValueChange = {
                                virtualizerVal = it
                                onVirtualizerChanged(it.toInt())
                                KinemaxEqualizerManager.setVirtualizer(it.toInt())
                            },
                            valueRange = 0f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = KinemaxAccent,
                                activeTrackColor = KinemaxAccent
                            )
                        )
                    }
                }
            }

            // Section DJ Personality & Locución
            Card(
                colors = CardDefaults.cardColors(containerColor = KinemaxSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = KinemaxAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Estilo de Locución del Modo DJ IA",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Personaliza la voz, ritmo y personalidad del locutor de radio en vivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KinemaxTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val djStyles = listOf(
                        "Radio FM Enérgico",
                        "DJ de Club & Mezclas",
                        "Locutor Clásico & Seductor",
                        "Chill & Acoustic"
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        djStyles.forEach { style ->
                            val isSelected = djStyle.equals(style, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) KinemaxAccent.copy(alpha = 0.15f) else KinemaxBackground)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) KinemaxAccent else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onDjStyleSelected(style) }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = style,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSelected) KinemaxAccent else MaterialTheme.colorScheme.onBackground
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = KinemaxAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Crossfade
            Card(
                colors = CardDefaults.cardColors(containerColor = KinemaxSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = KinemaxAccent)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Crossfade (Fundido Cruzado)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Proporciona transiciones suaves sin pausas entre canciones.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KinemaxTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duración de fundido", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (crossfadeVal == 0f) "Desactivado (0s)" else "${crossfadeVal.toInt()} segundos",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = KinemaxAccent
                        )
                    }

                    Slider(
                        value = crossfadeVal,
                        onValueChange = {
                            crossfadeVal = it
                            onCrossfadeChanged(it.toInt())
                        },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = KinemaxAccent,
                            activeTrackColor = KinemaxAccent
                        )
                    )
                }
            }
        }
    }
}

private data class QualityOption(
    val title: String,
    val subtitle: String
)
