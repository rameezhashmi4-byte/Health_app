package com.pushprime.ui.screens.music

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pushprime.data.MusicSettingsRepository
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicPhase
import com.pushprime.music.MusicPhaseLogic
import com.pushprime.music.MusicProviderManager
import com.pushprime.music.MusicSessionType
import com.pushprime.music.MusicSource
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicModeScreen(
    onNavigateBack: () -> Unit,
    onStartSession: (MusicSessionType) -> Unit,
    onConnectSpotify: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settingsRepository = remember { MusicSettingsRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    val defaultSource by settingsRepository.musicSource.collectAsState(initial = MusicSource.BASIC)
    val defaultEnergy by settingsRepository.energyLevel.collectAsState(initial = EnergyLevel.FOCUS)
    val defaultBpm by settingsRepository.baseBpm.collectAsState(initial = 130)
    val defaultAutopilot by settingsRepository.autopilotEnabled.collectAsState(initial = true)
    val defaultSessionType by settingsRepository.sessionType.collectAsState(initial = MusicSessionType.QUICK_SESSION)

    var selectedSource by remember { mutableStateOf(defaultSource) }
    var selectedEnergy by remember { mutableStateOf(defaultEnergy) }
    var selectedBpm by remember { mutableStateOf(defaultBpm) }
    var autopilotEnabled by remember { mutableStateOf(defaultAutopilot) }
    var selectedSessionType by remember { mutableStateOf(defaultSessionType) }

    LaunchedEffect(defaultSource, defaultEnergy, defaultBpm, defaultAutopilot, defaultSessionType) {
        selectedSource = defaultSource
        selectedEnergy = defaultEnergy
        selectedBpm = defaultBpm
        autopilotEnabled = defaultAutopilot
        selectedSessionType = defaultSessionType
    }

    LaunchedEffect(Unit) {
        settingsRepository.ensureDefaultMappings()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Music Mode",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                ),
                navigationIcon = {
                    Text(
                        text = "←",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onNavigateBack() },
                        style = MaterialTheme.typography.titleLarge
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
                    text = "Music Source",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MusicSource.values().forEach { source ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSource = source },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedSource == source) {
                                    PushPrimeColors.GTAYellow.copy(alpha = 0.2f)
                                } else {
                                    PushPrimeColors.Surface
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = source.label,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (source == MusicSource.BASIC) {
                                        "Run Music Mode without Spotify"
                                    } else {
                                        "Scaffold for full Spotify control"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PushPrimeColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Energy Style",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnergyLevel.values().forEach { energy ->
                        FilterChip(
                            selected = selectedEnergy == energy,
                            onClick = { selectedEnergy = energy },
                            label = { Text(energy.label) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Session Type",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MusicSessionType.values().forEach { sessionType ->
                        FilterChip(
                            selected = selectedSessionType == sessionType,
                            onClick = { selectedSessionType = sessionType },
                            label = { Text(sessionType.label) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "BPM Preference",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${selectedBpm} BPM",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = selectedBpm.toFloat(),
                            onValueChange = { selectedBpm = it.toInt() },
                            valueRange = 90f..180f
                        )
                        Text(
                            text = "Warm-up: ${selectedBpm - 20} • Main: $selectedBpm • Finisher: ${selectedBpm + 20}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PushPrimeColors.OnSurfaceVariant
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Autopilot",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Auto-switch music by workout phase",
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autopilotEnabled,
                            onCheckedChange = { autopilotEnabled = it }
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            settingsRepository.updateMusicSource(selectedSource)
                            settingsRepository.updateEnergyLevel(selectedEnergy)
                            settingsRepository.updateBaseBpm(selectedBpm)
                            settingsRepository.updateAutopilotEnabled(autopilotEnabled)
                            settingsRepository.updateSessionType(selectedSessionType)
                        }
                        MusicProviderManager.setSource(selectedSource)
                        val provider = MusicProviderManager.currentProvider.value
                        provider.connect()
                        provider.setEnergyLevel(selectedEnergy)
                        val targetBpm = MusicPhaseLogic.targetBpmForPhase(selectedBpm, MusicPhase.MAIN)
                        provider.setTargetBpm(MusicPhaseLogic.bpmRangeForTarget(targetBpm))
                        onStartSession(selectedSessionType)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.Primary)
                ) {
                    Text(
                        text = "Start Music Mode",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            if (selectedSource == MusicSource.SPOTIFY) {
                item {
                    Button(
                        onClick = onConnectSpotify,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.GTAYellow)
                    ) {
                        Text(
                            text = "Connect Spotify",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
