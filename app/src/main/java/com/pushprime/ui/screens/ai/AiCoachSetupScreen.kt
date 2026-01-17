package com.pushprime.ui.screens.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.data.AiCoachMode
import com.pushprime.ui.components.RamboostTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachSetupScreen(
    viewModel: AiCoachSetupViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState(initial = null)
    val state by viewModel.state.collectAsState()
    var apiKeyInput by remember { mutableStateOf(viewModel.getSavedKey().orEmpty()) }
    var providerExpanded by remember { mutableStateOf(false) }
    val isModelNameValid = !settings?.modelName.isNullOrBlank()
    val isApiKeyValid = apiKeyInput.trim().isNotEmpty()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Coach Setup", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Choose mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(
                    selected = settings?.mode == AiCoachMode.OPENAI,
                    onClick = { viewModel.updateMode(AiCoachMode.OPENAI) }
                )
                Column {
                    Text("Use AI Coach (Bring your own API key)", fontWeight = FontWeight.Medium)
                    Text("OpenAI supported", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RadioButton(
                    selected = settings?.mode == AiCoachMode.BASIC,
                    onClick = { viewModel.updateMode(AiCoachMode.BASIC) }
                )
                Column {
                    Text("Use RAMBOOST basic coach (no AI)", fontWeight = FontWeight.Medium)
                    Text("Works offline", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }

            if (settings?.mode == AiCoachMode.OPENAI) {
                val modelNameError = if (!isModelNameValid) "Model name is required" else null
                val apiKeyError = if (!isApiKeyValid) "API key is required" else null
                Text("Provider", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = { providerExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("OpenAI")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = providerExpanded, onDismissRequest = { providerExpanded = false }) {
                    DropdownMenuItem(text = { Text("OpenAI") }, onClick = { providerExpanded = false })
                    DropdownMenuItem(text = { Text("Custom provider (future)") }, onClick = { providerExpanded = false }, enabled = false)
                }

                RamboostTextField(
                    value = settings?.modelName.orEmpty(),
                    onValueChange = { viewModel.updateModelName(it) },
                    label = "Model name",
                    required = true,
                    errorText = modelNameError,
                    modifier = Modifier.fillMaxWidth()
                )

                RamboostTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = "API key",
                    required = true,
                    errorText = apiKeyError,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = { viewModel.verifyAndSaveKey(apiKeyInput.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isApiKeyValid && isModelNameValid && !state.isVerifying
                ) {
                    Text(if (state.isVerifying) "Verifying..." else "Verify & Save")
                }

                Text(
                    "Your key is stored locally on your device.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            state.statusMessage?.let { message ->
                Text(message, color = Color.Gray)
            }
        }
    }
}
