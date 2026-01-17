package com.pushprime.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Error Screen
 * Shows error message when app initialization fails
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(
    message: String,
    onNavigateBack: (() -> Unit)? = null
) {
    Scaffold(
        topBar = {
            if (onNavigateBack != null) {
                TopAppBar(
                    title = { Text("Error") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PushPrimeColors.Surface
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(PushPrimeColors.Background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PushPrimeColors.Error.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = PushPrimeColors.Error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PushPrimeColors.OnSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
