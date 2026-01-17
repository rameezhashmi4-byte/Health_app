package com.pushprime.ui.screens.progress_photos

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pushprime.model.ProgressPhotoEntity
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProgressCollageScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit,
    viewModel: ProgressCollageViewModel = hiltViewModel()
) {
    val photos by viewModel.photos.collectAsState()
    val selectedIds = remember { mutableStateListOf<String>() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isSaving by viewModel.isSaving.collectAsState()
    val isFormValid = selectedIds.size == 4

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Collage", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        autoPickPhotos(photos, selectedIds)
                        scope.launch {
                            snackbarHostState.showSnackbar("Picked latest 4 photos")
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Auto Pick")
                }
                Button(
                    onClick = {
                        selectedIds.clear()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear")
                }
            }

            Text(
                text = "Select 4 photos (${selectedIds.size}/4)",
                style = MaterialTheme.typography.bodyMedium,
                color = PushPrimeColors.OnSurfaceVariant
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = true)
            ) {
                items(photos) { photo ->
                    CollagePhotoTile(
                        photo = photo,
                        isSelected = selectedIds.contains(photo.photoId),
                        selectionIndex = selectedIds.indexOf(photo.photoId) + 1,
                        onClick = {
                            toggleSelection(selectedIds, photo.photoId, snackbarHostState, scope)
                        }
                    )
                }
            }

            Button(
                onClick = {
                    if (selectedIds.size < 4) {
                        scope.launch { snackbarHostState.showSnackbar("Select 4 photos first") }
                        return@Button
                    }
                    val selected = photos.filter { selectedIds.contains(it.photoId) }.take(4)
                    scope.launch {
                        val result = viewModel.createCollage(selected)
                        if (result.isSuccess) {
                            val collageId = result.getOrNull()?.collageId
                            if (collageId != null) {
                                onNavigateToPreview(collageId)
                            }
                        } else {
                            snackbarHostState.showSnackbar("Failed to create collage")
                        }
                    }
                },
                enabled = isFormValid && !isSaving,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Generate Collage")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CollagePhotoTile(
    photo: ProgressPhotoEntity,
    isSelected: Boolean,
    selectionIndex: Int,
    onClick: () -> Unit
) {
    val imageModel = photo.localPath?.let { File(it) } ?: photo.downloadUrl
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .combinedClickable(onClick = onClick, onLongClick = {})
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PushPrimeColors.Primary
                    ) {
                        Text(
                            text = selectionIndex.toString(),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun autoPickPhotos(
    photos: List<ProgressPhotoEntity>,
    selectedIds: MutableList<String>
) {
    selectedIds.clear()
    val byPose = photos.groupBy { it.poseTag }
    val candidate = byPose.values
        .filter { it.size >= 4 }
        .maxByOrNull { group -> group.maxOf { it.takenAt } }
        ?: photos
    candidate.sortedByDescending { it.takenAt }.take(4).forEach { selectedIds.add(it.photoId) }
}

private fun toggleSelection(
    selectedIds: MutableList<String>,
    photoId: String,
    snackbarHostState: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope
) {
    if (selectedIds.contains(photoId)) {
        selectedIds.remove(photoId)
        return
    }
    if (selectedIds.size >= 4) {
        scope.launch {
            snackbarHostState.showSnackbar("You can select up to 4 photos")
        }
        return
    }
    selectedIds.add(photoId)
}
