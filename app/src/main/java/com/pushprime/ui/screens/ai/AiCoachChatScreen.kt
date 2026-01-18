package com.pushprime.ui.screens.ai

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.ui.components.AppTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.pushprime.coach.CoachSettings
import com.pushprime.coach.VoiceProviderAdapter
import com.pushprime.coach.VoiceProviderType
import com.pushprime.data.CoachSettingsRepository
import com.pushprime.data.OpenAiKeyStore
import com.pushprime.ui.theme.PushPrimeTheme
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

    AiCoachChatContent(
        messages = uiState.messages,
        isLoading = uiState.isLoading,
        speakReplies = coachSettings.speakReplies,
        input = input,
        onInputChange = { input = it },
        onSpeakRepliesChange = { enabled ->
            coroutineScope.launch { coachSettingsRepository.setSpeakReplies(enabled) }
        },
        onPromptSelected = { prompt -> input = prompt },
        onSend = { message ->
            viewModel.sendMessage(message)
            input = ""
        },
        onNavigateBack = onNavigateBack,
        onNavigateToSetup = onNavigateToSetup
    )
}

@Composable
private fun AiCoachChatContent(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    speakReplies: Boolean,
    input: String,
    onInputChange: (String) -> Unit,
    onSpeakRepliesChange: (Boolean) -> Unit,
    onPromptSelected: (String) -> Unit,
    onSend: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToSetup: () -> Unit
) {
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = speakReplies,
                            onCheckedChange = onSpeakRepliesChange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onNavigateToSetup,
                            modifier = Modifier.heightIn(min = 48.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "Setup",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                items(messages) { message ->
                    MessageBubble(message)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                suggestedPrompts.forEach { prompt ->
                    OutlinedButton(
                        onClick = { onPromptSelected(prompt) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Text(prompt, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                val isMessageValid = input.trim().isNotEmpty()
                AppTextField(
                    value = input,
                    onValueChange = onInputChange,
                    label = "Message",
                    placeholder = "Ask your coach...",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onSend(input.trim()) },
                    enabled = isMessageValid
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }

            if (isLoading) {
                Text("Coach is thinking...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
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
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .background(bubbleColor, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            )
        }
    }
}

@Preview(name = "AI Coach Chat - Light", showBackground = true)
@Preview(
    name = "AI Coach Chat - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AiCoachChatScreenPreview() {
    PushPrimeTheme {
        AiCoachChatContent(
            messages = listOf(
                ChatMessage(ChatRole.ASSISTANT, "Tell me your goal and how much time you have."),
                ChatMessage(ChatRole.USER, "30 mins, fat loss, no equipment."),
                ChatMessage(ChatRole.ASSISTANT, "Got it. Here’s a 30-min session: warm-up, circuit, finisher.")
            ),
            isLoading = false,
            speakReplies = false,
            input = "Build me a 30-min fat burn session",
            onInputChange = {},
            onSpeakRepliesChange = {},
            onPromptSelected = {},
            onSend = {},
            onNavigateBack = {},
            onNavigateToSetup = {}
        )
    }
}
