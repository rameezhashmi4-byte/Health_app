package com.pushprime.network

import kotlinx.coroutines.delay

/**
 * Quote Service
 * Fetches daily motivational quotes from web or static fallback
 */
object QuoteService {
    // Static fallback quotes
    private val fallbackQuotes = listOf(
        Quote("The only bad workout is the one that didn't happen.", "Unknown"),
        Quote("Your body can stand almost anything. It's your mind you need to convince.", "Arnold Schwarzenegger"),
        Quote("Take care of your body. It's the only place you have to live.", "Jim Rohn"),
        Quote("The pain you feel today will be the strength you feel tomorrow.", "Unknown"),
        Quote("Success is the sum of small efforts repeated day in and day out.", "Robert Collier"),
        Quote("Don't stop when you're tired. Stop when you're done.", "Unknown"),
        Quote("Push yourself because no one else is going to do it for you.", "Unknown"),
        Quote("Strength doesn't come from what you can do. It comes from overcoming the things you once thought you couldn't.", "Rikki Rogers"),
        Quote("The difference between try and triumph is just a little umph!", "Marvin Phillips"),
        Quote("Every champion was once a contender who refused to give up.", "Rocky Balboa")
    )
    
    /**
     * Gets daily quote (web API with fallback)
     */
    suspend fun getDailyQuote(): Quote {
        return try {
            // In production: Call actual API
            // val response = quoteApi.getDailyQuote()
            // response.body() ?: getFallbackQuote()
            
            delay(300) // Simulate network delay
            getFallbackQuote()
        } catch (e: Exception) {
            getFallbackQuote()
        }
    }
    
    /**
     * Gets quote based on day (consistent daily quote)
     */
    private fun getFallbackQuote(): Quote {
        val dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        return fallbackQuotes[dayOfYear % fallbackQuotes.size]
    }
    
    data class Quote(
        val text: String,
        val author: String
    )
}
