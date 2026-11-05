package id.okejack.okedriver.feature.order.presentation.components

import id.okejack.okedriver.core.designsystem.component.DriverOutlinedButton as OutlinedButton
import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import com.google.maps.android.compose.Polyline
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import id.okejack.okedriver.core.designsystem.component.LocalLocationConsent
import id.okejack.okedriver.core.designsystem.component.SlidingButton
import id.okejack.okedriver.core.designsystem.component.DriverCard
import id.okejack.okedriver.core.designsystem.component.OkeButton

private const val ORDER_MAP_ZOOM = 15f

@Composable
fun OrderContent(
    order: DriverOrder,
    isCurrentOrder: Boolean,
    isUpdatingStatus: Boolean = false,
    isCancelling: Boolean = false,
    showChatAction: Boolean = true,
    showPaymentDetailAction: Boolean = true,
    onUpdateStatus: () -> Unit = {},
    onCancel: () -> Unit = {},
    onOpenChat: () -> Unit = {},
    onOpenEditShopping: () -> Unit = {},
    onOpenPayments: () -> Unit = {},
    showMap: Boolean = true,
    showActions: Boolean = true
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showMap) OrderHeroCard(
            order = order,
            onNavigate = { openMap(context, order.targetLatLng) }
        )
        AddressCard(order = order)
        if (order.driverInfo.isNotBlank()) {
            InfoCard(title = "Catatan pelanggan", body = order.driverInfo)
        }
        CustomerCard(
            order = order,
            onCall = { openDial(context, order.customerPhone) },
            onOpenChat = onOpenChat,
            showChatAction = showChatAction
        )
        PaymentCard(
            order = order,
            showPaymentDetailAction = showPaymentDetailAction,
            onOpenPayments = onOpenPayments
        )
        if (order.courier != null) {
            CourierCard(order = order)
        }
        if (order.items.isNotEmpty() || (isCurrentOrder && order.canEditShopping)) {
            ItemsCard(
                order = order,
                isCurrentOrder = isCurrentOrder && order.canEditShopping,
                onOpenEditShopping = onOpenEditShopping
            )
        }
        order.merchant?.let { merchant ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(merchant.name, style = MaterialTheme.typography.titleMedium)
                    Text(merchant.address)
                    if (isCurrentOrder && merchant.phone.isNotBlank()) TextButton(onClick = { openDial(context, merchant.phone) }) { Text("Telepon merchant") }
                }
            }
        }
        if (order.expressVerificationCode.isNotBlank()) InfoCard("Kode verifikasi", order.expressVerificationCode)
        order.packageInfo?.let { info ->
            InfoCard("Paket ${info.name}", buildString {
                append(info.description)
                if (info.duration > 0) append("\nDurasi: ${info.duration} menit")
                if (info.overtime > 0) append("\nTambahan waktu: ${info.overtime} menit (${CurrencyFormatter.rupiah(info.overtimeFee)})")
            })
        }
        if (!isCurrentOrder) {
            order.cancellation?.takeIf { order.status == DriverOrder.STATUS_CANCELLED }?.let {
                InfoCard("Pembatalan", listOf(it.reason, it.cancelledAt).filter(String::isNotBlank).joinToString("\n"))
            }
            order.customerScore?.let {
                InfoCard("Penilaian customer", listOf(when (it.score) { 2 -> "Baik"; 1 -> "Kurang baik"; else -> "Belum ada penilaian" }, it.review).filter(String::isNotBlank).joinToString("\n"))
            }
        }
        if (isCurrentOrder && showActions) {
            CurrentOrderActions(
                order = order,
                isUpdatingStatus = isUpdatingStatus,
                isCancelling = isCancelling,
                onUpdateStatus = onUpdateStatus,
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun OrderHeroCard(
    order: DriverOrder,
    onNavigate: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${order.serviceLabel} #${order.id}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.statusLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onNavigate) {
                    Icon(
                        imageVector = Icons.Rounded.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Navigasi")
                }
            }
            OrderMap(order = order)
        }
    }
}

@Composable
private fun OrderMap(order: DriverOrder) {
    val mapViewModel: OrderMapViewModel = hiltViewModel()
    val route by mapViewModel.route.collectAsStateWithLifecycle()
    val locationConsent = LocalLocationConsent.current
    val target = remember(order.targetLatLng) { parseLatLng(order.targetLatLng) }
    if (target == null) {
        InfoCard(
            title = "Peta",
            body = "Koordinat order belum tersedia"
        )
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(target, ORDER_MAP_ZOOM)
    }

    LaunchedEffect(target) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(target, ORDER_MAP_ZOOM)
        )
    }
    LaunchedEffect(order.id, order.status, order.targetLatLng, order.encodedRoute, locationConsent.granted) {
        mapViewModel.load(order, locationConsent.granted)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(8.dp)),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    ) {
        if (route.size > 1) Polyline(points = route.map { LatLng(it.latitude, it.longitude) }, color = MaterialTheme.colorScheme.primary, width = 8f)
        Marker(
            state = remember(target) { MarkerState(position = target) },
            title = order.targetAddress.ifBlank { "Tujuan order" }
        )
    }
}

