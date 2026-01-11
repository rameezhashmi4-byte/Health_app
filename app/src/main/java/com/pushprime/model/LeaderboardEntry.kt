package com.pushprime.model

/**
 * Leaderboard entry model
 * Used for both local and global leaderboards
 */
data class LeaderboardEntry(
    val rank: Int = 0,
    val username: String = "",
    val pushups: Int = 0,
    val workoutTime: Int = 0,
    val country: String = "US",
    val date: String = "",
    val timestamp: Long = 0L,
    val isLocal: Boolean = true
) {
    fun getCountryFlag(): String {
        // Simple emoji mapping (in production, use proper flag library)
        return when (country.uppercase()) {
            "US" -> "🇺🇸"
            "UK", "GB" -> "🇬🇧"
            "CA" -> "🇨🇦"
            "AU" -> "🇦🇺"
            "DE" -> "🇩🇪"
            "FR" -> "🇫🇷"
            "IT" -> "🇮🇹"
            "ES" -> "🇪🇸"
            "BR" -> "🇧🇷"
            "MX" -> "🇲🇽"
            "IN" -> "🇮🇳"
            "CN" -> "🇨🇳"
            "JP" -> "🇯🇵"
            "KR" -> "🇰🇷"
            else -> "🌍"
        }
    }
}
