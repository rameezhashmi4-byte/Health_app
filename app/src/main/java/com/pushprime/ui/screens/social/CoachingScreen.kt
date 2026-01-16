package com.pushprime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pushprime.ai.PredictionHelper
import com.pushprime.data.LocalStore
import com.pushprime.model.User
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Coaching Screen
 * User input (age, gender, fitness level) and AI prediction
 */
@Composable
fun CoachingScreen(
    localStore: LocalStore,
    onNavigateBack: () -> Unit
) {
    var age by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf<User.Gender?>(null) }
    var selectedLevel by remember { mutableStateOf<User.FitnessLevel?>(null) }
    var prediction by remember { mutableStateOf<Int?>(null) }
    var showPrediction by remember { mutableStateOf(false) }
    val user by localStore.user.collectAsState()
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PushPrimeColors.Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Smart Coaching",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.OnSurface
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PushPrimeColors.Primary.copy(alpha = 0.12f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Let's personalize your journey",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PushPrimeColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Share some details, and we'll predict your push-up potential and create a personalized plan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
            
            // Age Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PushPrimeColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Age",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PushPrimeColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                        label = { Text("Enter your age") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
            
            // Gender Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PushPrimeColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PushPrimeColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GenderOption(
                            label = "Male",
                            selected = selectedGender == User.Gender.MALE,
                            onClick = { selectedGender = User.Gender.MALE },
                            modifier = Modifier.weight(1f)
                        )
                        GenderOption(
                            label = "Female",
                            selected = selectedGender == User.Gender.FEMALE,
                            onClick = { selectedGender = User.Gender.FEMALE },
                            modifier = Modifier.weight(1f)
                        )
                        GenderOption(
                            label = "Other",
                            selected = selectedGender == User.Gender.OTHER,
                            onClick = { selectedGender = User.Gender.OTHER },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            
            // Fitness Level
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PushPrimeColors.Surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Fitness Level",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PushPrimeColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FitnessLevelOption(
                            label = "Beginner",
                            description = "Starting your fitness journey",
                            level = User.FitnessLevel.BEGINNER,
                            selected = selectedLevel == User.FitnessLevel.BEGINNER,
                            onClick = { selectedLevel = User.FitnessLevel.BEGINNER }
                        )
                        FitnessLevelOption(
                            label = "Intermediate",
                            description = "Regular exercise routine",
                            level = User.FitnessLevel.INTERMEDIATE,
                            selected = selectedLevel == User.FitnessLevel.INTERMEDIATE,
                            onClick = { selectedLevel = User.FitnessLevel.INTERMEDIATE }
                        )
                        FitnessLevelOption(
                            label = "Advanced",
                            description = "Experienced athlete",
                            level = User.FitnessLevel.ADVANCED,
                            selected = selectedLevel == User.FitnessLevel.ADVANCED,
                            onClick = { selectedLevel = User.FitnessLevel.ADVANCED }
                        )
                    }
                }
            }
            
            // Get Prediction Button
            Button(
                onClick = {
                    if (age.isNotBlank() && selectedGender != null && selectedLevel != null) {
                        val newUser = User(
                            username = user?.username ?: "User",
                            age = age.toInt(),
                            gender = selectedGender!!,
                            fitnessLevel = selectedLevel!!,
                            predictedMaxPushups = 0,
                            dailyGoal = 0,
                            country = user?.country ?: "US"
                        )
                        val predicted = PredictionHelper.predictMaxPushups(newUser)
                        val dailyGoal = PredictionHelper.getDailyGoal(predicted)
                        prediction = predicted
                        showPrediction = true
                        localStore.saveUser(newUser.copy(
                            predictedMaxPushups = predicted,
                            dailyGoal = dailyGoal
                        ))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = age.isNotBlank() && selectedGender != null && selectedLevel != null,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PushPrimeColors.Primary
                )
            ) {
                Text(
                    text = "Get My Prediction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Prediction Result
            if (showPrediction && prediction != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PushPrimeColors.Success.copy(alpha = 0.12f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = PushPrimeColors.Success,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Estimated Max",
                            style = MaterialTheme.typography.titleMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                        Text(
                            text = "$prediction push-ups",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = PushPrimeColors.Success
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Daily Goal: ${user?.dailyGoal ?: 0} push-ups",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun GenderOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FitnessLevelOption(
    label: String,
    description: String,
    level: User.FitnessLevel,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) PushPrimeColors.Primary.copy(alpha = 0.12f) else PushPrimeColors.SurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PushPrimeColors.OnSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}
