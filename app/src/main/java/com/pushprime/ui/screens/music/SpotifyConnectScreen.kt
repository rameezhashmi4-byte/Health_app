package com.pushprime.ui.screens.music

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.music.MusicProviderManager
import com.pushprime.music.MusicSource
import com.pushprime.ui.theme.PushPrimeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyConnectScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Connect Spotify", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                ),
                navigationIcon = {
                    Text(
                        text = "←",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onNavigateBack() },
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connect Spotify",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "RAMBOOST can control playback during workouts.",
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )
            Text(
                text = "Coming soon (requires Spotify SDK + app credentials)",
                style = MaterialTheme.typography.bodySmall,
                color = PushPrimeColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    MusicProviderManager.setSource(MusicSource.SPOTIFY)
                    MusicProviderManager.currentProvider.value.connect()
                    Toast.makeText(
                        context,
                        "Spotify integration coming soon",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.GTAYellow)
            ) {
                Text("Connect")
            }
        }
    }
}
