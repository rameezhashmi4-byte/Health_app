package com.pushprime.ui.screens.music

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pushprime.R
import com.pushprime.data.SpotifyHelper
import com.pushprime.music.MusicProviderManager
import com.pushprime.music.MusicSource
import com.pushprime.ui.theme.PushPrimeColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyConnectScreen(
    spotifyHelper: SpotifyHelper?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val isConnected = spotifyHelper?.isConnected?.collectAsState(initial = false)?.value ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Connect Spotify",
                        style = MaterialTheme.typography.headlineMedium
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
                        style = MaterialTheme.typography.titleLarge
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
            Image(
                painter = painterResource(id = R.drawable.ic_spotify),
                contentDescription = "Spotify",
                modifier = Modifier.size(96.dp)
            )
            Text(
                text = "Connect Spotify",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "RAMBOOST can control playback during workouts.",
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )
            Text(
                text = if (isConnected) {
                    "Connected with your Spotify account"
                } else {
                    "Login stays within Spotify and returns here"
                },
                style = MaterialTheme.typography.bodySmall,
                color = PushPrimeColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (spotifyHelper == null) {
                        Toast.makeText(
                            context,
                            "Spotify helper not available",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (isConnected) {
                        spotifyHelper.disconnect()
                        Toast.makeText(context, "Spotify disconnected", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (activity == null) {
                        Toast.makeText(
                            context,
                            "Unable to start Spotify login",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    MusicProviderManager.setSource(MusicSource.SPOTIFY)
                    spotifyHelper.connect(activity) { error ->
                        Toast.makeText(
                            context,
                            error.message ?: "Spotify connection failed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PushPrimeColors.GTAYellow)
            ) {
                Text(
                    text = if (isConnected) "Disconnect" else "Connect",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
