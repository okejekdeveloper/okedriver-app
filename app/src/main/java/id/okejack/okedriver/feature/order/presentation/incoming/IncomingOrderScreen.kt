package id.okejack.okedriver.feature.order.presentation.incoming

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.presentation.components.OrderContent
import id.okejack.okedriver.feature.order.presentation.components.formatOrderDistance
import id.okejack.okedriver.core.designsystem.theme.LocalDriverUiStyle
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.feature.order.presentation.dynamic.DynamicIncomingSummary

@Composable
fun IncomingOrderScreen(orderId: Long, onClose: () -> Unit, onAccepted: (Long) -> Unit,
    viewModel: IncomingOrderViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val busy = state.isAccepting || state.isDeclining
    var confirmDecline by remember { mutableStateOf(false) }
    fun requestClose() { if (!busy) { if (state.order == null) onClose() else confirmDecline = true } }
    BackHandler { requestClose() }
    if (confirmDecline) AlertDialog(onDismissRequest = { confirmDecline = false }, title = { Text("Tolak order ini?") },
        text = { Text("Order ini akan ditolak dan tidak bisa Anda jalankan.") },
        confirmButton = { TextButton(onClick = { confirmDecline = false; viewModel.declineOrder(onClose) }) { Text("Ya, tolak order") } },
        dismissButton = { TextButton(onClick = { confirmDecline = false }) { Text("Kembali ke order") } })
    LaunchedEffect(orderId) {
        NotificationManagerCompat.from(context).cancel("ORDER", orderId.hashCode())
        viewModel.loadOrder(orderId)
    }
    IncomingOrderContent(state.order, state.isLoading, state.errorMessage, state.isAccepting, state.isDeclining,
        ::requestClose, { viewModel.loadOrder() }, { viewModel.acceptOrder(onAccepted) }, { confirmDecline = true })
}
@Composable
fun IncomingOrderContent(order: DriverOrder?, isLoading: Boolean, errorMessage: String?,
    isAccepting: Boolean, isDeclining: Boolean, onClose: () -> Unit,
    onRefresh: () -> Unit, onAccept: () -> Unit, onDecline: () -> Unit) {
    Scaffold(topBar = { OkeTopBar("Order masuk", onClose) {
        IconButton(onClick = onRefresh, enabled = !isLoading && !isAccepting && !isDeclining) {
            Icon(Icons.Rounded.Refresh, "Perbarui order")
        }
    } }, bottomBar = {
        if (order != null) Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
            Box(Modifier.fillMaxWidth().navigationBarsPadding(), contentAlignment = Alignment.Center) {
                Row(Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OkeOutlinedButton(if (isDeclining) "Menolak..." else "Tolak", onDecline,
                        Modifier.weight(1f), enabled = !isAccepting && !isDeclining)
                    OkeButton("Terima order", onAccept, Modifier.weight(2f),
                        enabled = !isDeclining, isLoading = isAccepting)
                }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            DriverContent {
                if (errorMessage != null) DriverNotice(errorMessage, "Coba lagi", onRefresh, true)
                when {
                    order != null -> {
                        if (LocalDriverUiStyle.current == DriverUiStyle.DYNAMIC) DynamicIncomingSummary(order)
                        else DriverCard {
                            Text("${order.serviceLabel} · #${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Total pembayaran pelanggan", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.rupiah(order.totalPaid), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                            Text("${order.paymentProviderName.ifBlank { "Lihat detail pembayaran" }} · ${formatOrderDistance(order.distance)}",
                                style = MaterialTheme.typography.bodyLarge)
                        }
                        OrderContent(order, isCurrentOrder = false, showChatAction = false,
                            showPaymentDetailAction = false, showMap = false, showActions = false)
                    }
                    isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                    else -> DriverEmptyState("Order tidak tersedia", "Order mungkin sudah dibatalkan atau diambil driver lain.", "Periksa lagi", onRefresh)
                }
            }
        }
    }
}
