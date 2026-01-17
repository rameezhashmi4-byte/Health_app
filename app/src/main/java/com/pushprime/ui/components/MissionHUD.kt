package com.pushprime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

@Composable
fun MissionHUD(
    health: Float, // 0.0 to 1.0
    stamina: Float, // 0.0 to 1.0
    points: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.End
    ) {
        // Points / Money style
        Text(
            text = "$${String.format("%,d", points)}",
            style = MaterialTheme.typography.displaySmall,
            color = PushPrimeColors.GTAGreen,
            fontWeight = FontWeight.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Health Bar (Green)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "HP",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            LinearProgressIndicator(
                progress = health,
                modifier = Modifier
                    .width(120.dp)
                    .height(8.dp),
                color = PushPrimeColors.GTAGreen,
                trackColor = Color.DarkGray
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Stamina Bar (Blue/Yellow)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "ST",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
            LinearProgressIndicator(
                progress = stamina,
                modifier = Modifier
                    .width(120.dp)
                    .height(8.dp),
                color = PushPrimeColors.GTAYellow,
                trackColor = Color.DarkGray
            )
        }
    }
}
