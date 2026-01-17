package com.pushprime.ui.screens.progress_photos

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.pushprime.model.PoseTag
import com.pushprime.ui.components.AppTextField
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProgressPhotoScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddProgressPhotoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isSaving by viewModel.isSaving.collectAsState()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var poseTag by remember { mutableStateOf(PoseTag.FRONT) }
    var notes by remember { mutableStateOf("") }
    var takenAt by remember { mutableStateOf(System.currentTimeMillis()) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedUri = tempCameraUri
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempImageUri(context)
            tempCameraUri = uri
            takePictureLauncher.launch(uri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission denied. Use gallery instead.")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add Photo",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedUri == null) {
                Text(
                    text = "Choose a source",
                    style = MaterialTheme.typography.titleLarge
                )
                Button(
                    onClick = {
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            val uri = createTempImageUri(context)
                            tempCameraUri = uri
                            takePictureLauncher.launch(uri)
                        } else {
                            requestCameraPermission.launch(permission)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Take Photo",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                OutlinedButton(
                    onClick = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Pick from Gallery",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                AsyncImage(
                    model = selectedUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = "Pose tag",
                    style = MaterialTheme.typography.titleLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PoseTag.values().forEach { tag ->
                        FilterChip(
                            selected = poseTag == tag,
                            onClick = { poseTag = tag },
                            label = { Text(tag.displayName) }
                        )
                    }
                }

                Text(
                    text = "Date",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedButton(
                    onClick = {
                        val cal = Calendar.getInstance().apply { timeInMillis = takenAt }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val updated = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                }
                                takenAt = updated.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dateFormat.format(takenAt),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (optional)",
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        onClick = {
                            val uri = selectedUri ?: return@Button
                            scope.launch {
                                val result = viewModel.savePhoto(uri, poseTag, notes, takenAt)
                                if (result.isSuccess) {
                                    onNavigateBack()
                                } else {
                                    snackbarHostState.showSnackbar("Failed to save photo")
                                }
                            }
                        },
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun createTempImageUri(context: android.content.Context): Uri {
    val tempFile = File.createTempFile("progress_photo_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
