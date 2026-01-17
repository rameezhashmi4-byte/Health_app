package com.pushprime.ui.screens.media

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.pushprime.data.AppDatabase
import com.pushprime.data.PhotoEntryDao
import com.pushprime.model.PhotoType
import com.pushprime.ui.theme.PushPrimeColors

/**
 * Photo Vault Screen
 * Before/After gallery with tabs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoVaultScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCollageCreator: () -> Unit,
    onNavigateToShareProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val photoDao = remember { database.photoEntryDao() }
    
    var selectedTab by remember { mutableStateOf<PhotoType>(PhotoType.BEFORE) }
    var selectedPhotoUri by remember { mutableStateOf<String?>(null) }
    
    val beforePhotos by photoDao.getPhotosByType(PhotoType.BEFORE.name)
        .collectAsState(initial = emptyList())
    val afterPhotos by photoDao.getPhotosByType(PhotoType.AFTER.name)
        .collectAsState(initial = emptyList())
    
    val currentPhotos = remember(selectedTab) {
        if (selectedTab == PhotoType.BEFORE) beforePhotos else afterPhotos
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Before/After",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToShareProgress) {
                        Icon(Icons.Default.Share, contentDescription = "Share Progress")
                    }
                    IconButton(onClick = onNavigateToCollageCreator) {
                        Icon(Icons.Default.Add, contentDescription = "Create Collage")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // TODO: Open photo picker
                },
                containerColor = PushPrimeColors.Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Photo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = if (selectedTab == PhotoType.BEFORE) 0 else 1,
                containerColor = PushPrimeColors.Surface
            ) {
                Tab(
                    selected = selectedTab == PhotoType.BEFORE,
                    onClick = { selectedTab = PhotoType.BEFORE },
                    text = { Text("Before") }
                )
                Tab(
                    selected = selectedTab == PhotoType.AFTER,
                    onClick = { selectedTab = PhotoType.AFTER },
                    text = { Text("After") }
                )
            }
            
            // Photo Grid
            if (currentPhotos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Photo,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PushPrimeColors.OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No ${selectedTab.displayName.lowercase()} photos yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PushPrimeColors.OnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(currentPhotos) { photo ->
                        PhotoGridItem(
                            photoUri = photo.uri,
                            onClick = { selectedPhotoUri = photo.uri }
                        )
                    }
                }
            }
        }
    }
    
    // Photo detail dialog
    selectedPhotoUri?.let { uri ->
        Dialog(onDismissRequest = { selectedPhotoUri = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun PhotoGridItem(
    photoUri: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
