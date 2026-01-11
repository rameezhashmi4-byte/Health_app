package com.pushprime.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PushPrime color palette
 * Light theme: soft white background, dark navy text, mint/blue accents
 */
object PushPrimeColors {
    // Background colors - Soft white
    val Background = Color(0xFFFAFAFA)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)
    
    // Text colors - Dark navy
    val OnBackground = Color(0xFF1A1F36)
    val OnSurface = Color(0xFF1A1F36)
    val OnSurfaceVariant = Color(0xFF4A5568)
    
    // Primary accent - Mint/Blue
    val Primary = Color(0xFF00D4AA) // Mint green
    val PrimaryVariant = Color(0xFF00B894)
    val PrimaryLight = Color(0xFF7FFFD4)
    
    // Secondary - Blue
    val Secondary = Color(0xFF6366F1) // Indigo blue
    val SecondaryVariant = Color(0xFF4F46E5)
    
    // Status colors
    val Error = Color(0xFFEF4444)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    
    // Borders
    val Outline = Color(0xFFE2E8F0)
    val OutlineVariant = Color(0xFFCBD5E0)
}

// Material 3 mapping
val Primary = PushPrimeColors.Primary
val PrimaryVariant = PushPrimeColors.PrimaryVariant
val Secondary = PushPrimeColors.Secondary
val Background = PushPrimeColors.Background
val Surface = PushPrimeColors.Surface
val Error = PushPrimeColors.Error
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFFFFFFFF)
val OnBackground = PushPrimeColors.OnBackground
val OnSurface = PushPrimeColors.OnSurface
