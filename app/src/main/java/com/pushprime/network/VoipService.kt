package com.pushprime.network

import android.content.Context
import android.util.Log
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * VOIP Service
 * Handles Twilio Voice SDK for group workout sessions
 */
class VoipService(private val context: Context) {
    private val TAG = "VoipService"
    
    data class CallState(
        val isConnected: Boolean = false,
        val isMuted: Boolean = false,
        val participants: List<Participant> = emptyList(),
        val countdown: Int? = null
    )
    
    data class Participant(
        val id: String,
        val username: String,
        val pushups: Int = 0,
        val isLeading: Boolean = false
    )
    
    private val _callState = MutableStateFlow(CallState())
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    
    private var activeCall: Call? = null
    
    init {
        Voice.setLogLevel(Log.DEBUG)
    }
    
    /**
     * Join group session
     */
    fun joinSession(accessToken: String, roomName: String) {
        try {
            val connectOptions = ConnectOptions.Builder(accessToken)
                .setTo(roomName)
                .build()
            
            activeCall = Voice.connect(context, connectOptions, callListener())
            _callState.value = _callState.value.copy(isConnected = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining session", e)
        }
    }
    
    /**
     * Leave session
     */
    fun leaveSession() {
        activeCall?.disconnect()
        activeCall = null
        _callState.value = CallState()
    }
    
    /**
     * Toggle mute
     */
    fun toggleMute() {
        activeCall?.let { call ->
            val isMuted = !_callState.value.isMuted
            call.mute(isMuted)
            _callState.value = _callState.value.copy(isMuted = isMuted)
        }
    }
    
    /**
     * Start countdown (for synchronized start)
     */
    fun startCountdown(seconds: Int = 3) {
        // In production, broadcast countdown to all participants
        _callState.value = _callState.value.copy(countdown = seconds)
    }
    
    /**
     * Update participant data (stubbed with fake data for now)
     */
    fun updateParticipants(fakeData: List<Participant>) {
        _callState.value = _callState.value.copy(participants = fakeData)
    }
    
    private fun callListener(): Call.Listener {
        return object : Call.Listener {
            override fun onConnectFailure(call: Call, callException: CallException) {
                Log.e(TAG, "Call connect failure", callException)
                _callState.value = _callState.value.copy(isConnected = false)
            }
            
            override fun onRinging(call: Call) {
                Log.d(TAG, "Call ringing")
            }
            
            override fun onConnected(call: Call) {
                Log.d(TAG, "Call connected")
                _callState.value = _callState.value.copy(isConnected = true)
            }
            
            override fun onReconnecting(call: Call, callException: CallException) {
                Log.d(TAG, "Call reconnecting")
            }
            
            override fun onReconnected(call: Call) {
                Log.d(TAG, "Call reconnected")
            }
            
            override fun onDisconnected(call: Call, callException: CallException?) {
                Log.d(TAG, "Call disconnected")
                activeCall = null
                _callState.value = CallState()
            }
        }
    }
}
