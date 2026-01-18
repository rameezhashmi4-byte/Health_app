package com.pushprime.ui.screens.ai

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.data.AiCoachMode
import com.pushprime.data.AiCoachSettings
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.theme.PushPrimeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachSetupScreen(
    viewModel: AiCoachSetupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState(initial = null)
    val state by viewModel.state.collectAsState()
    var apiKeyInput by remember { mutableStateOf(viewModel.getSavedKey().orEmpty()) }

    AiCoachSetupContent(
        settings = settings,
        state = state,
        apiKeyInput = apiKeyInput,
        onApiKeyInputChange = { apiKeyInput = it },
        onUpdateMode = viewModel::updateMode,
        onUpdateBaseUrl = viewModel::updateBaseUrl,
        onUpdateModelName = viewModel::updateModelName,
        onVerifyAndSaveKey = { viewModel.verifyAndSaveKey(it.trim()) },
        onTestAiCoach = viewModel::testAiCoach,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiCoachSetupContent(
    settings: AiCoachSettings?,
    state: AiCoachSetupState,
    apiKeyInput: String,
    onApiKeyInputChange: (String) -> Unit,
    onUpdateMode: (AiCoachMode) -> Unit,
    onUpdateBaseUrl: (String) -> Unit,
    onUpdateModelName: (String) -> Unit,
    onVerifyAndSaveKey: (String) -> Unit,
    onTestAiCoach: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var providerExpanded by remember { mutableStateOf(false) }
    val isModelNameValid = !settings?.modelName.isNullOrBlank()
    val isApiKeyValid = apiKeyInput.trim().isNotEmpty()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "AI Coach Setup",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Choose mode",
                style = MaterialTheme.typography.titleLarge
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(
                    selected = settings?.mode == AiCoachMode.OPENAI,
                    onClick = { onUpdateMode(AiCoachMode.OPENAI) }
                )
                Column {
                    Text(
                        text = "Use AI Coach (Bring your own API key)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "OpenAI supported",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(
                    selected = settings?.mode == AiCoachMode.BASIC,
                    onClick = { onUpdateMode(AiCoachMode.BASIC) }
                )
                Column {
                    Text(
                        text = "Use RAMBOOST basic coach (no AI)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Works offline",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (settings?.mode == AiCoachMode.OPENAI) {
                val modelNameError = if (!isModelNameValid) "Model name is required" else null
                val apiKeyError = if (!isApiKeyValid) "API key is required" else null
                Text(
                    text = "Provider",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedButton(
                    onClick = { providerExpanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = "OpenAI",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = providerExpanded, onDismissRequest = { providerExpanded = false }) {
                    DropdownMenuItem(text = { Text("OpenAI") }, onClick = { providerExpanded = false })
                    DropdownMenuItem(text = { Text("Custom provider (future)") }, onClick = { providerExpanded = false }, enabled = false)
                }

                AppTextField(
                    value = settings?.baseUrl.orEmpty(),
                    onValueChange = onUpdateBaseUrl,
                    label = "Base URL",
                    required = true,
                    placeholder = "https://api.openai.com",
                    modifier = Modifier.fillMaxWidth()
                )

                AppTextField(
                    value = settings?.modelName.orEmpty(),
                    onValueChange = onUpdateModelName,
                    label = "Model name",
                    required = true,
                    errorText = modelNameError,
                    modifier = Modifier.fillMaxWidth()
                )

                AppTextField(
                    value = apiKeyInput,
                    onValueChange = onApiKeyInputChange,
                    label = "API key",
                    required = true,
                    errorText = apiKeyError,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = { onVerifyAndSaveKey(apiKeyInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    enabled = isApiKeyValid && isModelNameValid && !state.isVerifying
                ) {
                    Text(
                        text = if (state.isVerifying) "Verifying..." else "Verify & Save",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    "Your key is stored locally on your device.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            state.statusMessage?.let { message ->
                Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (settings?.mode == AiCoachMode.OPENAI) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onTestAiCoach,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    enabled = !state.isVerifying
                ) {
                    Text("Test AI Coach")
                }

                state.testResult?.let { result ->
                    val color = if (result.success) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    val icon = if (result.success) "✅" else "❌"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("$icon ${if (result.success) "Success response" else "Request failed"}", color = color, style = MaterialTheme.typography.titleMedium)
                        Text(result.message, color = color, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Base URL used: ${result.baseUrl}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "AI Coach Setup - Light", showBackground = true)
@Preview(
    name = "AI Coach Setup - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AiCoachSetupScreenPreview() {
    PushPrimeTheme {
        var apiKey by remember { mutableStateOf("") }
        AiCoachSetupContent(
            settings = AiCoachSettings(mode = AiCoachMode.OPENAI, modelName = "gpt-4o-mini", baseUrl = "https://api.openai.com"),
            state = AiCoachSetupState(statusMessage = "Verified & saved.", isVerifying = false),
            apiKeyInput = apiKey,
            onApiKeyInputChange = { apiKey = it },
            onUpdateMode = {},
            onUpdateBaseUrl = {},
            onUpdateModelName = {},
            onVerifyAndSaveKey = {},
            onTestAiCoach = {},
            onNavigateBack = {}
        )
    }
}
