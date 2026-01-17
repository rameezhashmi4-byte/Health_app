package com.pushprime.data.notifications

import android.content.Context

class TokenRepository(
    private val context: Context
) {
    suspend fun saveToken(userId: String?, token: String) {
        if (userId.isNullOrBlank()) return
        // Placeholder for future Firestore write:
        // users/{uid}/deviceTokens/{tokenId}
        // Fields: token, deviceModel, createdAt
    }
}
