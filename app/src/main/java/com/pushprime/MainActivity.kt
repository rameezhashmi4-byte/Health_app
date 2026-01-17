package com.pushprime

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import com.pushprime.data.notifications.NotificationHelper
import com.pushprime.data.notifications.NotificationScheduler
import com.pushprime.data.notifications.SmartReminderNotificationConstants
import com.pushprime.ui.theme.RamboostTheme

import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val deepLinkRouteState = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkRouteState.value = intent?.getStringExtra(
            SmartReminderNotificationConstants.EXTRA_DEEP_LINK_ROUTE
        )
        
        // Make the app "slick" by enabling edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
            // Firebase is optional; app should still run without it
        }
        setContent {
            RamboostTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RamboostApp(
                        deepLinkRoute = deepLinkRouteState.value,
                        onDeepLinkConsumed = { deepLinkRouteState.value = null }
                    )
                    
                    // Initialize notifications on app start
                    LaunchedEffect(Unit) {
                        NotificationHelper(this@MainActivity).createChannels()
                        NotificationScheduler.resync(this@MainActivity)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        deepLinkRouteState.value = intent?.getStringExtra(
            SmartReminderNotificationConstants.EXTRA_DEEP_LINK_ROUTE
        )
    }
}
