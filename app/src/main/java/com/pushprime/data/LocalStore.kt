package com.pushprime.data

import android.content.Context
import android.content.SharedPreferences
import com.pushprime.model.Session
import com.pushprime.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Local data storage
 * Uses SharedPreferences for simple data and JSON for leaderboard
 */
class LocalStore(private val context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("PushPrimePrefs", Context.MODE_PRIVATE)
    
    private val _sessions = MutableStateFlow<List<Session>>(emptyList())
    val sessions: StateFlow<List<Session>> = _sessions.asStateFlow()
    
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    
    init {
        loadSessions()
        loadUser()
    }
    
    // User data
    fun saveUser(user: User) {
        _user.value = user
        prefs.edit().apply {
            putString("username", user.username)
            putInt("age", user.age)
            putString("gender", user.gender.name)
            putString("fitnessLevel", user.fitnessLevel.name)
            putInt("predictedMaxPushups", user.predictedMaxPushups)
            putInt("dailyGoal", user.dailyGoal)
            putString("country", user.country)
            apply()
        }
    }
    
    private fun loadUser() {
        val username = prefs.getString("username", "") ?: ""
        if (username.isNotEmpty()) {
            _user.value = User(
                username = username,
                age = prefs.getInt("age", 0),
                gender = User.Gender.valueOf(prefs.getString("gender", "MALE") ?: "MALE"),
                fitnessLevel = User.FitnessLevel.valueOf(
                    prefs.getString("fitnessLevel", "BEGINNER") ?: "BEGINNER"
                ),
                predictedMaxPushups = prefs.getInt("predictedMaxPushups", 0),
                dailyGoal = prefs.getInt("dailyGoal", 0),
                country = prefs.getString("country", "US") ?: "US"
            )
        }
    }
    
    // Sessions
    fun saveSession(session: Session) {
        val currentSessions = _sessions.value.toMutableList()
        currentSessions.add(0, session)
        _sessions.value = currentSessions
        saveSessionsToPrefs()
    }
    
    fun getTodaySessions(): List<Session> {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return _sessions.value.filter { it.date == today }
    }
    
    fun getTodayTotalPushups(): Int {
        return getTodaySessions().sumOf { it.pushups }
    }
    
    fun getTodayTotalTime(): Int {
        return getTodaySessions().sumOf { it.workoutTime }
    }
    
    fun getStreak(): Int {
        val sessions = _sessions.value.sortedByDescending { it.timestamp }
        if (sessions.isEmpty()) return 0
        
        var streak = 0
        val calendar = Calendar.getInstance()
        var currentDate = calendar.time
        
        for (session in sessions) {
            val sessionDate = Date(session.timestamp)
            val daysDiff = ((currentDate.time - sessionDate.time) / (1000 * 60 * 60 * 24)).toInt()
            
            if (daysDiff == streak) {
                streak++
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                currentDate = calendar.time
            } else if (daysDiff > streak) {
                break
            }
        }
        
        return streak
    }
    
    private fun loadSessions() {
        val sessionsJson = prefs.getString("sessions", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(sessionsJson)
            val sessions = mutableListOf<Session>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                sessions.add(
                    Session(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        pushups = obj.getInt("pushups"),
                        workoutTime = obj.getInt("workoutTime"),
                        timestamp = obj.getLong("timestamp"),
                        country = obj.getString("country"),
                        date = obj.getString("date")
                    )
                )
            }
            _sessions.value = sessions
        } catch (e: Exception) {
            _sessions.value = emptyList()
        }
    }
    
    private fun saveSessionsToPrefs() {
        val jsonArray = JSONArray()
        _sessions.value.forEach { session ->
            val obj = JSONObject().apply {
                put("id", session.id)
                put("username", session.username)
                put("pushups", session.pushups)
                put("workoutTime", session.workoutTime)
                put("timestamp", session.timestamp)
                put("country", session.country)
                put("date", session.date)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("sessions", jsonArray.toString()).apply()
    }
    
    // Leaderboard (local JSON)
    fun getLocalLeaderboard(): List<com.pushprime.model.LeaderboardEntry> {
        val leaderboardJson = prefs.getString("leaderboard", "[]") ?: "[]"
        val entries = mutableListOf<com.pushprime.model.LeaderboardEntry>()
        
        try {
            val jsonArray = JSONArray(leaderboardJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                entries.add(
                    com.pushprime.model.LeaderboardEntry(
                        rank = obj.getInt("rank"),
                        username = obj.getString("username"),
                        pushups = obj.getInt("pushups"),
                        workoutTime = obj.getInt("workoutTime"),
                        country = obj.getString("country"),
                        date = obj.getString("date"),
                        timestamp = obj.getLong("timestamp"),
                        isLocal = true
                    )
                )
            }
        } catch (e: Exception) {
            // Return empty list if parsing fails
        }
        
        return entries.sortedByDescending { it.pushups }
    }
    
    fun saveToLocalLeaderboard(session: Session) {
        val entries = getLocalLeaderboard().toMutableList()
        entries.add(
            com.pushprime.model.LeaderboardEntry(
                rank = entries.size + 1,
                username = session.username,
                pushups = session.pushups,
                workoutTime = session.workoutTime,
                country = session.country,
                date = session.date,
                timestamp = session.timestamp,
                isLocal = true
            )
        )
        
        // Keep top 100
        val topEntries = entries.sortedByDescending { it.pushups }.take(100)
        val jsonArray = JSONArray()
        topEntries.forEachIndexed { index, entry ->
            val obj = JSONObject().apply {
                put("rank", index + 1)
                put("username", entry.username)
                put("pushups", entry.pushups)
                put("workoutTime", entry.workoutTime)
                put("country", entry.country)
                put("date", entry.date)
                put("timestamp", entry.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("leaderboard", jsonArray.toString()).apply()
    }
}
