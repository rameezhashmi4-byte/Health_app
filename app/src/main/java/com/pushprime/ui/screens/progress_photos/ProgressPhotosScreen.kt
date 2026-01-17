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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pushprime.model.ProgressPhotoEntity
import com.pushprime.model.PoseTag
import com.pushprime.model.SyncStatus
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProgressPhotosScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToCompare: (String, String) -> Unit,
    onNavigateToCollage: () -> Unit,
    viewModel: ProgressPhotosViewModel = hiltViewModel()
) {
    val photos by viewModel.photos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val selectedIds = remember { mutableStateListOf<String>() }
    var menuPhotoId by remember { mutableStateOf<String?>(null) }

    val hasFailedUploads = photos.any { SyncStatus.from(it.syncStatus) == SyncStatus.FAILED }
    val galleryItems = remember(photos) { buildGalleryItems(photos) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress", fontWeight = FontWeight.Bold) },
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
        if (photos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection(
                    hasFailedUploads = hasFailedUploads,
                    onRetryUploads = {
                        viewModel.retryUploads()
                        scope.launch { snackbarHostState.showSnackbar("Retrying uploads") }
                    },
                    onAddPhoto = onNavigateToAdd,
                    onCreateCollage = onNavigateToCollage,
                    selectedCount = selectedIds.size,
                    onCompareSelected = {
                        if (selectedIds.size == 2) {
                            onNavigateToCompare(selectedIds[0], selectedIds[1])
                            selectedIds.clear()
                        }
                    },
                    onClearSelection = { selectedIds.clear() }
                )

                EmptyState(onAddPhoto = onNavigateToAdd)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    HeaderSection(
                        hasFailedUploads = hasFailedUploads,
                        onRetryUploads = {
                            viewModel.retryUploads()
                            scope.launch { snackbarHostState.showSnackbar("Retrying uploads") }
                        },
                        onAddPhoto = onNavigateToAdd,
                        onCreateCollage = onNavigateToCollage,
                        selectedCount = selectedIds.size,
                        onCompareSelected = {
                            if (selectedIds.size == 2) {
                                onNavigateToCompare(selectedIds[0], selectedIds[1])
                                selectedIds.clear()
                            }
                        },
                        onClearSelection = { selectedIds.clear() }
                    )
                }

                items(
                    items = galleryItems,
                    span = { item ->
                        if (item is GalleryItem.MonthHeader) GridItemSpan(3) else GridItemSpan(1)
                    }
                ) { item ->
                    when (item) {
                        is GalleryItem.MonthHeader -> {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        is GalleryItem.PhotoItem -> {
                            ProgressPhotoThumbnail(
                                photo = item.photo,
                                isSelected = selectedIds.contains(item.photo.photoId),
                                onClick = {
                                    toggleSelection(selectedIds, item.photo.photoId, snackbarHostState, scope)
                                },
                                onLongPress = {
                                    menuPhotoId = item.photo.photoId
                                }
                            )

                            if (menuPhotoId == item.photo.photoId) {
                                DropdownMenu(
                                    expanded = true,
                                    onDismissRequest = { menuPhotoId = null }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Compare") },
                                        onClick = {
                                            menuPhotoId = null
                                            if (selectedIds.isEmpty()) {
                                                selectedIds.add(item.photo.photoId)
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("Select one more photo to compare")
                                                }
                                            } else if (selectedIds.size == 1) {
                                                if (!selectedIds.contains(item.photo.photoId)) {
                                                    onNavigateToCompare(selectedIds[0], item.photo.photoId)
                                                    selectedIds.clear()
                                                }
                                            } else {
                                                onNavigateToCompare(selectedIds[0], selectedIds[1])
                                                selectedIds.clear()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.Red
                                            )
                                        },
                                        onClick = {
                                            menuPhotoId = null
                                            viewModel.deletePhoto(item.photo)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Photo deleted")
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share") },
                                        onClick = {
                                            menuPhotoId = null
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Share coming soon")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun HeaderSection(
    hasFailedUploads: Boolean,
    onRetryUploads: () -> Unit,
    onAddPhoto: () -> Unit,
    onCreateCollage: () -> Unit,
    selectedCount: Int,
    onCompareSelected: () -> Unit,
    onClearSelection: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Track your transformation 📸",
            style = MaterialTheme.typography.bodyMedium,
            color = PushPrimeColors.OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onAddPhoto,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Add Photo")
            }
            OutlinedButton(
                onClick = onCreateCollage,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Create Collage")
            }
        }
        if (hasFailedUploads) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Uploads failed. Retry when online.",
                style = MaterialTheme.typography.bodySmall,
                color = PushPrimeColors.OnSurfaceVariant
            )
            TextButton(onClick = onRetryUploads) {
                Text("Retry uploads")
            }
        }
        if (selectedCount > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCompareSelected,
                    enabled = selectedCount == 2,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Compare (${selectedCount}/2)")
                }
                OutlinedButton(onClick = onClearSelection, shape = RoundedCornerShape(12.dp)) {
                    Text("Clear")
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun EmptyState(onAddPhoto: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No photos yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add your first photo",
            style = MaterialTheme.typography.bodyMedium,
            color = PushPrimeColors.OnSurfaceVariant
        )
        Button(onClick = onAddPhoto, shape = RoundedCornerShape(12.dp)) {
            Text("Add your first photo")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProgressPhotoThumbnail(
    photo: ProgressPhotoEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val status = SyncStatus.from(photo.syncStatus)
    val poseTag = PoseTag.from(photo.poseTag)
    val dateLabel = formatDayLabel(photo.takenAt)
    val imageModel = photo.localPath?.let { File(it) } ?: photo.downloadUrl

    Card(
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
        ) {
            AsyncImage(
                model = imageModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = dateLabel,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
            Surface(
                color = PushPrimeColors.Primary.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                Text(
                    text = poseTag.displayName,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
            if (status == SyncStatus.PENDING) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            if (status == SyncStatus.FAILED) {
                Surface(
                    color = Color(0xCC000000),
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PushPrimeColors.Primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

private sealed class GalleryItem {
    data class MonthHeader(val label: String) : GalleryItem()
    data class PhotoItem(val photo: ProgressPhotoEntity) : GalleryItem()
}

private fun buildGalleryItems(photos: List<ProgressPhotoEntity>): List<GalleryItem> {
    val grouped = photos.groupBy { photo ->
        val month = Instant.ofEpochMilli(photo.takenAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        YearMonth.from(month)
    }
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    return grouped.entries.sortedByDescending { it.key }.flatMap { (month, monthPhotos) ->
        buildList {
            add(GalleryItem.MonthHeader(month.format(formatter)))
            monthPhotos.forEach { add(GalleryItem.PhotoItem(it)) }
        }
    }
}

private fun formatDayLabel(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
    val localDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return formatter.format(localDate)
}

private fun toggleSelection(
    selectedIds: MutableList<String>,
    photoId: String,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope
) {
    if (selectedIds.contains(photoId)) {
        selectedIds.remove(photoId)
        return
    }
    if (selectedIds.size >= 2) {
        scope.launch {
            snackbarHostState.showSnackbar("Select only 2 photos to compare")
        }
        return
    }
    selectedIds.add(photoId)
}
