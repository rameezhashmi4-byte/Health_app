package com.pushprime.ui.screens.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Collage Creator Screen
 * Create photo collages from before/after photos
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollageCreatorScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create Collage",
                        fontWeight = FontWeight.Bold
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
                Icon(
                    Icons.Default.AutoAwesomeMosaic,
                    contentDescription = "Collage",
                    modifier = Modifier.size(64.dp),
                    tint = PushPrimeColors.Primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Collage Creator",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select 2-6 photos from your Photo Vault to create a beautiful collage",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { /* TODO: Implement photo selection */ },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Photos")
                }
            }
        }
    }
}
