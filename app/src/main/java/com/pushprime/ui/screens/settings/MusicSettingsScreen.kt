package com.pushprime.ui.screens.settings

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pushprime.data.MusicSettingsRepository
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicSessionType
import com.pushprime.music.MusicSource
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppChoiceChip
import com.pushprime.ui.components.PremiumFadeSlideIn
import com.pushprime.ui.theme.AppSpacing
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
                title = {
                    Text(
                        text = "Music Settings",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        PremiumFadeSlideIn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                item {
                    SectionTitle("Default Music Source")
                    OptionChips(
                        options = MusicSource.values().toList(),
                        selected = musicSource,
                        label = { it.label },
                        onSelected = {
                            coroutineScope.launch { settingsRepository.updateMusicSource(it) }
                        }
                    )
                }

                item {
                    SectionTitle("Default Energy Style")
                    OptionChips(
                        options = EnergyLevel.values().toList(),
                        selected = energyLevel,
                        label = { it.label },
                        onSelected = {
                            coroutineScope.launch { settingsRepository.updateEnergyLevel(it) }
                        }
                    )
                }

                item {
                    SectionTitle("Default Session Type")
                    OptionChips(
                        options = MusicSessionType.values().toList(),
                        selected = sessionType,
                        label = { it.label },
                        onSelected = {
                            coroutineScope.launch { settingsRepository.updateSessionType(it) }
                        }
                    )
                }

                item {
                    SectionTitle("Default BPM")
                    AppCard {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${baseBpm} BPM",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.sm))
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
                    AppCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Autopilot Default",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = "Auto-switch music by workout phase",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.xxl))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = AppSpacing.xs)
    )
}

@Composable
private fun <T> OptionChips(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        options.forEach { option ->
            AppChoiceChip(
                selected = option == selected,
                onSelectedChange = { if (it) onSelected(option) },
                label = label(option)
            )
        }
    }
}
