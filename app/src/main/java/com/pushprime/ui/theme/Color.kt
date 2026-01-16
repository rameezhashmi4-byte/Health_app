package com.pushprime.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * PushPrime color palette
 * Hinge-inspired palette adapted for health & exercise
 */
object PushPrimeColors {
    // Uber-slick palette
    val UberBlack = Color(0xFF000000)
    val UberWhite = Color(0xFFFFFFFF)
    val UberGrey = Color(0xFFF6F6F6)
    val UberBlue = Color(0xFF276EF1)

    // GTA-inspired Gamification colors
    val GTAYellow = Color(0xFFFFD100) // Mission text
    val GTAGreen = Color(0xFF4CAF50) // Health
    val GTARed = Color(0xFFFF3B30) // Armor/Danger
    val GTAMapBackground = Color(0xFF2D2D2D)

    // Light theme (Uber-like)
    val Background = UberGrey
    val Surface = UberWhite
    val SurfaceVariant = Color(0xFFEEEEEE)

    val OnBackground = UberBlack
    val OnSurface = UberBlack
    val OnSurfaceVariant = Color(0xFF545454)

    // Primary accent - Sharp Black/White for Uber, but with GTA color for fun
    val Primary = UberBlack
    val OnPrimary = UberWhite
    val PrimaryVariant = Color(0xFF333333)
    val PrimaryLight = Color(0xFFE2E2E2)

    // Secondary - GTA Yellow for that gamified feel
    val Secondary = GTAYellow
    val SecondaryVariant = Color(0xFFFFE082)

    // Status colors
    val Error = GTARed
    val Success = GTAGreen
    val Warning = GTAYellow

    // Borders
    val Outline = Color(0xFFE2E2E2)
    val OutlineVariant = Color(0xFFD1D1D1)

    // Dark theme (GTA-like Night Mode)
    val DarkBackground = Color(0xFF0F0F0F)
    val DarkSurface = Color(0xFF1A1A1A)
    val DarkSurfaceVariant = Color(0xFF242424)
    val DarkOnBackground = UberWhite
    val DarkOnSurface = UberWhite
    val DarkOnSurfaceVariant = Color(0xFFB0B0B0)
    val DarkPrimary = UberWhite
    val DarkSecondary = GTAYellow
}

// Material 3 mapping
val Primary = PushPrimeColors.Primary
val PrimaryVariant = PushPrimeColors.PrimaryVariant
val Secondary = PushPrimeColors.Secondary
val Background = PushPrimeColors.Background
val Surface = PushPrimeColors.Surface
val Error = PushPrimeColors.Error
val OnPrimary = PushPrimeColors.OnPrimary
val OnSecondary = PushPrimeColors.UberBlack
val OnBackground = PushPrimeColors.OnBackground
val OnSurface = PushPrimeColors.OnSurface
