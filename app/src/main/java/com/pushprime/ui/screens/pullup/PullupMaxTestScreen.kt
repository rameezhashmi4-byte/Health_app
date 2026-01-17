package com.pushprime.ui.screens.pullup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.ui.components.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullupMaxTestScreen(
    viewModel: PullupMaxTestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var maxRepsText by remember { mutableStateOf("") }
    var formRating by remember { mutableStateOf<Int?>(null) }
    val isNewPr by viewModel.isNewPr.collectAsState()

    val maxReps = maxRepsText.toIntOrNull() ?: 0
    val maxRepsError = if (maxReps <= 0) "Enter a max reps value." else null
    val isFormValid = maxReps > 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Max Reps Test",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
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
            AppTextField(
                value = maxRepsText,
                onValueChange = {
                    maxRepsText = it.filter { ch -> ch.isDigit() }
                },
                label = "Max reps achieved",
                required = true,
                errorText = maxRepsError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Form quality (optional)",
                style = MaterialTheme.typography.titleLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { rating ->
                    OutlinedButton(
                        onClick = { formRating = rating },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            rating.toString(),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            if (isNewPr) {
                Text(
                    text = "🔥 New PR!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFF6D00)
                )
            }

            Button(
                onClick = {
                    if (maxReps <= 0) {
                        return@Button
                    }
                    viewModel.saveMaxTest(maxReps, formRating)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text(
                    text = "Save Test",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
