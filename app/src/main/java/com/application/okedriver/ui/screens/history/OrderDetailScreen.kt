package com.application.okedriver.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.OrderRouteCard
import com.application.okedriver.ui.components.toRupiah
import com.application.okedriver.ui.model.OrderHistorySampleData
import com.application.okedriver.ui.model.PaymentMethod
import com.application.okedriver.ui.model.VehicleType

/**
 * Premium modernized Order Detail screen.
 * Featuring a diagonal gradient header, modernized route visualization, and refined payment breakdown.
 */
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit = {}
) {
    val order = OrderHistorySampleData.findById(orderId)
        ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkeBg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Premium Diagonal Header ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(OkeLoginGradientMid, OkePrimaryDark)
                    )
                )
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(OkeShapeChip)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#${order.id}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Avatar/Icon Container
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (order.vehicleType == VehicleType.CAR)
                            Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = order.serviceType,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "${order.date} • ${order.status.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Route Section ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OkeShapeCard,
                colors = CardDefaults.cardColors(containerColor = OkeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ROUTE DETAILS",
                        style = MaterialTheme.typography.labelSmall,
                        color = OkeTextHint,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    OrderRouteCard(
                        pickupAddress = order.pickupAddress,
                        dropoffAddress = order.dropAddress
                    )
                }
            }

            // ── Payment Summary ───────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OkeShapeCard,
                colors = CardDefaults.cardColors(containerColor = OkeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PAYMENT SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = OkeTextHint,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        
                        Box(
                            modifier = Modifier
                                .clip(OkeShapeChip)
                                .background(OkeCashBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = order.paymentMethod.name.lowercase().replaceFirstChar { it.uppercase() },
                                color = OkeCashText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PaymentRow(label = "Base Fare", value = order.price)
                    PaymentRow(label = "Service Fee", value = order.serviceFee)

                    HorizontalDivider(
                        color = OkeDivider,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL EARNINGS",
                            style = MaterialTheme.typography.titleMedium,
                            color = OkeTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = order.price.toRupiah(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = OkePrimary,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // ── Merchant Payment ──────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = OkeMerchantBg.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = OkeMerchantText,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Merchant Settlement",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OkeMerchantText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = order.merchantPayment.toRupiah(),
                        style = MaterialTheme.typography.titleMedium,
                        color = OkeMerchantText,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun PaymentRow(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = OkeTextSecondary
        )
        Text(
            text = value.toRupiah(),
            style = MaterialTheme.typography.bodyMedium,
            color = OkeTextPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun OrderDetailScreenPreview() {
    OkedriverTheme {
        OrderDetailScreen(orderId = "3503543")
    }
}
