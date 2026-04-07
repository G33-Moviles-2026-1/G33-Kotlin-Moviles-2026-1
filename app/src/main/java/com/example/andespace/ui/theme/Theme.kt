package com.example.andespace.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryYellow,
    onPrimary = Black,
    surface = LightYellow,
    onSurface = Black,
    background = White,
    onBackground = Black,
    secondary = PrimaryYellow,
    onSecondary = Black,
    primaryContainer = LightYellow,
    onPrimaryContainer = Black,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    outline = Color(0xFFE8E8E8),
    outlineVariant = Color(0xFFE8E8E8)
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryYellow,
    onPrimary = Black,
    surface = Color(0xFF211917),
    onSurface = White,
    background = Color(0xFF0C1818),
    onBackground = White,
    secondary = PrimaryYellow,
    onSecondary = Black,
    primaryContainer = Color(0xFF211917),
    onPrimaryContainer = White,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBBBBBB),
    outline = Color(0xFF555555),
    outlineVariant = Color(0xFF444444)
)

@Composable
fun AndeSpaceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    val typography = AndeSpaceTypography()

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
