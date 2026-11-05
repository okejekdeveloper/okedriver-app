package id.okejack.okedriver.core.designsystem.theme.dynamic

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val DynamicLightColors = lightColorScheme(
    primary = Color(0xFF6D34CF), onPrimary = Color.White,
    primaryContainer = Color(0xFFEFE6FF), onPrimaryContainer = Color(0xFF452177),
    secondary = Color(0xFF147456), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F4EA), onSecondaryContainer = Color(0xFF12533B),
    tertiary = Color(0xFF945000), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFECCD), onTertiaryContainer = Color(0xFF673600),
    background = Color(0xFFF6F3FB), onBackground = Color(0xFF241B35),
    surface = Color.White, onSurface = Color(0xFF241B35),
    surfaceVariant = Color(0xFFF0EAF8), onSurfaceVariant = Color(0xFF62566F),
    surfaceContainer = Color(0xFFF6F3FB), surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color(0xFFF0EAF8), surfaceContainerHighest = Color(0xFFEAE1F5),
    outline = Color(0xFF978AA9), outlineVariant = Color(0xFFE6DEF0),
    error = Color(0xFFBA1A1A), onError = Color.White,
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF690005)
)

val DynamicDarkColors = darkColorScheme(
    primary = Color(0xFFD1B8FF), onPrimary = Color(0xFF30115B),
    primaryContainer = Color(0xFF442469), onPrimaryContainer = Color(0xFFECDDFF),
    secondary = Color(0xFF8FD7B7), onSecondary = Color(0xFF003825),
    secondaryContainer = Color(0xFF193F32), onSecondaryContainer = Color(0xFFB6F1D3),
    tertiary = Color(0xFFFFCD87), onTertiary = Color(0xFF482900),
    tertiaryContainer = Color(0xFF503715), onTertiaryContainer = Color(0xFFFFE6C1),
    background = Color(0xFF100C1B), onBackground = Color(0xFFF1EAF9),
    surface = Color(0xFF21192F), onSurface = Color(0xFFF1EAF9),
    surfaceVariant = Color(0xFF30263E), onSurfaceVariant = Color(0xFFCFC1DB),
    surfaceContainer = Color(0xFF191223), surfaceContainerLow = Color(0xFF21192F),
    surfaceContainerHigh = Color(0xFF30263E), surfaceContainerHighest = Color(0xFF3B304A),
    outline = Color(0xFFA598B3), outlineVariant = Color(0xFF463950),
    error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
)
