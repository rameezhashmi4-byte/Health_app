package com.pushprime.ui.screens.quick_session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.QuickSessionTemplates
import com.pushprime.ui.components.RamboostTextField
import com.pushprime.ui.theme.PushPrimeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSessionCompletionScreen(
    templateId: String,
    durationMinutes: Int,
    onSave: () -> Unit,
    onBackToHome: () -> Unit,
    viewModel: QuickSessionViewModel = hiltViewModel()
) {
    val template = remember(templateId) { QuickSessionTemplates.byId(templateId) }
    var notes by remember { mutableStateOf("") }
    var markAsWorkout by rememberSaveable { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Complete", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("🎉", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Session Completed 🔥", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = template?.name ?: "Quick Session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }

            Column {
                Text("Total time", color = PushPrimeColors.OnSurfaceVariant)
                Text(
                    text = "$durationMinutes minutes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mark as workout", fontWeight = FontWeight.Medium)
                    Text(
                        "Counts toward streak",
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
                Switch(checked = markAsWorkout, onCheckedChange = { markAsWorkout = it })
            }

            RamboostTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = "Notes (optional)",
                placeholder = "Optional notes"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (isSaving) return@Button
                    isSaving = true
                    viewModel.saveCompletedSession(
                        templateId = templateId,
                        notes = notes,
                        markAsWorkout = markAsWorkout
                    ) {
                        isSaving = false
                        onSave()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }

            Button(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.UberGrey),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Home", color = Color.Black)
            }
        }
    }
}
