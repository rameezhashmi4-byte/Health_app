package com.pushprime.ui.screens.workout

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.pushprime.data.AnalyticsHelper
import com.pushprime.data.AppDatabase
import com.pushprime.data.CoachSettingsRepository
import com.pushprime.data.LocalStore
import com.pushprime.data.MusicSettingsRepository
import com.pushprime.data.OpenAiKeyStore
import com.pushprime.model.ActivityType
import com.pushprime.model.Intensity
import com.pushprime.model.SessionEntity
import com.pushprime.music.EnergyLevel
import com.pushprime.music.MusicPhase
import com.pushprime.music.MusicProviderManager
import com.pushprime.music.MusicSource
import com.pushprime.ui.components.CoachControlBar
import com.pushprime.ui.components.WorkoutMusicBar
import com.pushprime.ui.components.MusicModeOverlay
import com.pushprime.ui.theme.PushPrimeColors
import com.pushprime.voice.VoiceCoachSettings
import com.pushprime.voice.VoiceProviderFactory
import com.pushprime.voice.VoiceProviderLifecycle
import com.pushprime.voice.VoiceProviderType as LegacyVoiceProviderType
import com.pushprime.voice.VoiceType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Workout Player Screen
 * State machine: COUNTDOWN -> ACTIVE -> REST -> COMPLETE
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPlayerScreen(
    sessionId: Long?,
    localStore: LocalStore,
    currentUserId: String?,
    spotifyHelper: com.pushprime.data.SpotifyHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember {
        try {
            AppDatabase.getDatabase(context)
        } catch (_: Exception) {
            null
        }
    }
    val sessionDao = remember(database) { database?.sessionDao() }
    val coroutineScope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val analyticsHelper = remember { AnalyticsHelper(context) }
    val musicSettingsRepository = remember { MusicSettingsRepository(context) }
    val achievementsViewModel: WorkoutAchievementsViewModel = hiltViewModel()
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

    val sessions by (sessionDao?.getAllSessions() ?: flowOf(emptyList()))
        .collectAsState(initial = emptyList())

    val workoutPlan = remember {
        listOf(
            WorkoutExercise(
                name = "Push-ups",
                targetReps = 18,
                targetSeconds = 40,
                restSeconds = 20,
                cue = "Keep core tight"
            ),
            WorkoutExercise(
                name = "Squats",
                targetReps = 20,
                targetSeconds = 45,
                restSeconds = 20,
                cue = "Drive through heels"
            ),
            WorkoutExercise(
                name = "Plank",
                targetReps = null,
                targetSeconds = 35,
                restSeconds = 20,
                cue = "Flat back, steady breath"
            ),
            WorkoutExercise(
                name = "Mountain Climbers",
                targetReps = 24,
                targetSeconds = 35,
                restSeconds = 0,
                cue = "Light on your feet"
            )
        )
    }

    val expectedDurationSeconds = remember(workoutPlan) {
        workoutPlan.sumOf { it.targetSeconds + it.restSeconds }.coerceAtLeast(1)
    }

    var phase by remember { mutableStateOf(WorkoutPhase.COUNTDOWN) }
    var exerciseIndex by remember { mutableStateOf(0) }
    var countdownSeconds by remember { mutableStateOf(3) }
    var activeSecondsRemaining by remember { mutableStateOf(workoutPlan.first().targetSeconds) }
    var activeSecondsElapsed by remember { mutableStateOf(0) }
    var restSecondsRemaining by remember { mutableStateOf(workoutPlan.first().restSeconds) }
    var activeReps by remember { mutableStateOf(0) }
    var totalReps by remember { mutableStateOf(0) }
    var workoutElapsedSeconds by remember { mutableStateOf(0) }
    var completedExercises by remember { mutableStateOf<List<CompletedExercise>>(emptyList()) }
    var isPaused by remember { mutableStateOf(false) }
    var sessionStartTime by remember { mutableStateOf<Long?>(null) }

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

    val personalBest = remember(sessions) {
        sessions.maxOfOrNull { it.totalReps ?: 0 } ?: 0
    }
    val isPersonalBest = totalReps > 0 && totalReps > personalBest

    val currentExercise = workoutPlan.getOrNull(exerciseIndex)
    val nextExercise = workoutPlan.getOrNull(exerciseIndex + 1)
    val totalPlannedSeconds = remember(workoutPlan) {
        workoutPlan.sumOf { it.targetSeconds + it.restSeconds }
    }

    fun resetForExercise(index: Int) {
        val exercise = workoutPlan.getOrNull(index) ?: return
        activeSecondsRemaining = exercise.targetSeconds
        activeSecondsElapsed = 0
        activeReps = 0
        isPaused = false
    }

    fun moveToNextExercise() {
        if (exerciseIndex >= workoutPlan.lastIndex) {
            phase = WorkoutPhase.COMPLETE
            return
        }
        exerciseIndex += 1
        resetForExercise(exerciseIndex)
        phase = WorkoutPhase.ACTIVE
    }

    fun completeExercise(skipped: Boolean) {
        val exercise = currentExercise ?: return
        val elapsedSeconds = activeSecondsElapsed.coerceAtMost(exercise.targetSeconds)
        val repsLogged = if (skipped) 0 else activeReps

        completedExercises = completedExercises + CompletedExercise(
            name = exercise.name,
            reps = repsLogged,
            seconds = elapsedSeconds
        )
        totalReps += repsLogged

        if (exerciseIndex >= workoutPlan.lastIndex) {
            phase = WorkoutPhase.COMPLETE
            return
        }

        restSecondsRemaining = exercise.restSeconds
        phase = if (restSecondsRemaining > 0) {
            WorkoutPhase.REST
        } else {
            moveToNextExercise()
            WorkoutPhase.ACTIVE
        }
    }

    fun stopSession() {
        val endTime = System.currentTimeMillis()
        val startTime = sessionStartTime ?: endTime
        val duration = workoutElapsedSeconds.takeIf { it > 0 } ?: ((endTime - startTime) / 1000).toInt()

        analyticsHelper.trackEvent(
            AnalyticsHelper.Events.WORKOUT_COMPLETED,
            mapOf(
                AnalyticsHelper.Params.PUSHUPS_COUNT to totalReps,
                AnalyticsHelper.Params.DURATION to duration
            )
        )

        if (sessionDao != null && (totalReps > 0 || workoutElapsedSeconds > 0)) {
            coroutineScope.launch {
                val session = SessionEntity(
                    userId = currentUserId ?: "anonymous",
                    startTime = startTime,
                    endTime = endTime,
                    activityType = ActivityType.GYM.name,
                    mode = "GUIDED",
                    totalReps = totalReps.takeIf { it > 0 },
                    totalSeconds = duration,
                    intensity = Intensity.MEDIUM.name
                )
                sessionDao.insert(session)
                localStore.recordSessionDate(session.date)
                achievementsViewModel.onSessionSaved()
            }
        }

        onNavigateBack()
    }

    LaunchedEffect(Unit) {
        sessionStartTime = System.currentTimeMillis()
        analyticsHelper.trackEvent(
            AnalyticsHelper.Events.WORKOUT_STARTED,
            mapOf(AnalyticsHelper.Params.WORKOUT_TYPE to "GUIDED")
        )
    }

    LaunchedEffect(phase) {
        when (phase) {
            WorkoutPhase.COUNTDOWN -> {
                countdownSeconds = 3
                while (countdownSeconds > 0 && phase == WorkoutPhase.COUNTDOWN) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    delay(1000)
                    countdownSeconds -= 1
                }
                if (phase == WorkoutPhase.COUNTDOWN) {
                    resetForExercise(exerciseIndex)
                    phase = WorkoutPhase.ACTIVE
                }
            }
            WorkoutPhase.ACTIVE -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            WorkoutPhase.REST -> haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            WorkoutPhase.COMPLETE -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(
        phase,
        isPaused,
        exerciseIndex,
        activeSecondsRemaining,
        restSecondsRemaining,
        countdownSeconds,
        workoutElapsedSeconds
    ) {
        if (phase == WorkoutPhase.COUNTDOWN) return@LaunchedEffect
        val sessionRemaining = (totalPlannedSeconds - workoutElapsedSeconds).coerceAtLeast(0)
        val sessionState = SessionState(
            sessionType = SessionType.QUICK,
            phase = when (phase) {
                WorkoutPhase.COUNTDOWN -> SessionPhase.WARMUP
                WorkoutPhase.ACTIVE -> SessionPhase.MAIN
                WorkoutPhase.REST -> SessionPhase.REST
                WorkoutPhase.COMPLETE -> SessionPhase.FINISHER
            },
            secondsElapsed = workoutElapsedSeconds,
            secondsRemaining = sessionRemaining,
            roundNumber = exerciseIndex + 1,
            isPaused = isPaused
        )
        coachOrchestrator.handleSessionState(sessionState, userContext)
    }

    LaunchedEffect(phase, isPaused, activeSecondsRemaining, exerciseIndex) {
        if (phase == WorkoutPhase.ACTIVE && !isPaused) {
            while (activeSecondsRemaining > 0 && phase == WorkoutPhase.ACTIVE && !isPaused) {
                delay(1000)
                activeSecondsRemaining -= 1
                activeSecondsElapsed += 1
                workoutElapsedSeconds += 1
            }
            if (phase == WorkoutPhase.ACTIVE && activeSecondsRemaining <= 0) {
                completeExercise(skipped = false)
            }
        }
    }

    LaunchedEffect(phase, restSecondsRemaining) {
        if (phase == WorkoutPhase.REST) {
            while (restSecondsRemaining > 0 && phase == WorkoutPhase.REST) {
                delay(1000)
                restSecondsRemaining -= 1
                workoutElapsedSeconds += 1
            }
            if (phase == WorkoutPhase.REST) {
                moveToNextExercise()
            }
        }
    }

    // Spotify state
    val isSpotifyConnected = if (spotifyHelper != null) {
        spotifyHelper.isConnected.collectAsState(initial = false).value
    } else {
        remember { mutableStateOf(false) }.value
    }

    val spotifyCurrentTrack = if (spotifyHelper != null) {
        spotifyHelper.currentTrack.collectAsState(initial = null).value
    } else {
        remember { mutableStateOf<com.pushprime.data.Track?>(null) }.value
    }

    val isMusicPlaying = if (spotifyHelper != null) {
        spotifyHelper.isPlaying.collectAsState(initial = false).value
    } else {
        remember { mutableStateOf(false) }.value
    }

    val currentTrack = remember(spotifyCurrentTrack) {
        spotifyCurrentTrack?.name ?: "No track"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Workout",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${exerciseIndex + 1} of ${workoutPlan.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { stopSession() }) {
                        Icon(Icons.Default.Close, contentDescription = "End Workout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        bottomBar = {
            if (isSpotifyConnected) {
                WorkoutMusicBar(
                    isPlaying = isMusicPlaying,
                    currentTrack = currentTrack,
                    onPlayPause = {
                        if (isMusicPlaying) spotifyHelper?.pause() else spotifyHelper?.resume()
                    },
                    onNext = { spotifyHelper?.skipNext() },
                    onPrevious = { spotifyHelper?.skipPrevious() },
                    onPresetSelected = { preset ->
                        analyticsHelper.trackEvent(
                            AnalyticsHelper.Events.ENERGY_PRESET_CHANGED,
                            mapOf(AnalyticsHelper.Params.PRESET_NAME to preset)
                        )
                    }
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Connect Spotify for better beats", color = Color.White)
                        TextButton(onClick = { /* Spotify login */ }) {
                            Text(
                                "CONNECT",
                                color = PushPrimeColors.GTAYellow,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PushPrimeColors.Background)
        ) {
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
                    .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                label = "workout_phase"
            ) { currentPhase ->
                when (currentPhase) {
                    WorkoutPhase.COUNTDOWN -> {
                        CountdownContent(
                            secondsRemaining = countdownSeconds,
                            exercise = currentExercise
                        )
                    }
                    WorkoutPhase.ACTIVE -> {
                        ActiveWorkoutContent(
                            exercise = currentExercise,
                            nextExercise = nextExercise,
                            isPaused = isPaused,
                            secondsRemaining = activeSecondsRemaining,
                            repsCompleted = activeReps,
                            onAddRep = {
                                activeReps += 1
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            onPauseToggle = {
                                isPaused = !isPaused
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            onFinishSet = { completeExercise(skipped = false) },
                            onSkip = { completeExercise(skipped = true) }
                        )
                    }
                    WorkoutPhase.REST -> {
                        RestContent(
                            restSeconds = restSecondsRemaining,
                            nextExercise = nextExercise,
                            onSkipRest = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                moveToNextExercise()
                            }
                        )
                    }
                    WorkoutPhase.COMPLETE -> {
                        CompletionContent(
                            completedExercises = completedExercises,
                            totalReps = totalReps,
                            totalTimeSeconds = workoutElapsedSeconds,
                            isPersonalBest = isPersonalBest,
                            onFinish = { stopSession() }
                        )
                    }
                }
            }

            MusicModeOverlay(
                elapsedSeconds = workoutElapsedSeconds,
                expectedDurationSeconds = expectedDurationSeconds,
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
                phaseHint = when (phase) {
                    WorkoutPhase.COUNTDOWN -> MusicPhase.WARM_UP
                    WorkoutPhase.ACTIVE -> MusicPhase.MAIN
                    WorkoutPhase.REST -> MusicPhase.MAIN
                    WorkoutPhase.COMPLETE -> MusicPhase.FINISHER
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun CountdownContent(
    secondsRemaining: Int,
    exercise: WorkoutExercise?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Starting in",
            style = MaterialTheme.typography.titleMedium,
            color = PushPrimeColors.OnSurfaceVariant
        )
        Text(
            text = secondsRemaining.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 88.sp,
            color = PushPrimeColors.Primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (exercise != null) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = exercise.targetLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActiveWorkoutContent(
    exercise: WorkoutExercise?,
    nextExercise: WorkoutExercise?,
    isPaused: Boolean,
    secondsRemaining: Int,
    repsCompleted: Int,
    onAddRep: () -> Unit,
    onPauseToggle: () -> Unit,
    onFinishSet: () -> Unit,
    onSkip: () -> Unit
) {
    if (exercise == null) return
    val progress = if (exercise.targetSeconds > 0) {
        1f - (secondsRemaining.toFloat() / exercise.targetSeconds.toFloat())
    } else {
        0f
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = exercise.cue,
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )

            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    color = PushPrimeColors.Primary
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatWorkoutTime(secondsRemaining),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = exercise.targetLabel(),
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }

            if (exercise.targetReps != null) {
                Text(
                    text = "Reps: $repsCompleted / ${exercise.targetReps}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(
                    onClick = onAddRep,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add rep")
                }
            }

            if (isPaused) {
                Text(
                    text = "Paused",
                    style = MaterialTheme.typography.labelLarge,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NextExercisePreview(nextExercise = nextExercise)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPauseToggle,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume" else "Pause"
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (isPaused) "Resume" else "Pause")
                }
                Button(
                    onClick = onFinishSet,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Finish set"
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Finish")
                }
            }
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Skip exercise"
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Skip exercise")
            }
        }
    }
}

@Composable
private fun RestContent(
    restSeconds: Int,
    nextExercise: WorkoutExercise?,
    onSkipRest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Rest",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$restSeconds sec",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.Primary
            )
            Text(
                text = "Breathe and reset",
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            NextExercisePreview(nextExercise = nextExercise)
            Button(
                onClick = onSkipRest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Skip rest"
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Skip rest")
            }
        }
    }
}

@Composable
private fun CompletionContent(
    completedExercises: List<CompletedExercise>,
    totalReps: Int,
    totalTimeSeconds: Int,
    isPersonalBest: Boolean,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Workout complete",
                tint = PushPrimeColors.Success,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = "Workout complete",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Nice work finishing strong.",
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Total time",
                    value = formatWorkoutTime(totalTimeSeconds),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Total reps",
                    value = totalReps.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
            if (isPersonalBest) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = PushPrimeColors.Secondary.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Personal best",
                            tint = PushPrimeColors.Secondary
                        )
                        Text(
                            text = "New personal best!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (completedExercises.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        completedExercises.forEachIndexed { index, exercise ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(exercise.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    text = if (exercise.reps > 0) "${exercise.reps} reps" else "${exercise.seconds}s",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PushPrimeColors.OnSurfaceVariant
                                )
                            }
                            if (index != completedExercises.lastIndex) {
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                            }
                        }
                    }
                }
            }

            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = PushPrimeColors.OnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NextExercisePreview(nextExercise: WorkoutExercise?) {
    if (nextExercise == null) return
    Card(
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Up next",
                    style = MaterialTheme.typography.labelLarge,
                    color = PushPrimeColors.OnSurfaceVariant
                )
                Text(
                    text = nextExercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = nextExercise.targetLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next exercise",
                tint = PushPrimeColors.Primary
            )
        }
    }
}

fun formatWorkoutTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

private data class WorkoutExercise(
    val name: String,
    val targetReps: Int?,
    val targetSeconds: Int,
    val restSeconds: Int,
    val cue: String
) {
    fun targetLabel(): String {
        return targetReps?.let { "Target: $it reps" } ?: "Target: $targetSeconds sec"
    }
}

private data class CompletedExercise(
    val name: String,
    val reps: Int,
    val seconds: Int
)

private enum class WorkoutPhase {
    COUNTDOWN,
    ACTIVE,
    REST,
    COMPLETE
}
