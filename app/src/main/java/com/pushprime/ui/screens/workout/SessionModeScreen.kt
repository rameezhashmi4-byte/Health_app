package com.pushprime.ui.screens.workout

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.pushprime.data.SessionDao
import com.pushprime.model.ActivityType
import com.pushprime.model.Intensity
import com.pushprime.model.SessionEntity
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicProviderManager
import com.pushprime.music.MusicSource
import com.pushprime.ui.components.CoachControlBar
import com.pushprime.ui.components.MusicModeOverlay
import com.pushprime.ui.screens.common.ErrorScreen
import com.pushprime.ui.theme.PushPrimeColors
import com.pushprime.voice.VoiceCoachSettings
import com.pushprime.voice.VoiceProviderFactory
import com.pushprime.voice.VoiceProviderLifecycle
import com.pushprime.voice.VoiceProviderType as LegacyVoiceProviderType
import com.pushprime.voice.VoiceType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

private const val CountdownStart = 3
private const val WarmupSeconds = 120
private const val MainSeconds = 360
private const val FinisherSeconds = 120
private const val TotalSeconds = WarmupSeconds + MainSeconds + FinisherSeconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionModeScreen(
    localStore: LocalStore,
    sessionDao: SessionDao?,
    currentUserId: String?,
    onNavigateBack: () -> Unit,
    onNavigateHome: () -> Unit
) {
    if (sessionDao == null) {
        ErrorScreen(message = "Database not available")
        return
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val view = LocalView.current

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

    val musicSettingsRepository = remember { MusicSettingsRepository(context) }
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

    var countdownValue by rememberSaveable { mutableStateOf(CountdownStart) }
    var isCountdown by rememberSaveable { mutableStateOf(true) }
    var showGoFlash by remember { mutableStateOf(false) }
    var remainingSeconds by rememberSaveable { mutableStateOf(TotalSeconds) }
    var elapsedSeconds by rememberSaveable { mutableStateOf(0) }
    var isPaused by rememberSaveable { mutableStateOf(false) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var isCompleted by rememberSaveable { mutableStateOf(false) }
    var showEndConfirm by remember { mutableStateOf(false) }
    var beastMode by rememberSaveable { mutableStateOf(false) }
    var musicEnabled by rememberSaveable { mutableStateOf(false) }
    var savedSession by remember { mutableStateOf(false) }
    var sessionStartTime by remember { mutableStateOf<Long?>(null) }
    var completionHapticPlayed by remember { mutableStateOf(false) }

    DisposableEffect(isRunning, isCompleted) {
        val previous = view.keepScreenOn
        if (isRunning && !isCompleted) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = previous
        }
    }

    LaunchedEffect(isCountdown) {
        if (isCountdown) {
            while (countdownValue > 0 && isCountdown) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(1000)
                countdownValue -= 1
            }
            if (isCountdown) {
                showGoFlash = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(300)
                showGoFlash = false
                isCountdown = false
                isRunning = true
                sessionStartTime = System.currentTimeMillis()
            }
        }
    }

    LaunchedEffect(isRunning, isPaused, remainingSeconds, isCompleted) {
        if (isRunning && !isPaused && !isCompleted) {
            while (remainingSeconds > 0 && isRunning && !isPaused) {
                delay(1000)
                remainingSeconds -= 1
                elapsedSeconds += 1
            }
            if (remainingSeconds <= 0) {
                isCompleted = true
                isRunning = false
            }
        }
    }

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds in 1..10 && !isCompleted) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        if (remainingSeconds <= 0 && !completionHapticPlayed) {
            completionHapticPlayed = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(elapsedSeconds) {
        if (elapsedSeconds > 0 && elapsedSeconds % 60 == 0) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val phase = remember(elapsedSeconds) { resolvePhase(elapsedSeconds) }
    val phaseLabel = remember(phase) { phase.displayLabel() }
    val progress = (elapsedSeconds.toFloat() / TotalSeconds.toFloat()).coerceIn(0f, 1f)

    LaunchedEffect(phase, remainingSeconds, elapsedSeconds, isPaused, isCountdown, isCompleted) {
        if (!isCountdown && !isCompleted) {
            val sessionState = SessionState(
                sessionType = SessionType.WORKOUT,
                phase = phase,
                secondsElapsed = elapsedSeconds,
                secondsRemaining = max(remainingSeconds, 0),
                roundNumber = phase.roundNumber(),
                isPaused = isPaused
            )
            coachOrchestrator.handleSessionState(sessionState, userContext)
        }
    }

    val motivationMessages = remember {
        listOf(
            "Lock in.",
            "Stay steady.",
            "Form first.",
            "You are building.",
            "Own this minute."
        )
    }
    val beastMessages = remember {
        listOf(
            "No excuses.",
            "Push harder.",
            "Finish loud.",
            "Beast mode on.",
            "All gas."
        )
    }
    var motivationIndex by remember { mutableStateOf(0) }
    LaunchedEffect(elapsedSeconds, beastMode) {
        if (elapsedSeconds > 0 && elapsedSeconds % 45 == 0) {
            motivationIndex += 1
        }
    }
    val motivationText = if (beastMode) {
        beastMessages[motivationIndex % beastMessages.size]
    } else {
        motivationMessages[motivationIndex % motivationMessages.size]
    }

    var pulseUp by remember { mutableStateOf(true) }
    LaunchedEffect(beastMode) {
        while (true) {
            delay(if (beastMode) 650L else 1000L)
            pulseUp = !pulseUp
        }
    }
    val pulseScale by animateFloatAsState(
        targetValue = if (pulseUp) {
            if (beastMode) 1.07f else 1.03f
        } else {
            0.97f
        },
        animationSpec = tween(durationMillis = if (beastMode) 550 else 900),
        label = "pulse"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (beastMode) Color(0xFF0D0D0D) else Color.Black,
        animationSpec = tween(400),
        label = "background"
    )

    Surface(color = backgroundColor, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (isCompleted) {
                SessionCompletionContent(
                    elapsedSeconds = elapsedSeconds,
                    sessionType = "Session Mode",
                    savedSession = savedSession,
                    onSave = {
                        if (savedSession) return@SessionCompletionContent
                        val endTime = System.currentTimeMillis()
                        val startTime = sessionStartTime ?: (endTime - (elapsedSeconds * 1000L))
                        coroutineScope.launch {
                            val session = SessionEntity(
                                userId = currentUserId ?: "anonymous",
                                startTime = startTime,
                                endTime = endTime,
                                activityType = ActivityType.QUICK_SESSION.name,
                                mode = "SESSION_MODE",
                                totalSeconds = elapsedSeconds,
                                intensity = if (beastMode) Intensity.HIGH.name else Intensity.MEDIUM.name
                            )
                            sessionDao.insert(session)
                            savedSession = true
                        }
                    },
                    onNavigateHome = onNavigateHome
                )
            } else if (isCountdown) {
                CountdownContent(
                    countdownValue = countdownValue,
                    showGoFlash = showGoFlash
                )
            } else {
                LiveSessionContent(
                    phaseLabel = phaseLabel,
                    elapsedSeconds = elapsedSeconds,
                    totalSeconds = TotalSeconds,
                    progress = progress,
                    motivationText = motivationText,
                    pulseScale = pulseScale,
                    accentColor = if (beastMode) Color(0xFFFF3B30) else PushPrimeColors.Primary,
                    isPaused = isPaused,
                    isMusicEnabled = musicEnabled,
                    isCoachEnabled = !coachMuted,
                    isBeastMode = beastMode,
                    onToggleMusic = { musicEnabled = !musicEnabled },
                    onToggleCoach = { coachMuted = !coachMuted },
                    onToggleBeastMode = { beastMode = !beastMode },
                    onPauseResume = { isPaused = !isPaused },
                    onSkipPhase = {
                        when (phase) {
                            SessionPhase.WARMUP -> {
                                elapsedSeconds = WarmupSeconds
                                remainingSeconds = max(TotalSeconds - elapsedSeconds, 0)
                            }
                            SessionPhase.MAIN -> {
                                elapsedSeconds = WarmupSeconds + MainSeconds
                                remainingSeconds = max(TotalSeconds - elapsedSeconds, 0)
                            }
                            SessionPhase.FINISHER -> {
                                remainingSeconds = 0
                                isCompleted = true
                                isRunning = false
                            }
                            SessionPhase.REST -> Unit
                        }
                    },
                    onEndSession = { showEndConfirm = true },
                    onNavigateBack = onNavigateBack
                )

                CoachControlBar(
                    isCoachEnabled = coachSettings.hybridEnabled,
                    isMuted = coachMuted,
                    onMuteToggle = { coachMuted = !coachMuted },
                    onSaySomething = {
                        coroutineScope.launch {
                            coachOrchestrator.requestManualLine(userContext)
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 68.dp, start = 16.dp, end = 16.dp)
                )
            }

            if (!isCountdown && !isCompleted && musicEnabled) {
                MusicModeOverlay(
                    elapsedSeconds = elapsedSeconds,
                    expectedDurationSeconds = TotalSeconds,
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }

            if (showEndConfirm) {
                AlertDialog(
                    onDismissRequest = { showEndConfirm = false },
                    title = { Text("End session?") },
                    text = { Text("Your progress will be saved to history.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showEndConfirm = false
                                isCompleted = true
                                isRunning = false
                                coachOrchestrator.stop()
                            }
                        ) {
                            Text("End session")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEndConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CountdownContent(countdownValue: Int, showGoFlash: Boolean) {
    val label = if (countdownValue <= 0) "GO" else countdownValue.toString()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (showGoFlash) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.15f))
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "SESSION STARTS",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedContent(
                targetState = label,
                transitionSpec = {
                    (scaleIn(tween(220)) + fadeIn(tween(220))) togetherWith
                        (scaleOut(tween(150)) + fadeOut(tween(150)))
                },
                label = "countdown"
            ) { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when (countdownValue % 3) {
                    0 -> "Lock in."
                    1 -> "No excuses."
                    else -> "RAMBOOST time."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
        }
    }
}

@Composable
private fun LiveSessionContent(
    phaseLabel: String,
    elapsedSeconds: Int,
    totalSeconds: Int,
    progress: Float,
    motivationText: String,
    pulseScale: Float,
    accentColor: Color,
    isPaused: Boolean,
    isMusicEnabled: Boolean,
    isCoachEnabled: Boolean,
    isBeastMode: Boolean,
    onToggleMusic: () -> Unit,
    onToggleCoach: () -> Unit,
    onToggleBeastMode: () -> Unit,
    onPauseResume: () -> Unit,
    onSkipPhase: () -> Unit,
    onEndSession: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.Close, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = "SESSION MODE",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            IconButton(onClick = { }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedContent(
                targetState = phaseLabel,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "phase"
            ) { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.LightGray
                )
            }

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(3.dp, accentColor, CircleShape)
                )
                Text(
                    text = formatTime(totalSeconds - elapsedSeconds),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
            }

            Text(
                text = motivationText,
                style = MaterialTheme.typography.titleLarge,
                color = if (isBeastMode) accentColor else Color.White
            )

            LinearProgressIndicator(
                progress = progress,
                color = accentColor,
                trackColor = Color.DarkGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TogglePill(
                    label = "🎧 Music",
                    enabled = isMusicEnabled,
                    onToggle = onToggleMusic,
                    modifier = Modifier.weight(1f)
                )
                TogglePill(
                    label = "🎙 Coach",
                    enabled = isCoachEnabled,
                    onToggle = onToggleCoach,
                    modifier = Modifier.weight(1f)
                )
                TogglePill(
                    label = "⚡ Beast",
                    enabled = isBeastMode,
                    onToggle = onToggleBeastMode,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPauseResume,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = if (isPaused) "Resume" else "Pause",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Black
                    )
                }
                Button(
                    onClick = onEndSession,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Text(
                        text = "End session",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }

            TextButton(onClick = onSkipPhase, modifier = Modifier.fillMaxWidth()) {
                Text("Skip phase", color = Color.LightGray)
            }
        }
    }
}

@Composable
private fun SessionCompletionContent(
    elapsedSeconds: Int,
    sessionType: String,
    savedSession: Boolean,
    onSave: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "DONE 🔥",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = sessionType,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(24.dp))
            ConfettiBurst()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Duration: ${formatTime(elapsedSeconds)}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = !savedSession,
                colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.Primary)
            ) {
                Text(
                    text = if (savedSession) "Saved ✅" else "Save Session",
                    style = MaterialTheme.typography.labelLarge
                )
            }
            Button(
                onClick = onNavigateHome,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = "Back to Home",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun ConfettiBurst() {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200),
        label = "confetti_alpha"
    )
    Text(
        text = "✨ 🎉 ✨",
        modifier = Modifier.alpha(alpha),
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White
    )
}

@Composable
private fun TogglePill(
    label: String,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) PushPrimeColors.Primary else Color(0xFF2A2A2A),
        onClick = onToggle
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.White else Color.LightGray
            )
        }
    }
}

private fun resolvePhase(elapsedSeconds: Int): SessionPhase {
    return when {
        elapsedSeconds < WarmupSeconds -> SessionPhase.WARMUP
        elapsedSeconds < WarmupSeconds + MainSeconds -> SessionPhase.MAIN
        else -> SessionPhase.FINISHER
    }
}

private fun SessionPhase.displayLabel(): String {
    return when (this) {
        SessionPhase.WARMUP -> "Warm-up"
        SessionPhase.MAIN -> "Main"
        SessionPhase.FINISHER -> "Finisher"
        SessionPhase.REST -> "Rest"
    }
}

private fun SessionPhase.roundNumber(): Int {
    return when (this) {
        SessionPhase.WARMUP -> 1
        SessionPhase.MAIN -> 2
        SessionPhase.FINISHER -> 3
        SessionPhase.REST -> 0
    }
}

private fun formatTime(seconds: Int): String {
    val safeSeconds = max(seconds, 0)
    val mins = safeSeconds / 60
    val secs = safeSeconds % 60
    return String.format("%02d:%02d", mins, secs)
}
