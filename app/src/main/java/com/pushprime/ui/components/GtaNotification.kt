package com.pushprime.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.delay

@Composable
fun GtaNotification(
    message: String,
    subMessage: String = "",
    isSuccess: Boolean = true,
    onFinished: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(3000)
        visible = false
        delay(500)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSuccess) "MISSION PASSED" else "WASTED",
                    style = MaterialTheme.typography.displayLarge,
                    color = if (isSuccess) PushPrimeColors.GTAYellow else PushPrimeColors.GTARed
                )
                if (subMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subMessage,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}
