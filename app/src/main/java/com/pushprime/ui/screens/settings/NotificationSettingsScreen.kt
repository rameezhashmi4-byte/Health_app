package com.pushprime.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.core.content.ContextCompat
import com.pushprime.data.NotificationHelper
import com.pushprime.ui.theme.PushPrimeColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Notification Settings Screen
 * Configure smart daily exercise reminders
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    
    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedHour by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(0) }
    
    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val isNotificationPermissionGranted = remember {
        mutableStateOf(
            !needsNotificationPermission || ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationPermissionGranted.value = granted
    }
    
    LaunchedEffect(notificationsEnabled) {
        if (notificationsEnabled) {
            if (needsNotificationPermission && !isNotificationPermissionGranted.value) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (!needsNotificationPermission || isNotificationPermissionGranted.value) {
                notificationHelper.scheduleDailyReminders()
            }
        } else {
            notificationHelper.cancelReminders()
        }
    }
    
    LaunchedEffect(selectedHour, selectedMinute) {
        if (notificationsEnabled) {
            notificationHelper.updatePreferredWorkoutTime(selectedHour)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notification Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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
            item {
                Text(
                    text = "Daily Reminders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Card(
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Enable Daily Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Get notified about your recommended daily exercise",
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }
                }
            }
            
            if (notificationsEnabled) {
                item {
                    Text(
                        text = "Preferred Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PushPrimeColors.Surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Optimal Workout Time",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "We'll remind you at the best time based on your schedule",
                                style = MaterialTheme.typography.bodySmall,
                                color = PushPrimeColors.OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Time picker (simplified)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TimeSelector(
                                    label = "Hour",
                                    value = selectedHour,
                                    onValueChange = { selectedHour = it },
                                    range = 6..22
                                )
                                Text(":", style = MaterialTheme.typography.headlineMedium)
                                TimeSelector(
                                    label = "Minute",
                                    value = selectedMinute,
                                    onValueChange = { selectedMinute = it },
                                    range = 0..59,
                                    step = 15
                                )
                            }
                        }
                    }
                }
                
                item {
                    InfoCard(
                        icon = Icons.Default.Info,
                        title = "Smart Scheduling",
                        description = "Notifications are sent at optimal times based on your workout history and schedule. You'll get one reminder per day."
                    )
                }
            }
        }
    }
}

@Composable
fun TimeSelector(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    step: Int = 1
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PushPrimeColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newValue = (value - step).coerceIn(range.first, range.last)
                    onValueChange(newValue)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(
                text = String.format("%02d", value),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = {
                    val newValue = (value + step).coerceIn(range.first, range.last)
                    onValueChange(newValue)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PushPrimeColors.Primary.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PushPrimeColors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PushPrimeColors.Primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
    }
}
