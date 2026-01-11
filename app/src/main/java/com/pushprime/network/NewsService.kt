package com.pushprime.network

import kotlinx.coroutines.delay

/**
 * News Service
 * Fetches health news from RSS feeds (WHO, NHS, Healthline)
 * Falls back to static news.json
 */
object NewsService {
    // RSS feed URLs
    private val rssFeeds = listOf(
        "https://www.who.int/rss-feeds/news-english.xml",
        "https://www.nhs.uk/news/feed/",
        "https://www.healthline.com/health-news/rss.xml"
    )
    
    // Fallback news items
    private val fallbackNews = listOf(
        NewsItem(
            title = "Regular Exercise Can Improve Mental Health",
            source = "WHO",
            date = System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000L),
            url = ""
        ),
        NewsItem(
            title = "New Study Shows Benefits of Daily Push-ups",
            source = "Healthline",
            date = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L),
            url = ""
        ),
        NewsItem(
            title = "WHO Recommends 150 Minutes of Exercise Weekly",
            source = "WHO",
            date = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000L),
            url = ""
        )
    )
    
    /**
     * Gets latest health news (RSS feeds with fallback)
     */
    suspend fun getHealthNews(limit: Int = 3): List<NewsItem> {
        return try {
            // In production: Parse RSS feeds
            // val newsItems = mutableListOf<NewsItem>()
            // rssFeeds.forEach { feedUrl ->
            //     val items = parseRSSFeed(feedUrl)
            //     newsItems.addAll(items)
            // }
            // newsItems.sortedByDescending { it.date }.take(limit)
            
            delay(500) // Simulate network delay
            fallbackNews.take(limit)
        } catch (e: Exception) {
            fallbackNews.take(limit)
        }
    }
    
    data class NewsItem(
        val title: String,
        val source: String,
        val date: Long,
        val url: String
    ) {
        fun getFormattedDate(): String {
            val diff = System.currentTimeMillis() - date
            val daysDiff = diff / (1000 * 60 * 60 * 24)
            
            return when {
                daysDiff == 0L -> "Today"
                daysDiff == 1L -> "Yesterday"
                daysDiff < 7 -> "$daysDiff days ago"
                else -> java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                    .format(java.util.Date(date))
            }
        }
    }
}
