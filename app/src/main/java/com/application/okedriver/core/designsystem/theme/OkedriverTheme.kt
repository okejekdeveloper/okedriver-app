package com.application.okedriver.core.designsystem.theme

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

private val OkeLightColorScheme = lightColorScheme(
    primary          = OkePrimary,
    onPrimary        = OkeTextOnPrimary,
    primaryContainer = OkePrimaryContainer,
    onPrimaryContainer = OkePrimary,
    secondary        = OkeSecondary,
    onSecondary      = Color.White,
    secondaryContainer = OkeSecondaryContainer,
    background       = OkeBg,
    onBackground     = OkeTextPrimary,
    surface          = OkeSurface,
    onSurface        = OkeTextPrimary,
    surfaceVariant   = OkeInputBg,
    onSurfaceVariant = OkeTextSecondary,
    outline          = OkeInputBorder,
    error            = OkeDanger,
    onError          = Color.White
)

private val OkeDarkColorScheme = darkColorScheme(
    primary            = OkePrimaryLight,
    onPrimary          = OkeDarkBg,
    primaryContainer   = OkeDarkSurface,
    onPrimaryContainer = OkePrimaryLight,
    secondary          = OkeSecondary,
    onSecondary        = OkeDarkBg,
    background         = OkeDarkBg,
    onBackground       = Color.White,
    surface            = OkeDarkSurface,
    onSurface          = Color.White,
    error              = OkeDanger,
    onError            = Color.White
)

@Composable
fun OkedriverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) OkeDarkColorScheme else OkeLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = OkeTypography,
        shapes      = OkeShapes,
        content     = content
    )
}