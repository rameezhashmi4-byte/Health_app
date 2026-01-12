package com.pushprime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.pushprime.data.NotificationHelper
import com.pushprime.ui.theme.PushPrimeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PushPrimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PushPrimeApp()
                    
                    // Initialize notifications on app start
                    LaunchedEffect(Unit) {
                        val notificationHelper = NotificationHelper(this@MainActivity)
                        // Schedule daily reminders if not already scheduled
                        notificationHelper.scheduleDailyReminders()
                    }
                }
            }
        }
    }
}
