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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pushprime.data.notifications.NotificationPermissions
import com.pushprime.data.notifications.NotificationPreferences
import com.pushprime.data.notifications.NotificationPreferencesStore
import com.pushprime.data.notifications.NotificationScheduler
import com.pushprime.data.notifications.NotificationTimeUtils
import com.pushprime.ui.components.AppCard
import com.pushprime.ui.components.AppSecondaryButton
import com.pushprime.ui.components.PremiumFadeSlideIn
import com.pushprime.ui.theme.AppSpacing
import kotlinx.coroutines.launch

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
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        PremiumFadeSlideIn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg)
            ) {
                item {
                    SectionCard(
                        title = "Enable Notifications",
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
                        AppCard(containerColor = MaterialTheme.colorScheme.errorContainer) {
                            Text(
                                text = "Permission denied. Please enable notifications in system settings to receive reminders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                item {
                    SectionCard(
                        title = "Daily Reminder",
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
                    AppCard(modifier = Modifier.alpha(if (preferences.notificationsEnabled && preferences.dailyReminderEnabled) 1f else 0.5f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Reminder Time",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = NotificationTimeUtils.formatTime(preferences.dailyReminderTimeMinutes),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            AppSecondaryButton(
                                text = "Change",
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
                                enabled = preferences.notificationsEnabled && preferences.dailyReminderEnabled,
                                fullWidth = false
                            )
                        }
                    }
                }

                item {
                    SectionCard(
                        title = "Streak Protection",
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
                
                item {
                    Spacer(modifier = Modifier.height(AppSpacing.xxl))
                }
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
    AppCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.md))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
