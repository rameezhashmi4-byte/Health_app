package com.pushprime.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pushprime.data.notifications.TokenRepository

class RamboostFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val repository = TokenRepository(this)
        // Placeholder: userId lookup not wired yet.
        // repository.saveToken(userId, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Placeholder for future FCM handling.
    }
}
