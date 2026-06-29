package id.okejack.okedriver.feature.order.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onOpenPayments: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OrderHeroCard(
            order = order,
            onNavigate = { openMap(context, order.targetLatLng) }
        )
        CustomerCard(
            order = order,
            onCall = { openDial(context, order.customerPhone) },
            onOpenChat = onOpenChat,
            showChatAction = showChatAction
        )
        AddressCard(order = order)
        PaymentCard(
            order = order,
            showPaymentDetailAction = showPaymentDetailAction,
            onOpenPayments = onOpenPayments
        )
        if (order.courier != null) {
            CourierCard(order = order)
        }
        if (order.items.isNotEmpty()) {
            ItemsCard(
                order = order,
                isCurrentOrder = isCurrentOrder,
                onOpenEditShopping = onOpenEditShopping
            )
        }
        if (order.driverInfo.isNotBlank()) {
            InfoCard(title = "Catatan", body = order.driverInfo)
        }
        if (isCurrentOrder) {
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
        Marker(
            state = MarkerState(position = target),
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
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.customerName.ifBlank { "Customer" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.customerPhone.ifBlank { "Nomor belum tersedia" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onCall, enabled = order.customerPhone.isNotBlank()) {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("Telepon")
            }
            if (showChatAction) {
                TextButton(onClick = onOpenChat) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Chat")
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
            AmountRow("Ongkir", CurrencyFormatter.rupiah(order.fee))
            if (order.totalDiscount > java.math.BigDecimal.ZERO) {
                AmountRow("Diskon", "-${CurrencyFormatter.rupiah(order.totalDiscount)}")
            }
            if (order.totalShopping > java.math.BigDecimal.ZERO) {
                AmountRow("Belanja", CurrencyFormatter.rupiah(order.totalShopping))
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
private fun CurrentOrderActions(
    order: DriverOrder,
    isUpdatingStatus: Boolean,
    isCancelling: Boolean,
    onUpdateStatus: () -> Unit,
    onCancel: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onUpdateStatus,
                enabled = order.canUpdateStatus && !isUpdatingStatus && !isCancelling,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isUpdatingStatus) "Memproses..." else order.nextActionLabel)
            }
            OutlinedButton(
                onClick = onCancel,
                enabled = order.canCancel && !isUpdatingStatus && !isCancelling,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isCancelling) "Membatalkan..." else "Batalkan order")
            }
        }
    }
}

private fun parseLatLng(value: String): LatLng? {
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

private fun openMap(context: Context, latLng: String) {
    val target = parseLatLng(latLng) ?: return
    val value = "${target.latitude},${target.longitude}"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$value?z=14&q=$value")).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

private fun openDial(context: Context, phone: String) {
    if (phone.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
    context.startActivity(intent)
}
