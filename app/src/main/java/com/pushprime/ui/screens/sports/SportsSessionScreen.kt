package com.pushprime.ui.screens.sports

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.pushprime.coach.BasicCoachProvider
import com.pushprime.coach.CoachOrchestrator
import com.pushprime.coach.CoachSettings
import com.pushprime.coach.OpenAiCoachProvider
import com.pushprime.coach.SessionPhase
import com.pushprime.coach.SessionState
import com.pushprime.coach.SessionType
import com.pushprime.coach.UserContext
import com.pushprime.coach.VoiceProviderAdapter
import com.pushprime.coach.VoiceProviderType
import com.pushprime.data.CoachSettingsRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.MusicSettingsRepository
import com.pushprime.data.OpenAiKeyStore
import com.pushprime.model.Intensity
import com.pushprime.model.SportType
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicProviderManager
import com.pushprime.music.MusicSource
import com.pushprime.ui.components.MusicModeOverlay
import com.pushprime.ui.components.CoachControlBar
import com.pushprime.ui.theme.PushPrimeColors
import com.pushprime.voice.VoiceCoachSettings
import com.pushprime.voice.VoiceProviderFactory
import com.pushprime.voice.VoiceProviderLifecycle
import com.pushprime.voice.VoiceProviderType as LegacyVoiceProviderType
import com.pushprime.voice.VoiceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsSessionScreen(
    sportType: SportType,
    localStore: LocalStore,
    onFinish: (SportType, Long, Long, Int, Intensity, Boolean, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val musicSettingsRepository = remember { MusicSettingsRepository(context) }
    val coroutineScope = rememberCoroutineScope()
    val coachSettingsRepository = remember { CoachSettingsRepository(context) }
    val coachSettings by coachSettingsRepository.settings.collectAsState(initial = CoachSettings())
    val openAiKeyStore = remember { OpenAiKeyStore(context) }
    val voiceProviderFactory = remember { VoiceProviderFactory(context, openAiKeyStore) }
    val voiceProvider = remember(coachSettings.voiceProvider) {
        val providerType = when (coachSettings.voiceProvider) {
            VoiceProviderType.OPENAI -> LegacyVoiceProviderType.AI_OPENAI
            VoiceProviderType.SYSTEM -> LegacyVoiceProviderType.SYSTEM
        }
        val baseSettings = VoiceCoachSettings(
            enabled = coachSettings.hybridEnabled,
            provider = providerType,
            voiceType = VoiceType.SYSTEM_DEFAULT
        )
        val created = voiceProviderFactory.create(baseSettings) { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        if (!created.isAvailable) {
            voiceProviderFactory.create(
                baseSettings.copy(provider = LegacyVoiceProviderType.SYSTEM)
            ) { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        } else {
            created
        }
    }
    val voiceAdapter = remember { VoiceProviderAdapter(voiceProvider) }
    val basicCoachProvider = remember { BasicCoachProvider() }
    val aiCoachProvider = remember { OpenAiCoachProvider(openAiKeyStore.getApiKey(), basicCoachProvider) }
    val coachOrchestrator = remember { CoachOrchestrator(aiCoachProvider, basicCoachProvider, voiceAdapter) }
    var coachMuted by remember { mutableStateOf(false) }
    val profile by localStore.profile.collectAsState(initial = null)
    val userContext = remember(profile) {
        UserContext(goal = profile?.goal?.name?.replace("_", " "))
    }

    val startTime = remember { System.currentTimeMillis() }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }
    var effortLevel by remember { mutableStateOf(Intensity.MEDIUM) }
    var intervalsEnabled by remember { mutableStateOf(false) }
    var warmupEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        coachOrchestrator.resetSession()
    }

    LaunchedEffect(coachSettings) {
        coachOrchestrator.updateSettings(coachSettings)
        if (!coachSettings.hybridEnabled) {
            coachOrchestrator.stop()
        }
    }

    LaunchedEffect(coachMuted) {
        voiceAdapter.setMuted(coachMuted)
    }

    LaunchedEffect(voiceProvider) {
        voiceAdapter.updateProvider(voiceProvider)
    }

    DisposableEffect(voiceProvider) {
        onDispose {
            if (voiceProvider is VoiceProviderLifecycle) {
                voiceProvider.shutdown()
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            coachOrchestrator.stop()
        }
    }

    val musicSource by musicSettingsRepository.musicSource.collectAsState(initial = MusicSource.BASIC)
    val energyLevel by musicSettingsRepository.energyLevel.collectAsState(initial = EnergyLevel.FOCUS)
    val baseBpm by musicSettingsRepository.baseBpm.collectAsState(initial = 130)
    val autopilotDefault by musicSettingsRepository.autopilotEnabled.collectAsState(initial = true)
    var autopilotEnabled by remember { mutableStateOf(autopilotDefault) }

    val musicProvider by MusicProviderManager.currentProvider.collectAsState(
        initial = MusicProviderManager.currentProvider.value
    )

    LaunchedEffect(autopilotDefault) {
        autopilotEnabled = autopilotDefault
    }

    LaunchedEffect(musicSource) {
        MusicProviderManager.setSource(musicSource)
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    LaunchedEffect(elapsedSeconds, isRunning) {
        val sessionState = SessionState(
            sessionType = SessionType.SPORT,
            phase = SessionPhase.MAIN,
            secondsElapsed = elapsedSeconds,
            secondsRemaining = Int.MAX_VALUE,
            roundNumber = 1,
            isPaused = !isRunning
        )
        coachOrchestrator.handleSessionState(sessionState, userContext)
    }

    val view = LocalView.current
    DisposableEffect(isRunning) {
        val previous = view.keepScreenOn
        if (isRunning) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = previous
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = sportType.displayName,
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
        containerColor = Color.White,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CoachControlBar(
                isCoachEnabled = coachSettings.hybridEnabled,
                isMuted = coachMuted,
                onMuteToggle = { coachMuted = !coachMuted },
                onSaySomething = {
                    coroutineScope.launch {
                        coachOrchestrator.requestManualLine(userContext)
                    }
                }
            )
            AnimatedContent(targetState = elapsedSeconds) { seconds ->
                Text(
                    text = formatTimer(seconds),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.Black
                )
            }

            Text(
                text = "Keep going 🔥",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Effort Level",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Intensity.values().forEach { level ->
                        FilterChip(
                            selected = effortLevel == level,
                            onClick = { effortLevel = level },
                            label = { Text(level.displayName) }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToggleRow(
                    title = "Intervals mode",
                    enabled = intervalsEnabled,
                    onToggle = { intervalsEnabled = it }
                )
                ToggleRow(
                    title = "Warm-up reminder",
                    enabled = warmupEnabled,
                    onToggle = { warmupEnabled = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            MusicModeOverlay(
                elapsedSeconds = elapsedSeconds,
                expectedDurationSeconds = 600,
                baseBpm = baseBpm,
                energyLevel = energyLevel,
                musicSource = musicSource,
                autopilotEnabled = autopilotEnabled,
                onAutopilotToggle = { enabled ->
                    autopilotEnabled = enabled
                    coroutineScope.launch {
                        musicSettingsRepository.updateAutopilotEnabled(enabled)
                    }
                },
                musicProvider = musicProvider,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = if (isRunning) "Pause" else "Resume",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = {
                        coachOrchestrator.stop()
                        val endTime = System.currentTimeMillis()
                        onFinish(
                            sportType,
                            startTime,
                            endTime,
                            elapsedSeconds,
                            effortLevel,
                            intervalsEnabled,
                            warmupEnabled
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Finish",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = PushPrimeColors.Surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

private fun formatTimer(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
