package id.okejack.okedriver.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import id.okejack.okedriver.core.designsystem.theme.simple.SimpleLightColors
import id.okejack.okedriver.core.designsystem.theme.simple.SimpleDarkColors
import id.okejack.okedriver.core.designsystem.theme.dynamic.DynamicLightColors
import id.okejack.okedriver.core.designsystem.theme.dynamic.DynamicDarkColors

@Composable
fun OkedriverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    uiStyle: DriverUiStyle = LocalDriverUiStyle.current,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        uiStyle == DriverUiStyle.DYNAMIC && darkTheme -> DynamicDarkColors
        uiStyle == DriverUiStyle.DYNAMIC -> DynamicLightColors
        darkTheme -> SimpleDarkColors
        else -> SimpleLightColors
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalDriverUiStyle provides uiStyle) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = OkeTypography,
        shapes = OkeShapes,
        content = content
    )
    }
}
