package com.pushprime.ui.screens.settings

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.notifications.NotificationPermissions
import com.pushprime.data.notifications.NotificationPreferences
import com.pushprime.data.notifications.NotificationPreferencesStore
import com.pushprime.data.notifications.NotificationScheduler
import com.pushprime.data.notifications.NotificationTimeUtils
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.launch

/**
 * Notification Settings Screen
 * Configure local reminders and permissions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesStore = remember { NotificationPreferencesStore(context) }
    val coroutineScope = rememberCoroutineScope()
    val preferences by preferencesStore.preferencesFlow.collectAsState(
        initial = NotificationPreferences()
    )

    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val isNotificationPermissionGranted = remember {
        mutableStateOf(NotificationPermissions.isGranted(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationPermissionGranted.value = granted
    }

    fun requestPermissionIfNeeded() {
        if (needsNotificationPermission && !isNotificationPermissionGranted.value) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(preferences, isNotificationPermissionGranted.value) {
        if (preferences.notificationsEnabled && isNotificationPermissionGranted.value) {
            NotificationScheduler.resync(context)
        } else {
            NotificationScheduler.cancelDailyReminder(context)
            NotificationScheduler.cancelStreakProtection(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifications",
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
                SectionCard(
                    title = "Enable notifications",
                    description = "Allow reminders to keep you on track",
                    checked = preferences.notificationsEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            preferencesStore.setNotificationsEnabled(enabled)
                        }
                        if (enabled) {
                            requestPermissionIfNeeded()
                        }
                    }
                )
            }

            if (preferences.notificationsEnabled && needsNotificationPermission && !isNotificationPermissionGranted.value) {
                item {
                    Text(
                        text = "Enable in system settings",
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }

            item {
                SectionCard(
                    title = "Daily reminder",
                    description = "“Time to train” at your chosen time",
                    checked = preferences.dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            preferencesStore.setDailyReminderEnabled(enabled)
                        }
                        if (enabled) {
                            requestPermissionIfNeeded()
                        }
                    },
                    enabled = preferences.notificationsEnabled
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Daily reminder time",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = NotificationTimeUtils.formatTime(preferences.dailyReminderTimeMinutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            val current = NotificationTimeUtils.minutesToLocalTime(
                                preferences.dailyReminderTimeMinutes
                            )
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    coroutineScope.launch {
                                        preferencesStore.setDailyReminderTimeMinutes(
                                            hour * 60 + minute
                                        )
                                    }
                                },
                                current.hour,
                                current.minute,
                                false
                            ).show()
                        },
                        enabled = preferences.notificationsEnabled && preferences.dailyReminderEnabled
                    ) {
                        Text("Change")
                    }
                }
            }

            item {
                SectionCard(
                    title = "Streak protection",
                    description = "Ping me if I haven’t worked out today",
                    checked = preferences.streakProtectionEnabled,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            preferencesStore.setStreakProtectionEnabled(enabled)
                        }
                        if (enabled) {
                            requestPermissionIfNeeded()
                        }
                    },
                    enabled = preferences.notificationsEnabled
                )
            }

        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = PushPrimeColors.Primary
                )
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}
