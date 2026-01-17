package com.pushprime.ui.screens.progress_photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pushprime.ui.theme.PushPrimeColors
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareProgressPhotosScreen(
    beforeId: String,
    afterId: String,
    onNavigateBack: () -> Unit,
    viewModel: ProgressPhotoDetailViewModel = hiltViewModel()
) {
    var leftId by remember { mutableStateOf(beforeId) }
    var rightId by remember { mutableStateOf(afterId) }
    var sliderPosition by remember { mutableStateOf(0.5f) }

    val leftPhoto by viewModel.observePhoto(leftId).collectAsState(initial = null)
    val rightPhoto by viewModel.observePhoto(rightId).collectAsState(initial = null)

    val poseMismatch = leftPhoto != null && rightPhoto != null &&
        leftPhoto?.poseTag != rightPhoto?.poseTag

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Compare",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val temp = leftId
                        leftId = rightId
                        rightId = temp
                    }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Swap")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PushPrimeColors.Surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (poseMismatch) {
                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Pose tags don't match. Comparing anyway.",
                        modifier = Modifier.padding(12.dp),
                        color = Color(0xFF7A5B00)
                    )
                }
            }

            if (leftPhoto == null || rightPhoto == null) {
                Text(
                    text = "Loading photos...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PushPrimeColors.OnSurfaceVariant
                )
                return@Column
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val revealWidth = maxWidth * sliderPosition
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = leftPhoto?.localPath?.let { File(it) } ?: leftPhoto?.downloadUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(revealWidth)
                            .background(Color.Transparent)
                    ) {
                        AsyncImage(
                            model = rightPhoto?.localPath?.let { File(it) } ?: rightPhoto?.downloadUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    DateBadge(
                        label = formatFullDate(leftPhoto?.takenAt ?: 0L),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    )
                    DateBadge(
                        label = formatFullDate(rightPhoto?.takenAt ?: 0L),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    )
                }
            }

            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                valueRange = 0.05f..0.95f
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Button(
                    onClick = {
                        val temp = leftId
                        leftId = rightId
                        rightId = temp
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Swap",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBadge(label: String, modifier: Modifier = Modifier) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun formatFullDate(timestamp: Long): String {
    if (timestamp == 0L) return ""
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
    val localDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    return formatter.format(localDate)
}
