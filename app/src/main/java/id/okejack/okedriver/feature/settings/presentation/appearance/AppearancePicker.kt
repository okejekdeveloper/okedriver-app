package id.okejack.okedriver.feature.settings.presentation.appearance

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.core.designsystem.theme.LocalDriverAppearance
import id.okejack.okedriver.core.designsystem.component.dynamic.DynamicHeroEnd
import id.okejack.okedriver.core.designsystem.component.dynamic.DynamicHeroStart

@Composable
fun AppearancePicker(onDismiss: () -> Unit) {
    val appearance = LocalDriverAppearance.current
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Pilih tampilan") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Pilih tampilan yang paling nyaman untuk Anda.", style = MaterialTheme.typography.bodyLarge)
                DriverUiStyle.entries.forEach { style ->
                    val selected = appearance.style == style
                    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton,
                            onClick = { appearance.select(style) })) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.fillMaxWidth().height(64.dp).background(
                                if (style == DriverUiStyle.DYNAMIC) Brush.linearGradient(listOf(DynamicHeroStart, DynamicHeroEnd))
                                else Brush.linearGradient(listOf(Color(0xFFF0F2F4), Color(0xFFF0F2F4))), RoundedCornerShape(12.dp))) {
                                Row(Modifier.align(Alignment.Center).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    repeat(3) { index -> Box(Modifier.weight(1f).height((18 + index * 8).dp)
                                        .background(if (style == DriverUiStyle.DYNAMIC) Color.White.copy(alpha = 0.65f) else Color(0xFF89919A), RoundedCornerShape(5.dp))) }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(style.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(style.description, style = MaterialTheme.typography.bodyMedium)
                                }
                                RadioButton(selected, onClick = null)
                            }
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(onClick = onDismiss) { Text("Selesai") } })
}
