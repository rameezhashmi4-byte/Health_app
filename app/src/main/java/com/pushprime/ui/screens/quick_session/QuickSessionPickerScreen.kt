package com.pushprime.ui.screens.quick_session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.QuickSessionTemplate
import com.pushprime.ui.theme.PushPrimeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSessionPickerScreen(
    onNavigateBack: () -> Unit,
    onStartSession: (String) -> Unit,
    viewModel: QuickSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Quick Sessions",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            "10 minutes. No excuses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.showStreakRisk) {
                item {
                    StreakRiskBanner()
                }
            }

            items(uiState.templates, key = { it.id }) { template ->
                QuickSessionTemplateCard(
                    template = template,
                    recommended = uiState.recommendedIds.contains(template.id),
                    onStart = { onStartSession(template.id) }
                )
            }
        }
    }
}

@Composable
private fun StreakRiskBanner() {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Secondary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Protect your streak today ❄️🔥",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Quick 10 minutes keeps it alive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickSessionTemplateCard(
    template: QuickSessionTemplate,
    recommended: Boolean,
    onStart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.UberGrey),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                }
                if (recommended) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Recommended for you") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(template.difficulty.label) })
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Best for: ${template.bestFor}",
                style = MaterialTheme.typography.bodySmall,
                color = PushPrimeColors.OnSurfaceVariant
            )
            Text(
                text = "Equipment: ${template.equipment}",
                style = MaterialTheme.typography.bodySmall,
                color = PushPrimeColors.OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Start",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
