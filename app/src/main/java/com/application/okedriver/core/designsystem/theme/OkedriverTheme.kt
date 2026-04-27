package id.okejack.okedriver.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = OkePrimary,
    onPrimary = OkeOnPrimary,
    primaryContainer = OkePrimaryLight,
    onPrimaryContainer = OkeOnPrimary,
    secondary = OkePrimaryLight,
    onSecondary = OkeOnPrimary,
    error = OkeError,
    surface = OkeSurface,
    surfaceVariant = OkeSurfaceVariant,
    outline = OkeOutline,
    onSurface = OkeTextPrimary,
    onSurfaceVariant = OkeTextSecondary,
)

private val DarkColorScheme = darkColorScheme(
    primary = OkePrimaryDarkTheme,
    onPrimary = OkeOnPrimaryDark,
    primaryContainer = OkePrimaryContainerDark,
    onPrimaryContainer = OkePrimaryDarkTheme,
    error = OkeError,
)

@Composable
fun OkedriverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OkedriverTypography,
        content = content
    )
}
