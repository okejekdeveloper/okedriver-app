package id.okejack.okedriver.feature.order.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.theme.OkeDanger
import id.okejack.okedriver.core.designsystem.theme.OkeSuccess
import id.okejack.okedriver.feature.order.domain.model.DriverOrder

@Composable
fun OrderHistoryCard(order: DriverOrder, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${order.serviceLabel} · #${order.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(order.statusLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                color = when (order.status) { DriverOrder.STATUS_CANCELLED -> MaterialTheme.colorScheme.error; DriverOrder.STATUS_ARRIVED -> MaterialTheme.colorScheme.secondary; else -> MaterialTheme.colorScheme.primary })
            if (order.createdAt.isNotBlank()) Text(order.createdAt, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider()
            Text("Jemput: ${order.originAddress.ifBlank { "Alamat belum tersedia" }}", style = MaterialTheme.typography.bodyMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("Tujuan: ${order.destinationAddress.ifBlank { "Alamat belum tersedia" }}", style = MaterialTheme.typography.bodyMedium,
                maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (order.distance > 0) "%.1f km".format(java.util.Locale("id", "ID"), order.distance / 1000.0) else "Jarak belum tersedia",
                    Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(CurrencyFormatter.rupiah(order.totalPaid), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
            Text("Lihat detail order", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        }
    }
}
