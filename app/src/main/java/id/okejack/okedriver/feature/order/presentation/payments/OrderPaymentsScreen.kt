package id.okejack.okedriver.feature.order.presentation.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.theme.*
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.presentation.detail.OrderDetailViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderPaymentsScreen(
    orderId: Long,
    onBackClick: () -> Unit,
    // Reuse OrderDetailViewModel karena data order sudah di-fetch di sana
    viewModel: OrderDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Pembayaran", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.loadOrder(orderId) },
                        enabled = !state.isLoading
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OkeBg)
                .padding(padding)
        ) {
            when {
                state.isLoading && state.order == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = OkePrimary
                    )
                }
                state.order != null -> {
                    PaymentsContent(
                        order = state.order!!,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    )
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.errorMessage ?: "Data tidak ditemukan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OkeTextSecondary
                        )
                        Button(
                            onClick = { viewModel.loadOrder(orderId) },
                            colors = ButtonDefaults.buttonColors(containerColor = OkePrimary)
                        ) { Text("Coba Lagi") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentsContent(order: DriverOrder, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = OkeShapeCard,
            colors = CardDefaults.cardColors(containerColor = OkePrimaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(OkePrimary, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Payment, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text(
                        text = order.paymentProviderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OkeTextPrimary
                    )
                    Text(
                        text = "Order #${order.id} · ${order.serviceLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OkeTextSecondary
                    )
                }
            }
        }

        // Rincian biaya
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = OkeShapeCard,
            colors = CardDefaults.cardColors(containerColor = OkeSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Rincian Biaya",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OkeTextPrimary
                )

                PaymentRow("Ongkos kirim", order.fee)

                if (order.originalFee != order.fee && order.originalFee > BigDecimal.ZERO) {
                    PaymentRow("Harga asli", order.originalFee, strikethrough = true)
                }
                if (order.totalDiscount > BigDecimal.ZERO) {
                    PaymentRow("Diskon", order.totalDiscount, isDiscount = true)
                }
                if (order.totalShopping > BigDecimal.ZERO) {
                    PaymentRow("Total belanja", order.totalShopping)
                }
                if (order.serviceFee > BigDecimal.ZERO) {
                    PaymentRow("Biaya layanan", order.serviceFee)
                }

                HorizontalDivider(color = OkeDivider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Dibayar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OkeTextPrimary
                    )
                    Text(
                        text = CurrencyFormatter.rupiah(order.totalPaid),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = OkePrimary
                    )
                }
            }
        }

        // Item list (kalau ada)
        if (order.items.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OkeShapeCard,
                colors = CardDefaults.cardColors(containerColor = OkeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Item (${order.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OkeTextPrimary
                    )
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name.ifBlank { "Item" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OkeTextPrimary
                                )
                                if (item.detail.isNotBlank()) {
                                    Text(
                                        text = item.detail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OkeTextSecondary
                                    )
                                }
                            }
                            Text(
                                text = "${item.quantity}× ${CurrencyFormatter.rupiah(item.price)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = OkeTextPrimary
                            )
                        }
                    }
                    HorizontalDivider(color = OkeDivider)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subtotal item",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OkeTextSecondary
                        )
                        Text(
                            text = CurrencyFormatter.rupiah(order.totalShopping),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = OkeTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(
    label: String,
    amount: BigDecimal,
    isDiscount: Boolean = false,
    strikethrough: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OkeTextSecondary
        )
        Text(
            text = if (isDiscount) "-${CurrencyFormatter.rupiah(amount)}" else CurrencyFormatter.rupiah(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDiscount) OkeSuccess else if (strikethrough) OkeTextHint else OkeTextPrimary
        )
    }
}
