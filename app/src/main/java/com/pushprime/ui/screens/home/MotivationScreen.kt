package com.pushprime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pushprime.network.NewsService
import com.pushprime.network.QuoteService
import com.pushprime.ui.components.QuoteCard
import com.pushprime.ui.theme.PushPrimeColors
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Motivation Screen
 * Daily quote and health news headlines
 */
@Composable
fun MotivationScreen(
    onNavigateBack: () -> Unit
) {
    var quote by remember { mutableStateOf<QuoteService.Quote?>(null) }
    var newsItems by remember { mutableStateOf<List<NewsService.NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    suspend fun loadContent() {
        isLoading = true
        errorMessage = null
        val quoteResult = withTimeoutOrNull(3000) { QuoteService.getDailyQuote() }
        val newsResult = withTimeoutOrNull(3000) { NewsService.getHealthNews(limit = 3) }
        quote = quoteResult ?: QuoteService.Quote("Stay consistent. Progress follows.", "PushPrime")
        newsItems = newsResult ?: emptyList()
        if (newsResult == null) {
            errorMessage = "Unable to load news right now."
        }
        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadContent()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PushPrimeColors.Background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Motivation",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PushPrimeColors.OnSurface
            )
        }
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PushPrimeColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = PushPrimeColors.Surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PushPrimeColors.OnSurfaceVariant
                                )
                                TextButton(onClick = { coroutineScope.launch { loadContent() } }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                // Daily Quote
                item {
                    if (quote != null) {
                        QuoteCard(quote = quote!!)
                    }
                }
                
                // Health News Section
                item {
                    Text(
                        text = "Health News",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PushPrimeColors.OnSurface,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(newsItems) { newsItem ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PushPrimeColors.Surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = newsItem.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PushPrimeColors.OnSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = newsItem.source,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PushPrimeColors.Primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = newsItem.getFormattedDate(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PushPrimeColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
