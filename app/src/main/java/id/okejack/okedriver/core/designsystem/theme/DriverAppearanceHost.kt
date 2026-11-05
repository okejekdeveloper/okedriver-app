package id.okejack.okedriver.core.designsystem.theme

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.datastore.AppearancePreferences

data class DriverAppearanceController(val style: DriverUiStyle, val select: (DriverUiStyle) -> Unit)
val LocalDriverAppearance = staticCompositionLocalOf {
    DriverAppearanceController(DriverUiStyle.SIMPLE) {}
}

@Composable
fun DriverAppearanceHost(content: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { AppearancePreferences(context) }
    val style by preferences.changes.collectAsStateWithLifecycle(initialValue = preferences.current)
    CompositionLocalProvider(
        LocalDriverUiStyle provides style,
        LocalDriverAppearance provides DriverAppearanceController(style, preferences::select)
    ) {
        // Keep navigation and in-progress work when appearance changes.
        OkedriverTheme(uiStyle = style, content = content)
    }
}
