package id.okejack.okedriver.feature.order.presentation.dynamic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.dynamic.*
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.presentation.components.formatOrderDistance

@Composable
private fun OrderServiceBadge(order: DriverOrder) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp).background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(when (order.type) {
                DriverOrder.TYPE_CAR, DriverOrder.TYPE_RENTAL_CAR -> Icons.Rounded.DirectionsCar
                DriverOrder.TYPE_DELIVERY, DriverOrder.TYPE_EXPRESS -> Icons.Rounded.LocalShipping
                DriverOrder.TYPE_FOOD, DriverOrder.TYPE_SHOPPING, DriverOrder.TYPE_MART -> Icons.Rounded.ShoppingBag
                else -> Icons.Rounded.TwoWheeler
            }, null, tint = Color.White)
        }
        Column(Modifier.weight(1f)) {
            Text(order.serviceLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Order #${order.id}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DynamicIncomingSummary(order: DriverOrder) {
    DynamicHero {
        OrderServiceBadge(order)
        Text("Total pembayaran pelanggan", style = MaterialTheme.typography.bodyLarge)
        Text(CurrencyFormatter.rupiah(order.totalPaid), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(order.paymentProviderName.ifBlank { "Lihat detail pembayaran" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(formatOrderDistance(order.distance).let { if (order.distance > 0) "$it perjalanan" else it },
            style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun DynamicCurrentSummary(order: DriverOrder, onNavigate: () -> Unit) {
    val pickingUp = order.status == DriverOrder.STATUS_PENDING || order.status == DriverOrder.STATUS_ASSIGNED
    DynamicHero {
        OrderServiceBadge(order)
        Text(if (pickingUp) "Menuju lokasi jemput" else order.statusLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (pickingUp) "LOKASI JEMPUT" else "LOKASI TUJUAN", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(order.targetAddress.ifBlank { "Alamat belum tersedia" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        if (order.targetAddressDetail.isNotBlank()) Text(order.targetAddressDetail, style = MaterialTheme.typography.bodyLarge)
        DynamicHeroButton("Buka petunjuk jalan", onNavigate, enabled = order.targetLatLng.isNotBlank())
    }
}
