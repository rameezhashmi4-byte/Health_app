package com.pushprime.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.LocalStore
import com.pushprime.data.SessionDao
import com.pushprime.data.calculateStreak
import com.pushprime.data.todaySessionDate
import com.pushprime.data.totalDurationSeconds
import com.pushprime.data.totalRepsForDate
import com.pushprime.ui.components.FeedCard
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Profile Screen
 * Instagram-like profile with stats, preferences, and settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    localStore: LocalStore,
    sessionDao: SessionDao?,
    onNavigateToPhotoVault: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionDao == null) {
        ErrorScreen(message = "Database not available")
        return
    }

    val user by localStore.user.collectAsState(initial = null)
    val sessions by sessionDao.getAllSessions().collectAsState(initial = emptyList())
    
    var streak by remember { mutableStateOf(0) }
    var totalSessions by remember { mutableStateOf(0) }
    var totalMinutes by remember { mutableStateOf(0) }
    var todayTotal by remember { mutableStateOf(0) }
    
    LaunchedEffect(sessions) {
        streak = calculateStreak(sessions)
        totalSessions = sessions.size
        totalMinutes = totalDurationSeconds(sessions) / 60
        todayTotal = totalRepsForDate(sessions, todaySessionDate())
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = PushPrimeColors.Primary.copy(alpha = 0.2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = user?.username?.take(1)?.uppercase() ?: "U",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = PushPrimeColors.Primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user?.username ?: "User",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (user != null) {
                        Text(
                            text = "${user!!.age} years • ${user!!.fitnessLevel.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
            }
            
            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Streak", "$streak days", "🔥")
                    StatItem("Sessions", "$totalSessions", "💪")
                    StatItem("Minutes", "$totalMinutes", "⏱️")
                }
            }
            
            // Insight
            item {
                FeedCard(
                    title = "Insight",
                    subtitle = buildInsight(streak, todayTotal, user?.dailyGoal ?: 100),
                    icon = Icons.Default.TrendingUp
                )
            }
            
            item {
                FeedCard(
                    title = "Photo Vault",
                    subtitle = "Before/After photos",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = onNavigateToPhotoVault
                )
            }
            
            // Preferences Section
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                PreferenceItem(
                    title = "Notifications",
                    subtitle = "Workout reminders",
                    icon = Icons.Default.Notifications,
                    onClick = onNavigateToNotificationSettings
                )
            }

            item {
                PreferenceItem(
                    title = "Account",
                    subtitle = "Email and security",
                    icon = Icons.Default.Person,
                    onClick = onNavigateToAccount
                )
            }
            
            item {
                PreferenceItem(
                    title = "Privacy",
                    subtitle = "Data and sharing",
                    icon = Icons.Default.Lock,
                    onClick = { /* TODO: Open privacy settings */ }
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, emoji: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PushPrimeColors.Primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PushPrimeColors.OnSurfaceVariant
        )
    }
}

@Composable
fun PreferenceItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PushPrimeColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = PushPrimeColors.OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun buildInsight(streak: Int, todayTotal: Int, dailyGoal: Int): String {
    return when {
        streak >= 7 -> "Seven-day streak. Keep the rhythm strong."
        todayTotal >= dailyGoal -> "Goal achieved today. Consider a light recovery set."
        todayTotal > 0 -> "You're ${dailyGoal - todayTotal} reps from today's goal."
        else -> "Start a quick session to build consistency."
    }
}
