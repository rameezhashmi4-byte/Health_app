package com.pushprime.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.MusicSettingsRepository
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicSessionType
import com.pushprime.music.MusicSource
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settingsRepository = remember { MusicSettingsRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val musicSource by settingsRepository.musicSource.collectAsState(initial = MusicSource.BASIC)
    val energyLevel by settingsRepository.energyLevel.collectAsState(initial = EnergyLevel.FOCUS)
    val baseBpm by settingsRepository.baseBpm.collectAsState(initial = 130)
    val autopilotEnabled by settingsRepository.autopilotEnabled.collectAsState(initial = true)
    val sessionType by settingsRepository.sessionType.collectAsState(initial = MusicSessionType.QUICK_SESSION)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Music Settings", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PushPrimeColors.Surface),
                navigationIcon = {
                    Text(
                        text = "←",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onNavigateBack() },
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Default Music Source",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MusicSource.values().forEach { source ->
                        FilterChip(
                            selected = musicSource == source,
                            onClick = {
                                coroutineScope.launch {
                                    settingsRepository.updateMusicSource(source)
                                }
                            },
                            label = { Text(source.label) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Default Energy Style",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnergyLevel.values().forEach { energy ->
                        FilterChip(
                            selected = energyLevel == energy,
                            onClick = {
                                coroutineScope.launch {
                                    settingsRepository.updateEnergyLevel(energy)
                                }
                            },
                            label = { Text(energy.label) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Default Session Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MusicSessionType.values().forEach { type ->
                        FilterChip(
                            selected = sessionType == type,
                            onClick = {
                                coroutineScope.launch {
                                    settingsRepository.updateSessionType(type)
                                }
                            },
                            label = { Text(type.label) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Default BPM",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${baseBpm} BPM",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = baseBpm.toFloat(),
                            onValueChange = { value ->
                                coroutineScope.launch {
                                    settingsRepository.updateBaseBpm(value.toInt())
                                }
                            },
                            valueRange = 90f..180f
                        )
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Autopilot default",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Auto-switch music by workout phase",
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autopilotEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    settingsRepository.updateAutopilotEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
