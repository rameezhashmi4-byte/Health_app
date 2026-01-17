package com.pushprime.ui.screens.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pushprime.model.SessionEntity
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

enum class HistoryFilter(val label: String) {
    ALL("All"),
    WORKOUTS("Workouts"),
    SPORTS("Sports"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    HIGH_EFFORT("High Effort")
}

enum class HistoryItemType {
    WORKOUT,
    SPORTS,
    QUICK_SESSION
}

data class HistoryItem(
    val id: Long,
    val type: HistoryItemType,
    val title: String,
    val timestamp: Long,
    val durationMinutes: Int,
    val previewLine: String,
    val badge: String,
    val icon: String,
    val session: SessionEntity
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onOpenDetails: (Long, Boolean) -> Unit,
    onStartSession: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<HistoryItem?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val filteredItems by remember(sessions, selectedFilter, searchQuery) {
        derivedStateOf {
            sessions
                .map { it.toHistoryItem() }
                .filter { item ->
                    matchesFilter(item.session, selectedFilter) &&
                        matchesQuery(item, searchQuery)
                }
                .sortedByDescending { it.timestamp }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your progress over time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Optional search action */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Optional filter action */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search sessions") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryFilter.values()) { filter ->
                    val selected = filter == selectedFilter
                    FilterChip(
                        selected = selected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.Black)
                    }
                }
                filteredItems.isEmpty() -> {
                    EmptyHistoryState(onStartSession = onStartSession)
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredItems) { item ->
                            SessionCard(
                                item = item,
                                onClick = { onOpenDetails(item.id, false) },
                                onLongClick = {
                                    selectedItem = item
                                    showSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSheet && selectedItem != null) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = selectedItem?.title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = {
                        val item = selectedItem
                        showSheet = false
                        if (item != null) {
                            onOpenDetails(item.id, true)
                        }
                    }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                TextButton(
                    onClick = {
                        showSheet = false
                        showDeleteConfirm = true
                    }
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF3B30))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", color = Color(0xFFFF3B30))
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDeleteConfirm && selectedItem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete session?") },
            text = { Text("This will remove the session permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedItem?.session?.let { viewModel.deleteSession(it) }
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SessionCard(
    item: HistoryItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF6F6F6),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.icon,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.previewLine, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(item.badge) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color(0xFFE0E0E0))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = buildDateLine(item.timestamp, item.durationMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun EmptyHistoryState(onStartSession: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No sessions yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start your first session",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStartSession,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Start your first session")
        }
    }
}

private fun SessionEntity.toHistoryItem(): HistoryItem {
    val type = when {
        activityType.equals("SPORT", ignoreCase = true) -> HistoryItemType.SPORTS
        activityType.equals("QUICK_SESSION", ignoreCase = true) -> HistoryItemType.QUICK_SESSION
        else -> HistoryItemType.WORKOUT
    }
    val title = when (type) {
        HistoryItemType.WORKOUT -> {
            val base = exerciseId?.takeIf { it.isNotBlank() } ?: "Workout"
            "$base Workout"
        }
        HistoryItemType.SPORTS -> {
            val sport = sportType?.takeIf { it.isNotBlank() } ?: "Sports"
            "$sport Session"
        }
        HistoryItemType.QUICK_SESSION -> {
            val base = exerciseId?.takeIf { it.isNotBlank() } ?: "Quick"
            "$base Quick Session"
        }
    }
    val durationMinutes = (getDurationSeconds() / 60).coerceAtLeast(1)
    val previewLine = when (type) {
        HistoryItemType.WORKOUT -> totalReps?.let { "$it reps" } ?: "Logged"
        HistoryItemType.SPORTS -> {
            val effort = intensity.replaceFirstChar { it.uppercaseChar() }
            val ratingPart = rating?.let { " • $it/5" } ?: ""
            "$effort effort$ratingPart"
        }
        HistoryItemType.QUICK_SESSION -> "${durationMinutes} min quick session"
    }
    return HistoryItem(
        id = id,
        type = type,
        title = title,
        timestamp = startTime,
        durationMinutes = durationMinutes,
        previewLine = previewLine,
        badge = when (type) {
            HistoryItemType.WORKOUT -> "Workout"
            HistoryItemType.SPORTS -> "Sports"
            HistoryItemType.QUICK_SESSION -> "Quick Session"
        },
        icon = resolveIcon(type, sportType),
        session = this
    )
}

private fun matchesFilter(session: SessionEntity, filter: HistoryFilter): Boolean {
    val type = when {
        session.activityType.equals("SPORT", ignoreCase = true) -> HistoryItemType.SPORTS
        session.activityType.equals("QUICK_SESSION", ignoreCase = true) -> HistoryItemType.QUICK_SESSION
        else -> HistoryItemType.WORKOUT
    }
    return when (filter) {
        HistoryFilter.ALL -> true
        HistoryFilter.WORKOUTS -> type == HistoryItemType.WORKOUT || type == HistoryItemType.QUICK_SESSION
        HistoryFilter.SPORTS -> type == HistoryItemType.SPORTS
        HistoryFilter.THIS_WEEK -> session.date >= startOfWeek()
        HistoryFilter.THIS_MONTH -> session.date >= startOfMonth()
        HistoryFilter.HIGH_EFFORT -> type == HistoryItemType.SPORTS &&
            session.intensity.equals("HIGH", ignoreCase = true)
    }
}

private fun matchesQuery(item: HistoryItem, query: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    val target = "${item.title} ${item.session.notes.orEmpty()}".lowercase(Locale.getDefault())
    return target.contains(trimmed.lowercase(Locale.getDefault()))
}

private fun startOfWeek(): String {
    val now = LocalDate.now()
    val monday = now.minusDays(((now.dayOfWeek.value + 6) % 7).toLong())
    return monday.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

private fun startOfMonth(): String {
    val now = LocalDate.now()
    return now.withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
}

private fun resolveIcon(type: HistoryItemType, sportType: String?): String {
    if (type == HistoryItemType.WORKOUT) return "🏋️"
    if (type == HistoryItemType.QUICK_SESSION) return "⚡"
    return when (sportType?.lowercase(Locale.getDefault())) {
        "squash" -> "🥎"
        "football" -> "⚽"
        "cricket" -> "🏏"
        "rugby" -> "🏉"
        "basketball" -> "🏀"
        "tennis" -> "🎾"
        "running" -> "🏃"
        "cycling" -> "🚴"
        "swimming" -> "🏊"
        "boxing" -> "🥊"
        else -> "⚽"
    }
}

private fun buildDateLine(timestamp: Long, durationMinutes: Int): String {
    val dateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())
    val dateLabel = dateFormat.format(Date(timestamp))
    return "$dateLabel • $durationMinutes mins"
}
