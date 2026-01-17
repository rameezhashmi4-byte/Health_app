package com.pushprime.ui.screens.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.ui.components.AppTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import com.pushprime.coach.CoachSettings
import com.pushprime.coach.VoiceProviderAdapter
import com.pushprime.coach.VoiceProviderType
import com.pushprime.data.CoachSettingsRepository
import com.pushprime.data.OpenAiKeyStore
import com.pushprime.voice.VoiceCoachSettings
import com.pushprime.voice.VoiceProviderFactory
import com.pushprime.voice.VoiceProviderLifecycle
import com.pushprime.voice.VoiceProviderType as LegacyVoiceProviderType
import com.pushprime.voice.VoiceType
import kotlinx.coroutines.launch

private val suggestedPrompts = listOf(
    "Build me a 30-min fat burn session",
    "What should I eat today to hit protein?",
    "Improve my pull-ups fast"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachChatScreen(
    viewModel: AiCoachViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    var input by remember { mutableStateOf("") }
    var lastSpokenIndex by remember { mutableStateOf(-1) }

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
            enabled = true,
            provider = providerType,
            voiceType = VoiceType.SYSTEM_DEFAULT
        )
        val created = voiceProviderFactory.create(baseSettings) { }
        if (!created.isAvailable) {
            voiceProviderFactory.create(
                baseSettings.copy(provider = LegacyVoiceProviderType.SYSTEM)
            ) { }
        } else {
            created
        }
    }
    val voiceAdapter = remember { VoiceProviderAdapter(voiceProvider) }

    DisposableEffect(voiceProvider) {
        voiceAdapter.updateProvider(voiceProvider)
        onDispose {
            if (voiceProvider is VoiceProviderLifecycle) {
                voiceProvider.shutdown()
            }
        }
    }

    LaunchedEffect(uiState.messages, coachSettings.speakReplies) {
        if (!coachSettings.speakReplies) return@LaunchedEffect
        val lastIndex = uiState.messages.indexOfLast { it.role == ChatRole.ASSISTANT }
        if (lastIndex >= 0 && lastIndex != lastSpokenIndex) {
            val message = uiState.messages[lastIndex].content.trim()
            if (message.isNotBlank()) {
                voiceAdapter.setStyle(coachSettings.style)
                voiceAdapter.speak(message)
                lastSpokenIndex = lastIndex
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "AI Coach",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Speak replies",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = coachSettings.speakReplies,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    coachSettingsRepository.setSpeakReplies(enabled)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = onNavigateToSetup) {
                            Text(
                                text = "Setup",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(message)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                suggestedPrompts.forEach { prompt ->
                    OutlinedButton(
                        onClick = { input = prompt },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(prompt, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val isMessageValid = input.trim().isNotEmpty()
                AppTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = "Message",
                    placeholder = "Ask your coach...",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val message = input.trim()
                        viewModel.sendMessage(message)
                        input = ""
                    },
                    enabled = isMessageValid
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }

            if (uiState.isLoading) {
                Text("Coach is thinking...", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val background = if (isUser) Color.Black else Color(0xFFF6F6F6)
    val textColor = if (isUser) Color.White else Color.Black
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Text(
                text = message.content,
                color = textColor,
                modifier = Modifier
                    .background(background, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
        }
    }
}
