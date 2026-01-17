package com.pushprime.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pushprime.data.OpenAiKeyStore
import com.pushprime.data.VoiceCoachSettingsRepository
import com.pushprime.ui.components.InfoCard
import com.pushprime.ui.theme.PushPrimeColors
import com.pushprime.voice.CoachFrequency
import com.pushprime.voice.CoachPersonality
import com.pushprime.voice.VoiceCoachPhrases
import com.pushprime.voice.VoiceCoachSettings
import com.pushprime.voice.VoiceProviderFactory
import com.pushprime.voice.VoiceProviderLifecycle
import com.pushprime.voice.VoiceProviderType
import com.pushprime.voice.VoiceType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCoachSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { VoiceCoachSettingsRepository(context) }
    val keyStore = remember { OpenAiKeyStore(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val settings by repository.settings.collectAsState(initial = VoiceCoachSettings())
    var apiKeyInput by remember { mutableStateOf("") }
    var hasSavedKey by remember { mutableStateOf(!keyStore.getApiKey().isNullOrBlank()) }

    val onUnavailable: (String) -> Unit = { message ->
        scope.launch {
            repository.updateEnabled(false)
            snackbarHostState.showSnackbar(message)
        }
    }
    val providerFactory = remember { VoiceProviderFactory(context, keyStore) }
    val voiceProvider = remember(settings.provider, settings.voiceType) {
        providerFactory.create(settings, onUnavailable)
    }

    LaunchedEffect(settings) {
        voiceProvider.setStyle(settings.personality)
        voiceProvider.setIntensity(settings.frequency.toIntensity())
        if (voiceProvider is com.pushprime.voice.VoiceTypeSupport) {
            voiceProvider.setVoiceType(settings.voiceType)
        }
        if (!settings.enabled) {
            voiceProvider.stop()
        }
    }

    DisposableEffect(voiceProvider) {
        onDispose {
            if (voiceProvider is VoiceProviderLifecycle) {
                voiceProvider.shutdown()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voice Coach",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Voice Coach",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Motivation and cues during workouts",
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.enabled,
                            onCheckedChange = {
                                scope.launch { repository.updateEnabled(it) }
                            }
                        )
                    }
                }
            }

            item {
                SectionTitle("Coach Personality")
                OptionChips(
                    options = CoachPersonality.values().toList(),
                    selected = settings.personality,
                    label = {
                        when (it) {
                            CoachPersonality.CALM -> "Calm"
                            CoachPersonality.HYPE -> "Hype"
                            CoachPersonality.MILITARY -> "Military"
                            CoachPersonality.FRIENDLY -> "Friendly"
                        }
                    },
                    onSelected = { scope.launch { repository.updatePersonality(it) } }
                )
            }

            item {
                SectionTitle("Frequency")
                OptionChips(
                    options = CoachFrequency.values().toList(),
                    selected = settings.frequency,
                    label = {
                        when (it) {
                            CoachFrequency.LOW -> "Low (2 min)"
                            CoachFrequency.MEDIUM -> "Medium (60s)"
                            CoachFrequency.HIGH -> "High (30s)"
                        }
                    },
                    onSelected = { scope.launch { repository.updateFrequency(it) } }
                )
            }

            item {
                SectionTitle("Voice Type")
                OptionChips(
                    options = VoiceType.values().toList(),
                    selected = settings.voiceType,
                    label = {
                        when (it) {
                            VoiceType.SYSTEM_DEFAULT -> "System default"
                            VoiceType.MALE -> "Male"
                            VoiceType.FEMALE -> "Female"
                        }
                    },
                    onSelected = { scope.launch { repository.updateVoiceType(it) } }
                )
                Text(
                    text = "Male/Female voices depend on device availability.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Speak during rest only",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Keep cues in rest periods",
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.speakDuringRestOnly,
                            onCheckedChange = {
                                scope.launch { repository.updateRestOnly(it) }
                            }
                        )
                    }
                }
            }

            item {
                SectionTitle("Voice Provider")
                OptionChips(
                    options = VoiceProviderType.values().toList(),
                    selected = settings.provider,
                    label = {
                        when (it) {
                            VoiceProviderType.SYSTEM -> "System Voice (Offline)"
                            VoiceProviderType.AI_OPENAI -> "AI Voice (OpenAI)"
                        }
                    },
                    onSelected = { scope.launch { repository.updateProvider(it) } }
                )
            }

            item {
                InfoCard(
                    icon = Icons.Default.Info,
                    title = "AI Voice Notes",
                    description = "ChatGPT Plus login can’t be used inside RAMBOOST. " +
                        "Use your own OpenAI API key now, or add a backend later for secure token minting. " +
                        "Realtime API is the best fit for advanced voice."
                )
            }

            if (settings.provider == VoiceProviderType.AI_OPENAI) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "OpenAI API Key",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(if (hasSavedKey) "Saved \u2022\u2022\u2022\u2022" else "sk-...") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        val trimmed = apiKeyInput.trim()
                                        if (trimmed.length < 10) {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Enter a valid OpenAI API key.")
                                            }
                                            return@Button
                                        }
                                        val result = keyStore.saveApiKey(trimmed)
                                        if (result.isSuccess) {
                                            apiKeyInput = ""
                                            hasSavedKey = true
                                            scope.launch {
                                                snackbarHostState.showSnackbar("API key saved.")
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Failed to save key.")
                                            }
                                        }
                                    }
                                ) {
                                    Text("Verify & Save")
                                }
                                if (hasSavedKey) {
                                    TextButton(
                                        onClick = {
                                            keyStore.clearApiKey()
                                            hasSavedKey = false
                                            scope.launch {
                                                snackbarHostState.showSnackbar("API key cleared.")
                                            }
                                        }
                                    ) {
                                        Text("Clear")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        if (!voiceProvider.isAvailable) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Voice engine not ready yet.")
                            }
                            return@Button
                        }
                        voiceProvider.speak(
                            VoiceCoachPhrases.sample(settings.personality),
                            queueIfBusy = false
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Play sample")
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { Text(label(option)) }
            )
        }
    }
}

private fun CoachFrequency.toIntensity(): com.pushprime.voice.CoachIntensity {
    return when (this) {
        CoachFrequency.LOW -> com.pushprime.voice.CoachIntensity.LOW
        CoachFrequency.MEDIUM -> com.pushprime.voice.CoachIntensity.MEDIUM
        CoachFrequency.HIGH -> com.pushprime.voice.CoachIntensity.HIGH
    }
}
