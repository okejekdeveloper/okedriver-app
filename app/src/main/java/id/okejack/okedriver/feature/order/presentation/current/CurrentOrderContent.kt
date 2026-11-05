package id.okejack.okedriver.feature.order.presentation.current

import id.okejack.okedriver.core.designsystem.component.DriverTonalButton as FilledTonalButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.presentation.components.CurrentOrderActions
import id.okejack.okedriver.feature.order.presentation.components.OrderContent
import id.okejack.okedriver.feature.order.presentation.components.OrderMapSurface
import id.okejack.okedriver.feature.order.presentation.components.openDial
import id.okejack.okedriver.feature.order.presentation.components.openMap

/**
 * Map, scrollable order details and fixed trip actions share a predictable vertical layout.
 */
@Composable
fun CurrentOrderContent(order: DriverOrder?, isLoading: Boolean, isUpdating: Boolean, isCancelling: Boolean,
    errorMessage: String?, onBack: () -> Unit, onRefresh: () -> Unit, onUpdate: () -> Unit,
    onCancel: () -> Unit, onChat: () -> Unit, onEdit: () -> Unit, onPayments: () -> Unit,
    onQris: () -> Unit, showMap: Boolean = true) {
    if (order == null) {
        OrderUnavailable(isLoading, errorMessage, onBack, onRefresh)
        return
    }
    val context = LocalContext.current
    val busy = isLoading || isUpdating || isCancelling
    MapFirstScaffold(
        clearNavigationBar = true,
        map = { padding ->
            if (showMap) OrderMapSurface(order, padding)
            else Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
        },
        overlay = {
            Row(
                Modifier.align(Alignment.TopStart).fillMaxWidth().statusBarsPadding().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                MapIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali", onBack)
                Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)) {
                    Text("${order.serviceLabel} · #${order.id}", Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                MapIconButton(Icons.Rounded.Refresh, "Perbarui order", onRefresh, enabled = !busy)
                MapIconButton(Icons.Rounded.Navigation, "Buka petunjuk jalan", { openMap(context, order.targetLatLng) },
                    enabled = order.targetLatLng.isNotBlank())
            }
        },
        peek = {
            errorMessage?.let { DriverNotice(it, "Coba lagi", onRefresh, true) }
            if (order.isQrisPaidInTransit) DriverNotice("Pembayaran QRIS sudah lunas. Order dapat diselesaikan setelah pengantaran selesai.")
            OrderSummary(order)
            ContactRow(
                onCall = { openDial(context, order.customerPhone) }, callEnabled = order.customerPhone.isNotBlank(),
                onChat = onChat
            )
        },
        details = {
            OrderContent(order, isCurrentOrder = true, onOpenChat = onChat, onOpenEditShopping = onEdit,
                onOpenPayments = onPayments, showActions = false, showMap = false)
        },
        actions = {
            if (order.canUpdateStatus || order.canCancel || order.requiresQrisPayment) {
                CurrentOrderActions(order, isUpdating, isCancelling, onUpdate, onCancel, onQris)
            }
        }
    )
}

@Composable
private fun OrderSummary(order: DriverOrder) {
    val pickingUp = order.status == DriverOrder.STATUS_PENDING || order.status == DriverOrder.STATUS_ASSIGNED
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("${if (pickingUp) "LOKASI JEMPUT" else "LOKASI TUJUAN"} · ${order.statusLabel}".uppercase(),
            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(order.targetAddress.ifBlank { "Alamat belum tersedia" }, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (order.targetAddressDetail.isNotBlank()) Text(order.targetAddressDetail, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ContactRow(onCall: () -> Unit, callEnabled: Boolean, onChat: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = onCall, enabled = callEnabled, modifier = Modifier.weight(1f).heightIn(min = 48.dp),
            shape = MaterialTheme.shapes.small) {
            Icon(Icons.Rounded.Call, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
            Text("Telepon", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        FilledTonalButton(onClick = onChat, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = MaterialTheme.shapes.small) {
            Icon(Icons.AutoMirrored.Rounded.Chat, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
            Text("Chat", maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Floating over the map, so it needs its own surface to stay legible on any map colour. */
@Composable
private fun MapIconButton(icon: ImageVector, description: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp)) { Icon(icon, description) }
    }
}

/** No order to draw on a map: still loading, or it could not be found. */
@Composable
private fun OrderUnavailable(isLoading: Boolean, errorMessage: String?, onBack: () -> Unit, onRefresh: () -> Unit) {
    Scaffold(topBar = { OkeTopBar("Order aktif", onBack) {
        IconButton(onClick = onRefresh, enabled = !isLoading) { Icon(Icons.Rounded.Refresh, "Perbarui order") }
    } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            DriverContent {
                if (isLoading) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator()
                        Text("Memuat order aktif...", style = MaterialTheme.typography.bodyLarge)
                    }
                } else DriverEmptyState("Order belum dapat dimuat",
                    errorMessage ?: "Periksa koneksi, lalu muat kembali order Anda.", "Coba lagi", onRefresh)
            }
        }
    }
}
