package com.pushprime.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PushPrime color palette
 * Hinge-inspired palette adapted for health & exercise
 */
object PushPrimeColors {
    // Light theme (Slack-inspired)
    val Background = Color(0xFFF8F8F8)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF2F2F2)

    val OnBackground = Color(0xFF1D1C1D)
    val OnSurface = Color(0xFF1D1C1D)
    val OnSurfaceVariant = Color(0xFF5E5E5E)

    // Primary accent - Slack aubergine
    val Primary = Color(0xFF4A154B)
    val PrimaryVariant = Color(0xFF3E1240)
    val PrimaryLight = Color(0xFF7E4A88)

    // Secondary - deep charcoal
    val Secondary = Color(0xFF1D1C1D)
    val SecondaryVariant = Color(0xFF2A2A2A)

    // Status colors
    val Error = Color(0xFFD93025)
    val Success = Color(0xFF2EB67D)
    val Warning = Color(0xFFECB22E)

    // Borders
    val Outline = Color(0xFFE1E1E1)
    val OutlineVariant = Color(0xFFD5D5D5)

    // Dark theme
    val DarkBackground = Color(0xFF1A1D21)
    val DarkSurface = Color(0xFF222529)
    val DarkSurfaceVariant = Color(0xFF2B2F33)
    val DarkOnBackground = Color(0xFFEDEDED)
    val DarkOnSurface = Color(0xFFEDEDED)
    val DarkOnSurfaceVariant = Color(0xFFB3B3B3)
    val DarkPrimary = Color(0xFFAD7FC1)
    val DarkSecondary = Color(0xFFD5D5D5)
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
