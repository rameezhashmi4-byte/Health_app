package com.pushprime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pushprime.model.ExerciseType
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Exercise Logger Component
 * Input field for logging reps or time
 */
@Composable
fun ExerciseLogger(
    exercise: ExerciseType,
    onLogSaved: (Int, Int) -> Unit, // reps/duration, intensity
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf(3) }
    var showInput by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            if (!showInput) {
                // Show exercise info and start button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PushPrimeColors.OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (exercise.isTimeBased) "Time-based exercise" else "Rep-based exercise",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showInput = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PushPrimeColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log")
                    }
                }
            } else {
                // Show input form
                Text(
                    text = "Log ${exercise.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PushPrimeColors.OnSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = value,
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            value = it
                        }
                    },
                    label = { 
                        Text(if (exercise.isTimeBased) "Duration (seconds)" else "Reps")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Intensity selector
                Text(
                    text = "Intensity: $intensity ⭐",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant
                )
                Slider(
                    value = intensity.toFloat(),
                    onValueChange = { intensity = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            showInput = false
                            value = ""
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val repsOrDuration = value.toIntOrNull() ?: 0
                            if (repsOrDuration > 0) {
                                onLogSaved(repsOrDuration, intensity)
                                showInput = false
                                value = ""
                                intensity = 3
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = value.toIntOrNull() ?: 0 > 0,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PushPrimeColors.Primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                }
            }
        }
    }
}