@Composable
private fun CustomerCard(
    order: DriverOrder,
    onCall: () -> Unit,
    onOpenChat: () -> Unit,
    showChatAction: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column {
                Text(
                    text = order.customerName.ifBlank { "Pelanggan" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.customerPhone.ifBlank { "Nomor belum tersedia" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onCall, enabled = order.customerPhone.isNotBlank(),
                modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Telepon")
            }
            if (showChatAction) {
                OutlinedButton(onClick = onOpenChat, modifier = Modifier.weight(1f).heightIn(min = 52.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Pesan")
                }
            }
            }
        }
    }
}

@Composable
private fun AddressCard(order: DriverOrder) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Alamat",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            AddressRow(
                title = "Jemput",
                address = order.originAddress,
                detail = order.originAddressDetail
            )
            HorizontalDivider()
            AddressRow(
                title = "Tujuan",
                address = order.destinationAddress,
                detail = order.destinationAddressDetail
            )
            Text(
                text = distanceLabel(order.distance),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddressRow(
    title: String,
    address: String,
    detail: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = address.ifBlank { "-" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (detail.isNotBlank()) {
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PaymentCard(
    order: DriverOrder,
    showPaymentDetailAction: Boolean,
    onOpenPayments: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.paymentProviderName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showPaymentDetailAction) {
                    TextButton(onClick = onOpenPayments) {
                        Text("Detail")
                    }
                }
            }
            AmountRow("Ongkir", CurrencyFormatter.rupiah(order.totalFee))
            if (order.totalDiscount > java.math.BigDecimal.ZERO) {
                AmountRow("Diskon", "-${CurrencyFormatter.rupiah(order.totalDiscount)}")
            }
            if (order.totalShopping > java.math.BigDecimal.ZERO) {
                AmountRow("Belanja", CurrencyFormatter.rupiah(order.totalShopping))
            }
            if (order.originalTotalShopping > java.math.BigDecimal.ZERO) {
                AmountRow("Bayar ke merchant", CurrencyFormatter.rupiah(order.originalTotalShopping))
            }
            if (order.serviceFee > java.math.BigDecimal.ZERO) {
                AmountRow("Biaya layanan", CurrencyFormatter.rupiah(order.serviceFee))
            }
            HorizontalDivider()
            AmountRow(
                label = "Total",
                value = CurrencyFormatter.rupiah(order.totalPaid),
                emphasized = true
            )
        }
    }
}

@Composable
private fun AmountRow(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CourierCard(order: DriverOrder) {
    val courier = order.courier ?: return
    val context = LocalContext.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Paket",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Barang: ${courier.itemDetail.ifBlank { "-" }}")
            Text("Pengirim: ${courier.senderName.ifBlank { "-" }}")
            Text("Penerima: ${courier.recipientName.ifBlank { "-" }}")
            if (courier.senderPhone.isNotBlank()) TextButton(onClick = { openDial(context, courier.senderPhone) }) { Text("Telepon pengirim") }
            if (courier.recipientPhone.isNotBlank()) TextButton(onClick = { openDial(context, courier.recipientPhone) }) { Text("Telepon penerima") }
        }
    }
}

@Composable
private fun ItemsCard(
    order: DriverOrder,
    isCurrentOrder: Boolean,
    onOpenEditShopping: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Item",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (isCurrentOrder) {
                    TextButton(onClick = onOpenEditShopping) {
                        Text("Edit")
                    }
                }
            }
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name.ifBlank { "Item" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (item.detail.isNotBlank()) {
                            Text(
                                text = item.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "${item.quantity}x ${CurrencyFormatter.rupiah(item.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CurrentOrderActions(
    order: DriverOrder,
    isUpdatingStatus: Boolean,
    isCancelling: Boolean,
    onUpdateStatus: () -> Unit,
    onCancel: () -> Unit,
    onOpenQris: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (order.requiresQrisPayment && onOpenQris != null) {
                Text("Minta pelanggan memindai QRIS, lalu tunggu konfirmasi pembayaran.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OkeButton("Tampilkan QRIS pembayaran", onOpenQris, enabled = !isUpdatingStatus && !isCancelling)
            } else if (order.canUpdateStatus) {
            Text(if (order.status == DriverOrder.STATUS_DEPARTED) "Pastikan pengantaran dan pembayaran sudah selesai."
                else "Mulai setelah selesai menjemput.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val primary = MaterialTheme.colorScheme.primary
            SlidingButton(
                label = when {
                    isUpdatingStatus -> "Memproses..."
                    order.status == DriverOrder.STATUS_DEPARTED -> "Geser untuk selesai"
                    else -> "Geser untuk mulai"
                },
                onConfirm = onUpdateStatus,
                enabled = order.canUpdateStatus && !isUpdatingStatus && !isCancelling,
                gradientColors = listOf(primary, primary),
                thumbIcon = Icons.Rounded.ChevronRight
            )
            }
            if (order.canCancel) OutlinedButton(
                onClick = onCancel,
                enabled = order.canCancel && !isUpdatingStatus && !isCancelling,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isCancelling) "Membatalkan..." else "Batalkan order")
            }
        }
    }
}

internal fun parseLatLng(value: String): LatLng? {
    val parts = value.split(",")
    if (parts.size != 2) return null
    val latitude = parts[0].trim().toDoubleOrNull() ?: return null
    val longitude = parts[1].trim().toDoubleOrNull() ?: return null
    if (latitude == 0.0 && longitude == 0.0) return null
    return LatLng(latitude, longitude)
}

private fun distanceLabel(distance: Int): String {
    if (distance <= 0) return "Jarak belum tersedia"
    return if (distance >= 1000) {
        "%.1f km".format(distance / 1000.0)
    } else {
        "$distance m"
    }
}

fun openMap(context: Context, latLng: String) {
    val target = parseLatLng(latLng) ?: return
    val value = "${target.latitude},${target.longitude}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$value?z=14&q=$value")).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$value"))) }
        catch (ignored: ActivityNotFoundException) { Toast.makeText(context, "Aplikasi peta tidak tersedia", Toast.LENGTH_SHORT).show() }
    }
}

fun openDial(context: Context, phone: String) {
    if (phone.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    try { context.startActivity(intent) }
    catch (e: ActivityNotFoundException) { Toast.makeText(context, "Aplikasi telepon tidak tersedia", Toast.LENGTH_SHORT).show() }
}
