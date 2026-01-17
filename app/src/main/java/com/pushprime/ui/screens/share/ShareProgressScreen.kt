package com.pushprime.ui.screens.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pushprime.data.AppDatabase
import com.pushprime.data.LocalStore
import com.pushprime.data.SessionDao
import com.pushprime.data.StepsRepository
import com.pushprime.data.calculateStreak
import com.pushprime.model.PhotoEntryEntity
import com.pushprime.model.PhotoType
import com.pushprime.ui.screens.common.ErrorScreen
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareProgressScreen(
    localStore: LocalStore,
    sessionDao: SessionDao?,
    currentUserId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (sessionDao == null) {
        ErrorScreen(message = "Database not available")
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val stepsRepository = remember { StepsRepository(context) }
    val database = remember { AppDatabase.getDatabase(context) }
    val photoDao = remember { database.photoEntryDao() }

    val user by localStore.user.collectAsState(initial = null)
    val profile by localStore.profile.collectAsState(initial = null)
    val stepsEnabled by localStore.stepTrackingEnabled.collectAsState()
    val sessions by sessionDao.getAllSessions().collectAsState(initial = emptyList())

    val beforePhotos by photoDao.getPhotosByType(PhotoType.BEFORE.name)
        .collectAsState(initial = emptyList())
    val afterPhotos by photoDao.getPhotosByType(PhotoType.AFTER.name)
        .collectAsState(initial = emptyList())

    var selectedTemplate by remember { mutableStateOf(ShareTemplate.MINIMAL) }
    var selectedBefore by remember { mutableStateOf<PhotoEntryEntity?>(null) }
    var selectedAfter by remember { mutableStateOf<PhotoEntryEntity?>(null) }
    var showBeforePicker by remember { mutableStateOf(false) }
    var showAfterPicker by remember { mutableStateOf(false) }
    var stepsToday by remember { mutableStateOf<Int?>(null) }
    var latestBadge by remember { mutableStateOf<ShareBadge?>(null) }
    var isSharing by remember { mutableStateOf(false) }
    var saveToCloud by remember { mutableStateOf(false) }

    val streakDays = remember(sessions) { calculateStreak(sessions) }
    val sessionsThisWeek = remember(sessions) { countSessionsThisWeek(sessions) }
    val displayName = profile?.fullName?.ifBlank { null } ?: user?.username ?: "RAMBOOST User"
    val goalText = formatGoal(profile?.goal?.name ?: user?.dailyGoal?.let { "Daily goal $it" } ?: "Get stronger")
    val initials = initialsFromName(displayName)

    val hasAnyPhotos = beforePhotos.isNotEmpty() || afterPhotos.isNotEmpty()
    val canUseBeforeAfter = selectedBefore != null && selectedAfter != null
    val effectiveTemplate = if (selectedTemplate == ShareTemplate.BEFORE_AFTER && !canUseBeforeAfter) {
        ShareTemplate.STATS_ONLY
    } else {
        selectedTemplate
    }

    LaunchedEffect(beforePhotos) {
        if (selectedBefore == null) {
            selectedBefore = selectDefaultPhoto(beforePhotos, preferOldest = true)
        }
    }

    LaunchedEffect(afterPhotos) {
        if (selectedAfter == null) {
            selectedAfter = selectDefaultPhoto(afterPhotos, preferOldest = false)
        }
    }

    LaunchedEffect(stepsEnabled) {
        if (stepsEnabled) {
            stepsToday = withContext(Dispatchers.IO) {
                runCatching { stepsRepository.getTodaySteps().toInt() }.getOrDefault(0)
            }
        } else {
            stepsToday = null
        }
    }

    LaunchedEffect(Unit) {
        latestBadge = selectLatestBadge(localStore.getUnlockedAchievements())
    }

    LaunchedEffect(hasAnyPhotos) {
        if (!hasAnyPhotos && selectedTemplate == ShareTemplate.BEFORE_AFTER) {
            selectedTemplate = ShareTemplate.STATS_ONLY
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Progress", fontWeight = FontWeight.Bold) },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Share Template",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShareTemplate.values().forEach { template ->
                    val enabled = template != ShareTemplate.BEFORE_AFTER || hasAnyPhotos
                    FilterChip(
                        selected = selectedTemplate == template,
                        onClick = { if (enabled) selectedTemplate = template },
                        label = { Text(template.displayName) },
                        enabled = enabled
                    )
                }
            }

            ShareProgressPreviewCard(
                template = effectiveTemplate,
                name = displayName,
                goal = goalText,
                avatarInitials = initials,
                streakDays = streakDays,
                sessionsThisWeek = sessionsThisWeek,
                stepsToday = stepsToday,
                beforePhoto = selectedBefore,
                afterPhoto = selectedAfter,
                latestBadge = latestBadge
            )

            PhotoSelectionSection(
                beforePhoto = selectedBefore,
                afterPhoto = selectedAfter,
                onPickBefore = { showBeforePicker = true },
                onPickAfter = { showAfterPicker = true },
                onClearBefore = { selectedBefore = null },
                onClearAfter = { selectedAfter = null }
            )

            if (!hasAnyPhotos) {
                Text(
                    text = "No progress photos yet. Sharing will use stats only.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            } else if (!canUseBeforeAfter) {
                Text(
                    text = "Pick both before and after photos to enable Before/After.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }

            CloudSaveRow(
                enabled = saveToCloud,
                onEnabledChange = { saveToCloud = it },
                isAvailable = currentUserId != null
            )

            Button(
                onClick = {
                    scope.launch {
                        isSharing = true
                        try {
                            val shareId = UUID.randomUUID().toString()
                            val data = withContext(Dispatchers.IO) {
                                buildShareData(
                                    context = context,
                                    name = displayName,
                                    goal = goalText,
                                    initials = initials,
                                    streakDays = streakDays,
                                    sessionsThisWeek = sessionsThisWeek,
                                    stepsToday = stepsToday,
                                    badge = latestBadge,
                                    before = selectedBefore,
                                    after = selectedAfter
                                )
                            }
                            val renderTemplate = if (
                                effectiveTemplate == ShareTemplate.BEFORE_AFTER &&
                                (data.beforePhoto?.bitmap == null || data.afterPhoto?.bitmap == null)
                            ) {
                                ShareTemplate.STATS_ONLY
                            } else {
                                effectiveTemplate
                            }
                            val bitmap = withContext(Dispatchers.Default) {
                                generateShareBitmap(renderTemplate, data)
                            }
                            val file = withContext(Dispatchers.IO) {
                                saveShareBitmap(context, bitmap, shareId)
                            }
                            shareImage(context, file)
                            if (saveToCloud && currentUserId != null) {
                                uploadShareCard(
                                    file = file,
                                    uid = currentUserId,
                                    shareId = shareId,
                                    template = renderTemplate
                                )
                            } else if (saveToCloud) {
                                snackbarHostState.showSnackbar("Sign in to save to cloud.")
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Unable to share right now.")
                        } finally {
                            isSharing = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSharing
            ) {
                if (isSharing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                } else {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Share")
            }
        }
    }

    if (showBeforePicker) {
        PhotoPickerDialog(
            title = "Pick Before Photo",
            photos = beforePhotos,
            onSelect = {
                selectedBefore = it
                showBeforePicker = false
            },
            onDismiss = { showBeforePicker = false }
        )
    }

    if (showAfterPicker) {
        PhotoPickerDialog(
            title = "Pick After Photo",
            photos = afterPhotos,
            onSelect = {
                selectedAfter = it
                showAfterPicker = false
            },
            onDismiss = { showAfterPicker = false }
        )
    }
}

@Composable
private fun ShareProgressPreviewCard(
    template: ShareTemplate,
    name: String,
    goal: String,
    avatarInitials: String,
    streakDays: Int,
    sessionsThisWeek: Int,
    stepsToday: Int?,
    beforePhoto: PhotoEntryEntity?,
    afterPhoto: PhotoEntryEntity?,
    latestBadge: ShareBadge?
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D0D0D), Color(0xFF1E1E1E))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = avatarInitials,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = goal,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }

                when (template) {
                    ShareTemplate.BEFORE_AFTER -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PreviewPhotoCard(beforePhoto, beforePhoto?.getFormattedDate())
                            PreviewPhotoCard(afterPhoto, afterPhoto?.getFormattedDate())
                        }
                    }
                    ShareTemplate.BADGE_STATS -> {
                        latestBadge?.let { badge ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                                shape = RoundedCornerShape(30.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = badge.icon,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = badge.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    ShareTemplate.MINIMAL,
                    ShareTemplate.STATS_ONLY -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color(0xFF333333))
                        )
                    }
                }

                ShareStatsRow(
                    streakDays = streakDays,
                    sessionsThisWeek = sessionsThisWeek,
                    stepsToday = stepsToday
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = "RAMBOOST",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xAAFFFFFF),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ShareStatsRow(
    streakDays: Int,
    sessionsThisWeek: Int,
    stepsToday: Int?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatTile(icon = "🔥", value = "$streakDays", label = "Streak")
        StatTile(icon = "🏋️", value = "$sessionsThisWeek", label = "Sessions")
        StatTile(icon = "👣", value = stepsToday?.toString() ?: "—", label = "Steps")
    }
}

@Composable
private fun StatTile(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFBBBBBB)
        )
    }
}

