package com.pushprime.data.ai

import com.pushprime.data.ai.openai.OpenAiException

/**
 * User-friendly error messages for AI-related errors
 * Ensures no technical jargon reaches the user
 */
object AiErrorMessages {
    
    fun formatForUser(error: Throwable): String {
        return when (error) {
            is OpenAiException -> formatOpenAiError(error)
            is ValidationException -> error.message ?: "Invalid response format"
            else -> "Something went wrong. Please try again."
        }
    }

    fun formatForRetry(error: Throwable): String {
        val baseMessage = formatForUser(error)
        return "$baseMessage\n\nTap to retry."
    }

    private fun formatOpenAiError(error: OpenAiException): String {
        return when (error.httpCode) {
            400 -> "Invalid request. Please check your settings."
            401 -> "Invalid API key. Please update your settings."
            403 -> "Access denied. Please check your API key permissions."
            404 -> "Service not found. Please verify your base URL in settings."
            429 -> "Too many requests. Please wait a moment and try again."
            500, 502, 503 -> "Service temporarily unavailable. Please try again in a few minutes."
            -1 -> when {
                error.message?.contains("Network") == true -> 
                    "No internet connection. Please check your network."
                error.message?.contains("parse") == true -> 
                    "Received invalid response. Please try again."
                else -> "Connection error. Please try again."
            }
            else -> "Service error (${error.httpCode}). Please try again."
        }
    }

    fun getWorkoutGenerationErrorMessage(error: Throwable, hasApiKey: Boolean): String {
        return when {
            !hasApiKey -> "Please set up your OpenAI API key in settings to use AI-powered workout generation."
            error is OpenAiException && error.httpCode == 401 -> 
                "Your API key is invalid. Please check your settings."
            error is OpenAiException && error.httpCode == 429 -> 
                "Rate limit reached. Using offline workout templates instead."
            error is ValidationException -> 
                "Generated workout was invalid. Using offline templates instead."
            else -> 
                "Could not connect to AI service. Using offline templates instead."
        }
    }

    fun getCoachMessageErrorMessage(error: Throwable, hasApiKey: Boolean): String {
        return when {
            !hasApiKey -> "Please set up your OpenAI API key in settings."
            error is OpenAiException && error.httpCode == 401 -> 
                "Your API key is invalid."
            error is OpenAiException && error.httpCode == 429 -> 
                "Too many messages. Please wait a moment."
            else -> 
                "Coach is temporarily unavailable."
        }
    }
}

/**
 * Exception for validation errors
 */
class ValidationException(message: String) : Exception(message)

/**
 * Result wrapper with user-friendly error messages
 */
sealed class AiResult<out T> {
    data class Success<T>(val data: T) : AiResult<T>()
    data class Error(val userMessage: String, val technicalError: Throwable) : AiResult<Nothing>()
    
    companion object {
        fun <T> fromResult(result: Result<T>): AiResult<T> {
            return result.fold(
                onSuccess = { Success(it) },
                onFailure = { Error(AiErrorMessages.formatForUser(it), it) }
            )
        }
    }
}

/**
 * UI state for AI-powered features
 */
sealed class AiUiState<out T> {
    object Idle : AiUiState<Nothing>()
    object Loading : AiUiState<Nothing>()
    data class Success<T>(val data: T, val source: String? = null) : AiUiState<T>()
    data class Warning<T>(val data: T, val warningMessage: String) : AiUiState<T>()
    data class Error(val message: String, val canRetry: Boolean = true) : AiUiState<Nothing>()
}
