package com.pushprime.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PushPrime color palette
 * Hinge-inspired palette adapted for health & exercise
 */
object PushPrimeColors {
    // Light theme (Hinge-inspired)
    val Background = Color(0xFFF8F6F3)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1ECE8)

    val OnBackground = Color(0xFF1F1B1A)
    val OnSurface = Color(0xFF1F1B1A)
    val OnSurfaceVariant = Color(0xFF5A534F)

    // Primary accent - Warm rose/coral
    val Primary = Color(0xFFE65A5F)
    val PrimaryVariant = Color(0xFFD44F54)
    val PrimaryLight = Color(0xFFFFC2C5)

    // Secondary - Dark charcoal
    val Secondary = Color(0xFF1C1B1F)
    val SecondaryVariant = Color(0xFF2A2625)

    // Status colors
    val Error = Color(0xFFCC4A4A)
    val Success = Color(0xFF2E9F6F)
    val Warning = Color(0xFFD89B3A)

    // Borders
    val Outline = Color(0xFFE2DAD6)
    val OutlineVariant = Color(0xFFD4C8C3)

    // Dark theme
    val DarkBackground = Color(0xFF111111)
    val DarkSurface = Color(0xFF1A1A1A)
    val DarkSurfaceVariant = Color(0xFF242424)
    val DarkOnBackground = Color(0xFFF4F2EF)
    val DarkOnSurface = Color(0xFFF4F2EF)
    val DarkOnSurfaceVariant = Color(0xFFCFC6C1)
    val DarkPrimary = Color(0xFFFF6B6F)
    val DarkSecondary = Color(0xFFEAE0DB)
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
