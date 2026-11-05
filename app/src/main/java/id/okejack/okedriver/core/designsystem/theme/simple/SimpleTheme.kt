package id.okejack.okedriver.core.designsystem.theme.simple

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import id.okejack.okedriver.core.designsystem.theme.*

val SimpleLightColors = lightColorScheme(
    primary = OkePrimary,
    onPrimary = OkeTextOnPrimary,
    primaryContainer = OkePrimaryContainer,
    onPrimaryContainer = OkePrimaryDark,
    secondary = OkeSecondary,
    onSecondary = Color.White,
    secondaryContainer = OkeSecondaryContainer,
    onSecondaryContainer = Color(0xFF173D2B),
    tertiary = OkePrimary,
    tertiaryContainer = OkePrimaryContainer,
    onTertiaryContainer = OkePrimaryDark,
    background = OkeBg,
    onBackground = OkeTextPrimary,
    surface = OkeSurface,
    surfaceContainer = OkeBg,
    surfaceContainerLow = OkeSurface,
    surfaceContainerHigh = OkeInputBg,
    surfaceContainerHighest = OkeInputBg,
    onSurface = OkeTextPrimary,
    surfaceVariant = OkeInputBg,
    onSurfaceVariant = OkeTextSecondary,
    outline = OkeCardBorder,
    outlineVariant = OkeInputBorder,
    error = OkeDanger,
    onError = Color.White,
    errorContainer = OkeDangerBg,
    onErrorContainer = Color(0xFF690005)
)

val SimpleDarkColors = darkColorScheme(
    primary = OkePrimaryLight,
    onPrimary = OkeDarkBg,
    primaryContainer = OkeDarkSurface,
    onPrimaryContainer = OkePrimaryLight,
    secondary = Color(0xFF9FD5B6),
    onSecondary = OkeDarkBg,
    background = OkeDarkBg,
    onBackground = Color.White,
    surface = OkeDarkSurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF282D35),
    onSurfaceVariant = Color(0xFFC7CCD2),
    secondaryContainer = Color(0xFF234534),
    onSecondaryContainer = Color(0xFFC0EED0),
    outline = Color(0xFF8D96A0),
    outlineVariant = Color(0xFF414850),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

