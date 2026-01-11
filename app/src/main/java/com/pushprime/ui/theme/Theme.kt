package com.pushprime.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * PushPrime Material 3 theme
 * Light theme: soft white, dark navy, mint/blue accents
 */
private val PushPrimeLightColorScheme = lightColorScheme(
    primary = PushPrimeColors.Primary,
    onPrimary = PushPrimeColors.OnBackground,
    primaryContainer = PushPrimeColors.PrimaryLight,
    onPrimaryContainer = PushPrimeColors.OnBackground,
    
    secondary = PushPrimeColors.Secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = PushPrimeColors.SecondaryVariant.copy(alpha = 0.1f),
    onSecondaryContainer = PushPrimeColors.OnBackground,
    
    background = PushPrimeColors.Background,
    onBackground = PushPrimeColors.OnBackground,
    
    surface = PushPrimeColors.Surface,
    onSurface = PushPrimeColors.OnSurface,
    surfaceVariant = PushPrimeColors.SurfaceVariant,
    onSurfaceVariant = PushPrimeColors.OnSurfaceVariant,
    
    error = PushPrimeColors.Error,
    onError = Color(0xFFFFFFFF),
    errorContainer = PushPrimeColors.Error.copy(alpha = 0.1f),
    onErrorContainer = PushPrimeColors.Error,
    
    outline = PushPrimeColors.Outline,
    outlineVariant = PushPrimeColors.OutlineVariant
)

@Composable
fun PushPrimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = PushPrimeLightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
