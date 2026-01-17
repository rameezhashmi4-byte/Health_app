package com.pushprime.ui.screens.sports

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.model.SportType
import com.pushprime.ui.theme.PushPrimeColors

private data class SportsCardInfo(
    val sport: SportType,
    val typicalSession: String,
    val focus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SportsModeSelectorScreen(
    onStartSession: (SportType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sports = listOf(
        SportsCardInfo(SportType.SQUASH, "Typical session: 30–60 mins", "Focus: cardio + agility"),
        SportsCardInfo(SportType.FOOTBALL, "Typical session: 60–90 mins", "Focus: endurance + speed"),
        SportsCardInfo(SportType.CRICKET, "Typical session: 45–90 mins", "Focus: stamina + coordination"),
        SportsCardInfo(SportType.RUGBY, "Typical session: 60–80 mins", "Focus: power + conditioning")
    )
    var selectedSport by remember { mutableStateOf<SportType?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sports Mode",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                ),
                navigationIcon = {
                    Text(
                        text = "←",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onNavigateBack() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        containerColor = Color.White,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose your sport and go beast mode.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                sports.forEach { info ->
                    val isSelected = selectedSport == info.sport
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSport = info.sport },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color.Black else PushPrimeColors.Surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = info.sport.icon,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = info.sport.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.Black
                                )
                            }
                            Text(
                                text = info.typicalSession,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color(0xFFBDBDBD) else Color.Gray
                            )
                            Text(
                                text = info.focus,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) Color(0xFFBDBDBD) else Color.Gray
                            )
                        }
                    }
                }
            }

            AnimatedContent(targetState = selectedSport) { sport ->
                Button(
                    onClick = { sport?.let(onStartSession) },
                    enabled = sport != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF1F1F1F),
                        disabledContentColor = Color.Gray
                    )
                ) {
                    Text("Start Session", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
