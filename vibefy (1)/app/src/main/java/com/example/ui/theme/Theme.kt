package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KinemaxColorScheme = darkColorScheme(
    primary = KinemaxAccent,
    onPrimary = KinemaxOnPrimary,
    primaryContainer = KinemaxSurfaceVariant,
    onPrimaryContainer = KinemaxAccentGlow,
    secondary = KinemaxAccentLight,
    onSecondary = KinemaxOnPrimary,
    tertiary = KinemaxGold,
    background = KinemaxBackground,
    onBackground = KinemaxTextPrimary,
    surface = KinemaxSurface,
    onSurface = KinemaxTextPrimary,
    surfaceVariant = KinemaxSurfaceVariant,
    onSurfaceVariant = KinemaxTextSecondary,
    outline = KinemaxDivider
)

@Composable
fun KinemaxMusicTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = KinemaxBackground.toArgb()
            window.navigationBarColor = KinemaxBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = KinemaxColorScheme,
        typography = Typography,
        content = content
    )
}