@Composable
private fun PreviewPhotoCard(photo: PhotoEntryEntity?, label: String?) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        if (photo == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .background(Color(0xFF1F1F1F)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color(0xFF666666)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
            ) {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .background(Color(0x99000000), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoSelectionSection(
    beforePhoto: PhotoEntryEntity?,
    afterPhoto: PhotoEntryEntity?,
    onPickBefore: () -> Unit,
    onPickAfter: () -> Unit,
    onClearBefore: () -> Unit,
    onClearAfter: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Before/After Photos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        PhotoSelectionRow(
            label = "Before Photo",
            photo = beforePhoto,
            onPick = onPickBefore,
            onClear = onClearBefore
        )
        PhotoSelectionRow(
            label = "After Photo",
            photo = afterPhoto,
            onPick = onPickAfter,
            onClear = onClearAfter
        )
    }
}

@Composable
private fun PhotoSelectionRow(
    label: String,
    photo: PhotoEntryEntity?,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = photo?.getFormattedDate() ?: "Not selected",
                style = MaterialTheme.typography.bodySmall,
                color = PushPrimeColors.OnSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPick) {
                Text(if (photo == null) "Pick" else "Change")
            }
            if (photo != null) {
                OutlinedButton(onClick = onClear) { Text("Clear") }
            }
        }
    }
}

