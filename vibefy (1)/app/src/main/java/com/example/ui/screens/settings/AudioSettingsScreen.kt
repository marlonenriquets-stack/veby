package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
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
    crossfadeSeconds: Int,
    selectedPreset: String,
    onCrossfadeChanged: (Int) -> Unit,
    onPresetSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var crossfadeVal by remember(crossfadeSeconds) { mutableFloatStateOf(crossfadeSeconds.toFloat()) }
    var currentPreset by remember(selectedPreset) { mutableStateOf(selectedPreset) }

    val presets = listOf("Plano", "Pop", "Rock", "Bass Boost", "Vocal")
    val freqs = remember { KinemaxEqualizerManager.getCenterFreqs() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = KinemaxBackground,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes de Audio", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            // Crossfade Card
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
                        text = "Permite cruzar el volumen progresivamente entre canciones durante el cambio de pista.",
                        style = MaterialTheme.typography.bodySmall,
                        color = KinemaxTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Duración", style = MaterialTheme.typography.bodyMedium)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Equalizer Card
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
                        text = "Ajusta la respuesta de frecuencia mediante los presets o de forma personalizada.",
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
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = KinemaxSurface,
                                    labelColor = MaterialTheme.colorScheme.onBackground
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Bandas de Frecuencia", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

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
                }
            }
        }
    }
}
