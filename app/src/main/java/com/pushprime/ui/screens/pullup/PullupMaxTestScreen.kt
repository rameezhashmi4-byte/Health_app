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
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullupMaxTestScreen(
    viewModel: PullupMaxTestViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var maxRepsText by remember { mutableStateOf("") }
    var formRating by remember { mutableStateOf<Int?>(null) }
    var showValidation by remember { mutableStateOf(false) }
    val isNewPr by viewModel.isNewPr.collectAsState()

    val maxReps = maxRepsText.toIntOrNull() ?: 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Max Reps Test", fontWeight = FontWeight.Black) },
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
            TextField(
                value = maxRepsText,
                onValueChange = { maxRepsText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Max reps achieved") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Form quality (optional)", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { rating ->
                    OutlinedButton(
                        onClick = { formRating = rating },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            rating.toString(),
                            fontWeight = if (formRating == rating) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (showValidation && maxReps <= 0) {
                Text("Enter a max reps value.", color = Color.Red)
            }

            if (isNewPr) {
                Text("🔥 New PR!", color = Color(0xFFFF6D00), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (maxReps <= 0) {
                        showValidation = true
                        return@Button
                    }
                    viewModel.saveMaxTest(maxReps, formRating)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Test")
            }
        }
    }
}
