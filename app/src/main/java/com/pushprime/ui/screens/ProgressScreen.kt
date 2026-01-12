package com.pushprime.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.data.AppDatabase
import com.pushprime.data.SessionDao
import com.pushprime.ui.theme.PushPrimeColors
import androidx.compose.ui.platform.LocalContext

/**
 * Progress Screen
 * Hub with Overview/Calendar/Analytics tabs
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit,
    onDayClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val sessionDao = remember { database.sessionDao() }
    
    val pagerState = rememberPagerState(pageCount = { 3 })
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf("Overview", "Calendar", "Analytics")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Progress",
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
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = PushPrimeColors.Surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                        },
                        text = { Text(title) }
                    )
                }
            }
            
            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ProgressOverviewScreen(sessionDao = sessionDao)
                    1 -> CalendarScreen(
                        sessionDao = sessionDao,
                        onDayClick = onDayClick,
                        onNavigateBack = {} // No back button in tab
                    )
                    2 -> AnalyticsScreen(
                        sessionDao = sessionDao,
                        onNavigateBack = {} // No back button in tab
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressOverviewScreen(
    sessionDao: SessionDao,
    modifier: Modifier = Modifier
) {
    // Simplified overview - can be enhanced
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = "Progress Overview",
            style = MaterialTheme.typography.titleLarge
        )
    }
}
