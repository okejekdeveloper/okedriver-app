package id.okejack.okedriver.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.designsystem.theme.OkeShapeSheet

/**
 * A bounded map above scrollable order details, with actions fixed below both.
 * Map controls and trip actions remain visible while the driver reads the details.
 */
@Composable
fun MapFirstScaffold(
    map: @Composable (mapPadding: PaddingValues) -> Unit,
    overlay: @Composable BoxScope.() -> Unit,
    peek: @Composable ColumnScope.() -> Unit,
    details: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    clearNavigationBar: Boolean = false,
    actions: @Composable ColumnScope.() -> Unit = {}
) {
    val density = LocalDensity.current
    val navBottom = if (clearNavigationBar) WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding() else 0.dp
    val controlsHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 72.dp
    var actionsHeight by remember { mutableStateOf(0.dp) }

    BoxWithConstraints(modifier.fillMaxSize()) {
        val availableHeight = (maxHeight - actionsHeight).coerceAtLeast(0.dp)
        val mapHeight = maxOf(availableHeight * 0.42f, controlsHeight + 48.dp)
            .coerceAtMost(availableHeight)
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(mapHeight)) {
                map(PaddingValues(top = controlsHeight))
                overlay()
            }
            Surface(Modifier.fillMaxWidth().weight(1f), shape = OkeShapeSheet,
                color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp),
                        content = peek)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    details()
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth()
                    .onSizeChanged { size -> actionsHeight = with(density) { size.height.toDp() } },
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
            ) {
                Column(Modifier.fillMaxWidth().padding(bottom = navBottom), content = actions)
            }
        }
    }
}
