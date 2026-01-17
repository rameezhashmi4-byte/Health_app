package com.pushprime.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pushprime.R
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Spotify Login Screen
 * Connect to Spotify account
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyLoginScreen(
    onNavigateBack: () -> Unit,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Connect Spotify",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_spotify),
                    contentDescription = "Spotify",
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Connect to Spotify",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Play your favorite workout playlists and control music during your sessions",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PushPrimeColors.OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1DB954)
                    )
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Connect with Spotify",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "You'll be redirected to Spotify to authorize",
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
        }
    }
}
