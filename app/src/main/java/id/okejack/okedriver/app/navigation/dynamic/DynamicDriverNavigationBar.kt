package id.okejack.okedriver.app.navigation.dynamic

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.app.navigation.DriverTab

@Composable
fun DynamicDriverNavigationBar(selectedRoute: String, onSelect: (DriverTab) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(Modifier.fillMaxWidth().navigationBarsPadding(), contentAlignment = Alignment.Center) {
            Surface(Modifier.widthIn(max = 680.dp).fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface) {
                Row(Modifier.padding(vertical = 6.dp)) {
                    DriverTab.entries.forEach { tab ->
                        val selected = tab.route == selectedRoute
                        val background by animateColorAsState(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            tween(180), label = "nav_color")
                        val foreground = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        Surface(modifier = Modifier.weight(1f).selectable(selected, role = Role.Tab, onClick = { if (!selected) onSelect(tab) }),
                            color = background, contentColor = foreground, shape = MaterialTheme.shapes.small) {
                            Column(Modifier.heightIn(min = 64.dp).padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(tab.icon, null)
                                Text(tab.label, style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    maxLines = 1, softWrap = false)
                            }
                        }
                    }
                }
            }
        }
    }
}
