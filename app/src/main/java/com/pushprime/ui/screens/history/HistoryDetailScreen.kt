package com.pushprime.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.SessionEntity
import com.pushprime.ui.components.RamboostTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    sessionId: Long,
    startInEditMode: Boolean,
    onNavigateBack: () -> Unit,
    viewModel: HistoryDetailViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isEditing by remember { mutableStateOf(startInEditMode) }
    var notes by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf("MEDIUM") }
    var durationMinutes by remember { mutableIntStateOf(30) }
    var rating by remember { mutableFloatStateOf(3f) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(session) {
        val current = session ?: return@LaunchedEffect
        notes = current.notes.orEmpty()
        intensity = current.intensity.ifBlank { "MEDIUM" }
        durationMinutes = (current.getDurationSeconds() / 60).coerceAtLeast(1)
        rating = (current.rating ?: 3).toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isEditing) {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        when {
            isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color.Black)
                }
            }
            session == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Session not found", color = Color.Gray)
                }
            }
            else -> {
                val current = session!!
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF6F6F6),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = currentTitle(current),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatDateRange(current, durationMinutes),
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    item {
                        InfoRow("Duration", "${durationMinutes} mins")
                        InfoRow("Effort", intensity.replaceFirstChar { it.uppercaseChar() })
                        InfoRow("Calories", "${estimateCalories(current, durationMinutes)} kcal")
                        InfoRow("Rating", "${rating.toInt()}/5")
                        InfoRow("Session Type", formatActivityType(current))
                    }

                    item {
                        if (isEditing) {
                            EditSection(
                                session = current,
                                notes = notes,
                                onNotesChange = { notes = it },
                                intensity = intensity,
                                onIntensityChange = { intensity = it },
                                durationMinutes = durationMinutes,
                                onDurationChange = { durationMinutes = it },
                                rating = rating,
                                onRatingChange = { rating = it }
                            )
                        } else {
                            Text("Notes", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(notes.ifBlank { "No notes added." }, color = Color.Gray)
                        }
                    }

                    if (isEditing) {
                        item {
                            Button(
                                onClick = {
                                    val updated = current.copy(
                                        notes = notes.ifBlank { null },
                                        intensity = intensity,
                                        rating = rating.toInt(),
                                        totalSeconds = durationMinutes * 60
                                    )
                                    viewModel.updateSession(updated) {
                                        isEditing = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("Save Changes")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete session?") },
            text = { Text("This will remove the session permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        session?.let {
                            viewModel.deleteSession(it) { onNavigateBack() }
                        }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EditSection(
    session: SessionEntity,
    notes: String,
    onNotesChange: (String) -> Unit,
    intensity: String,
    onIntensityChange: (String) -> Unit,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    rating: Float,
    onRatingChange: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Edit Session", fontWeight = FontWeight.Bold)
        RamboostTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = "Notes"
        )
        RamboostTextField(
            value = durationMinutes.toString(),
            onValueChange = { value ->
                val parsed = value.toIntOrNull() ?: durationMinutes
                onDurationChange(parsed.coerceAtLeast(1))
            },
            modifier = Modifier.fillMaxWidth(),
            label = "Duration (mins)",
            required = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        if (session.activityType.equals("SPORT", ignoreCase = true)) {
            Text("Effort Level", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("LOW", "MEDIUM", "HIGH").forEach { level ->
                    FilterChip(
                        selected = intensity.equals(level, ignoreCase = true),
                        onClick = { onIntensityChange(level) },
                        label = { Text(level.lowercase().replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }
        }
        Text("Rating", fontWeight = FontWeight.SemiBold)
        Slider(
            value = rating,
            onValueChange = onRatingChange,
            valueRange = 1f..5f,
            steps = 3
        )
        Text("${rating.toInt()}/5", color = Color.Gray)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

private fun estimateCalories(session: SessionEntity, durationMinutes: Int): Int {
    val multiplier = when {
        session.intensity.equals("HIGH", ignoreCase = true) -> 12
        session.intensity.equals("LOW", ignoreCase = true) -> 6
        else -> 9
    }
    return durationMinutes * multiplier
}

private fun formatDateRange(session: SessionEntity, durationMinutes: Int): String {
    val formatter = SimpleDateFormat("EEE d MMM, h:mm a", Locale.getDefault())
    val start = formatter.format(Date(session.startTime))
    val endTime = session.endTime ?: (session.startTime + (durationMinutes * 60 * 1000L))
    val end = formatter.format(Date(endTime))
    return "$start • $end"
}

private fun currentTitle(session: SessionEntity): String {
    return when {
        session.activityType.equals("SPORT", ignoreCase = true) -> {
            val sport = session.sportType?.takeIf { it.isNotBlank() } ?: "Sports"
            "$sport Session"
        }
        session.activityType.equals("QUICK_SESSION", ignoreCase = true) -> {
            val base = session.exerciseId?.takeIf { it.isNotBlank() } ?: "Quick"
            "$base Quick Session"
        }
        else -> {
            val base = session.exerciseId?.takeIf { it.isNotBlank() } ?: "Workout"
            "$base Workout"
        }
    }
}

private fun formatActivityType(session: SessionEntity): String {
    return when {
        session.activityType.equals("SPORT", ignoreCase = true) -> "Sport"
        session.activityType.equals("QUICK_SESSION", ignoreCase = true) -> "Quick Session"
        else -> "Workout"
    }
}