@Composable
private fun PhotoPickerDialog(
    title: String,
    photos: List<PhotoEntryEntity>,
    onSelect: (PhotoEntryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                if (photos.isEmpty()) {
                    Text(
                        text = "No photos available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PushPrimeColors.OnSurfaceVariant
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(photos) { photo ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelect(photo) }
                            ) {
                                AsyncImage(
                                    model = photo.uri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun CloudSaveRow(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    isAvailable: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PushPrimeColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Save share card to cloud",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isAvailable) "Optional backup to Firebase" else "Sign in to enable cloud save",
                    style = MaterialTheme.typography.bodySmall,
                    color = PushPrimeColors.OnSurfaceVariant
                )
            }
            Switch(
                checked = enabled && isAvailable,
                onCheckedChange = { onEnabledChange(it) },
                enabled = isAvailable
            )
        }
    }
}

private fun buildShareData(
    context: Context,
    name: String,
    goal: String,
    initials: String,
    streakDays: Int,
    sessionsThisWeek: Int,
    stepsToday: Int?,
    badge: ShareBadge?,
    before: PhotoEntryEntity?,
    after: PhotoEntryEntity?
): ShareProgressData {
    return ShareProgressData(
        name = name,
        goal = goal,
        avatarInitials = initials,
        streakDays = streakDays,
        sessionsThisWeek = sessionsThisWeek,
        stepsToday = stepsToday,
        badge = badge,
        beforePhoto = before?.let {
            SharePhoto(
                bitmap = loadBitmapFromUri(context, it.uri),
                dateLabel = it.getFormattedDate()
            )
        },
        afterPhoto = after?.let {
            SharePhoto(
                bitmap = loadBitmapFromUri(context, it.uri),
                dateLabel = it.getFormattedDate()
            )
        }
    )
}

private fun saveShareBitmap(context: Context, bitmap: Bitmap, shareId: String): File {
    val dir = File(context.cacheDir, "share_cards")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    val file = File(dir, "$shareId.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}

private fun shareImage(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share progress"))
}

private suspend fun uploadShareCard(
    file: File,
    uid: String,
    shareId: String,
    template: ShareTemplate
) {
    val storage = try {
        FirebaseStorage.getInstance()
    } catch (_: Exception) {
        return
    }
    val firestore = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        return
    }
    val storagePath = "sharedProgress/$uid/$shareId.png"
    val ref = storage.reference.child(storagePath)
    ref.putFile(Uri.fromFile(file)).await()
    val downloadUrl = ref.downloadUrl.await().toString()
    val data = mapOf(
        "shareId" to shareId,
        "template" to template.displayName,
        "createdAt" to System.currentTimeMillis(),
        "storagePath" to storagePath,
        "downloadUrl" to downloadUrl
    )
    firestore.collection("users")
        .document(uid)
        .collection("sharedProgress")
        .document(shareId)
        .set(data)
        .await()
}

private fun loadBitmapFromUri(context: Context, uriString: String): Bitmap? {
    return try {
        val uri = Uri.parse(uriString)
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openInputStream(resolver, uri, uriString)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }
        val sampleSize = calculateInSampleSize(bounds, 1080, 1080)
        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        openInputStream(resolver, uri, uriString)?.use { input ->
            BitmapFactory.decodeStream(input, null, opts)
        }
    } catch (_: Exception) {
        null
    }
}

private fun openInputStream(
    resolver: android.content.ContentResolver,
    uri: Uri,
    rawPath: String
): java.io.InputStream? {
    return if (uri.scheme == null) {
        runCatching { java.io.FileInputStream(java.io.File(rawPath)) }.getOrNull()
    } else {
        resolver.openInputStream(uri)
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        var halfHeight = height / 2
        var halfWidth = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

private fun initialsFromName(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    val first = parts.getOrNull(0)?.take(1).orEmpty()
    val second = parts.getOrNull(1)?.take(1).orEmpty()
    val initials = (first + second).ifBlank { "U" }
    return initials.uppercase(Locale.getDefault())
}

private fun formatGoal(rawGoal: String): String {
    val cleaned = rawGoal.replace("_", " ").lowercase(Locale.getDefault())
    return cleaned.replaceFirstChar { it.uppercase(Locale.getDefault()) }
}

private fun selectDefaultPhoto(
    photos: List<PhotoEntryEntity>,
    preferOldest: Boolean
): PhotoEntryEntity? {
    if (photos.isEmpty()) return null
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val monthStart = calendar.timeInMillis
    calendar.add(Calendar.MONTH, 1)
    val monthEnd = calendar.timeInMillis - 1

    val inMonth = photos.filter { it.timestamp in monthStart..monthEnd }
    val pool = if (inMonth.isNotEmpty()) inMonth else photos
    return if (preferOldest) {
        pool.minByOrNull { it.timestamp }
    } else {
        pool.maxByOrNull { it.timestamp }
    }
}

private fun countSessionsThisWeek(sessions: List<com.pushprime.model.SessionEntity>): Int {
    if (sessions.isEmpty()) return 0
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val start = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, 6)
    val end = calendar.time
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startKey = formatter.format(start)
    val endKey = formatter.format(end)
    return sessions.count { it.date >= startKey && it.date <= endKey }
}

private fun selectLatestBadge(unlocked: Set<String>): ShareBadge? {
    if (unlocked.isEmpty()) return null
    val sorted = unlocked.mapNotNull { id ->
        val badge = mapAchievementIdToBadge(id) ?: return@mapNotNull null
        val score = extractBadgeScore(id)
        Pair(score, badge)
    }.sortedByDescending { it.first }
    return sorted.firstOrNull()?.second
}

private fun mapAchievementIdToBadge(id: String): ShareBadge? {
    return when {
        id.startsWith("streak_") -> ShareBadge(icon = "🔥", label = "${id.removePrefix("streak_")}-Day Streak")
        id.startsWith("sessions_") -> ShareBadge(icon = "💪", label = "${id.removePrefix("sessions_")} Sessions")
        id.startsWith("sports_") -> ShareBadge(icon = "🏅", label = "Sports Milestone")
        id.startsWith("steps_") -> ShareBadge(icon = "👟", label = "Step Goal")
        else -> ShareBadge(icon = "🏆", label = "Achievement Unlocked")
    }
}

private fun extractBadgeScore(id: String): Int {
    return id.filter { it.isDigit() }.toIntOrNull() ?: 0
}
