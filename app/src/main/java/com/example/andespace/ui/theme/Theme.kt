package com.example.andespace.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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
    onPrimaryContainer = Black
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryYellow,
    onPrimary = Black,
    surface = LightYellow,
    onSurface = Black,
    background = White,
    onBackground = Black,
    secondary = PrimaryYellow,
    onSecondary = Black,
    primaryContainer = LightYellow,
    onPrimaryContainer = Black
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
