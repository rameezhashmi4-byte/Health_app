package com.pushprime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
 * PushPrime color palette
 * Hinge-inspired palette adapted for health & exercise
/**
object PushPrimeColors {
    // Uber-slick palette
    val UberBlack = Color(0xFF000000)
    val UberWhite = Color(0xFFFFFFFF)
    val UberGrey = Color(0xFFF6F6F6)
    val UberBlue = Color(0xFF276EF1)
        val OnPrimary = Color(0xFFFFFFFF)
    // GTA-inspired Gamification colors
    val GTAYellow = Color(0xFFFFD100) // Mission text
    val GTAGreen = Color(0xFF4CAF50) // Health
    val GTARed = Color(0xFFFF3B30) // Armor/Danger
    val GTAMapBackground = Color(0xFF2D2D2D)
        val OnSecondary = Color(0xFFFFFFFF)
    // Light theme (Uber-like)
    val Background = UberGrey
    val Surface = UberWhite
    val SurfaceVariant = Color(0xFFEEEEEE)
        val OnTertiary = Color(0xFFFFFFFF)
    val OnBackground = UberBlack
    val OnSurface = UberBlack
    val OnSurfaceVariant = Color(0xFF545454)
        val OnSurface = Color(0xFF111114)
    // Primary accent - Sharp Black/White for Uber, but with GTA color for fun
    val Primary = UberBlack
    val OnPrimary = UberWhite
    val PrimaryVariant = Color(0xFF333333)
    val PrimaryLight = Color(0xFFE2E2E2)
        val OnPrimaryContainer = Color(0xFFFFDADC)
    // Secondary - GTA Yellow for that gamified feel
    val Secondary = GTAYellow
    val SecondaryVariant = Color(0xFFFFE082)
        val OnSecondaryContainer = Color(0xFFE5E7EB)
    // Status colors
    val Error = GTARed
    val Success = GTAGreen
    val Warning = GTAYellow
        val Background = Color(0xFF121315)
    // Borders
    val Outline = Color(0xFFE2E2E2)
    val OutlineVariant = Color(0xFFD1D1D1)
        val OnSurface = Color(0xFFEFEFF1)
    // Dark theme (GTA-like Night Mode)
    val DarkBackground = Color(0xFF0F0F0F)
    val DarkSurface = Color(0xFF1A1A1A)
    val DarkSurfaceVariant = Color(0xFF242424)
    val DarkOnBackground = UberWhite
    val DarkOnSurface = UberWhite
    val DarkOnSurfaceVariant = Color(0xFFB0B0B0)
    val DarkPrimary = UberWhite
    val DarkSecondary = GTAYellow

        val Error = Color(0xFFFFB4AB)
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

        val OnError = Color(0xFF690005)
        val ErrorContainer = Color(0xFF93000A)
        val OnErrorContainer = Color(0xFFFFDAD6)

        val Success = Color(0xFF81C784)
        val Warning = Color(0xFFFFD54F)
    }
}

@Deprecated("Use RamboostColors or MaterialTheme.colorScheme")
object PushPrimeColors {
    val Primary: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val OnPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimary
    val PrimaryVariant: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val PrimaryLight: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer

    val Secondary: Color
        @Composable get() = MaterialTheme.colorScheme.secondary
    val SecondaryVariant: Color
        @Composable get() = MaterialTheme.colorScheme.secondaryContainer

    val Background: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val Surface: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val SurfaceVariant: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val OnBackground: Color
        @Composable get() = MaterialTheme.colorScheme.onBackground
    val OnSurface: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    val Outline: Color
        @Composable get() = MaterialTheme.colorScheme.outline
    val OutlineVariant: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val Error: Color
        @Composable get() = MaterialTheme.colorScheme.error
    val Success: Color
        @Composable get() = if (isSystemInDarkTheme()) {
            RamboostColors.Dark.Success
        } else {
            RamboostColors.Light.Success
        }
    val Warning: Color
        @Composable get() = if (isSystemInDarkTheme()) {
            RamboostColors.Dark.Warning
        } else {
            RamboostColors.Light.Warning
        }

    val GTAYellow: Color
        @Composable get() = Warning
    val GTAGreen: Color
        @Composable get() = Success
    val GTARed: Color
        @Composable get() = Error

    val UberBlue: Color
        @Composable get() = MaterialTheme.colorScheme.tertiary
    val UberBlack: Color
        @Composable get() = MaterialTheme.colorScheme.onBackground
    val UberWhite: Color
        @Composable get() = MaterialTheme.colorScheme.background

    val DarkBackground: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val DarkSurface: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val DarkSurfaceVariant: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
    val DarkOnBackground: Color
        @Composable get() = MaterialTheme.colorScheme.onBackground
    val DarkOnSurface: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val DarkOnSurfaceVariant: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val DarkPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val DarkSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.secondary
}
