package com.pushprime.data

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor(
    @ApplicationContext context: Context
) {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value.toString())
            }
        }
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    object Events {
        const val WORKOUT_STARTED = "workout_started"
        const val WORKOUT_COMPLETED = "workout_completed"
        const val ACHIEVEMENT_UNLOCKED = "achievement_unlocked"
        const val SPOTIFY_CONNECTED = "spotify_connected"
        const val ENERGY_PRESET_CHANGED = "energy_preset_changed"
        const val CHALLENGE_JOINED = "challenge_joined"
    }

    object Params {
        const val WORKOUT_TYPE = "workout_type"
        const val PUSHUPS_COUNT = "pushups_count"
        const val DURATION = "duration"
        const val ACHIEVEMENT_ID = "achievement_id"
        const val PRESET_NAME = "preset_name"
        const val CHALLENGE_ID = "challenge_id"
    }
}
