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
import androidx.compose.material.icons.rounded.Help
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
import com.application.okedriver.ui.components.toRupiah
import com.application.okedriver.ui.model.OrderHistorySampleData
import com.application.okedriver.ui.model.PaymentMethod
import com.application.okedriver.ui.model.VehicleType

/**
 * Order Detail screen.
 *
 * Sections:
 *  A. Purple gradient header with vehicle icon, order ID, service name
 *  B. Route card: pickup (purple dot) → destination (square)
 *  C. Payment summary: "Ringkasan Pembayaran" + Cash badge
 *  D. Price breakdown table (Fee Total / Service Fee / Total)
 *  E. Blue merchant payment container
 */
@Composable
fun OrderDetailScreen(
    orderId: String,
    onBackClick: () -> Unit = {}
) {
    val order = OrderHistorySampleData.findById(orderId)
        ?: return // Guard in case of invalid ID

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkeBg)
            .verticalScroll(rememberScrollState())
    ) {

        // ── A. Purple gradient header ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(OkeLoginGradientBottom, OkePrimary)
                    )
                )
                .statusBarsPadding()
                .padding(24.dp)
        ) {
            // Floating back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            // Center content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large vehicle icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.20f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (order.vehicleType == VehicleType.CAR)
                            Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }

                Text(
                    text = "#${order.id}",
                    color = Color.White.copy(alpha = 0.80f),
                    fontSize = 13.sp
                )
                Text(
                    text = order.serviceType,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── B. Route card ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(OkeSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Route",
                color = OkeTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Pickup row
            RoutePoint(
                markerType = MarkerType.CIRCLE,
                label = "Pickup",
                address = order.pickupAddress,
                markerColor = OkePrimary
            )

            // Connecting line
            Box(
                modifier = Modifier
                    .padding(start = 9.dp)
                    .width(2.dp)
                    .height(24.dp)
                    .background(OkeDivider)
            )

            // Destination row
            RoutePoint(
                markerType = MarkerType.SQUARE,
                label = "Destination",
                address = order.dropAddress,
                markerColor = OkePrimaryVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── C & D. Payment summary card ───────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(OkeSurface)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header row: title + payment badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ringkasan Pembayaran",
                    color = OkeTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                // Cash badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(OkeCashBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (order.paymentMethod) {
                            PaymentMethod.CASH     -> "Cash"
                            PaymentMethod.TRANSFER -> "Transfer"
                            PaymentMethod.OVO      -> "OVO"
                            PaymentMethod.DANA     -> "Dana"
                        },
                        color = OkeCashText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            HorizontalDivider(color = OkeDivider)

            // Price rows
            PaymentRow(label = "Fee Total",    value = order.price)
            PaymentRow(label = "Service Fee",  value = order.serviceFee)

            HorizontalDivider(color = OkeDivider)

            PaymentRow(
                label = "Total",
                value = order.price,
                isBold = true,
                labelColor = OkeTextPrimary,
                valueColor = OkePrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── E. Merchant payment container (blue) ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(OkeMerchantBg)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Help,
                    contentDescription = null,
                    tint = OkeMerchantText,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Total Payment To Merchant",
                    color = OkeMerchantText,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
            Text(
                text = order.merchantPayment.toRupiah(),
                color = OkeMerchantText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Private helper composables ────────────────────────────────────────────────

private enum class MarkerType { CIRCLE, SQUARE }

@Composable
private fun RoutePoint(
    markerType: MarkerType,
    label: String,
    address: String,
    markerColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Marker (circle or square)
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(20.dp)
                .clip(if (markerType == MarkerType.CIRCLE) CircleShape else RoundedCornerShape(4.dp))
                .background(markerColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(if (markerType == MarkerType.CIRCLE) CircleShape else RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = OkeTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = address,
                color = OkeTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun PaymentRow(
    label: String,
    value: Double,
    isBold: Boolean = false,
    labelColor: Color = OkeTextSecondary,
    valueColor: Color = OkeTextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = labelColor,
            fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 14.sp
        )
        Text(
            text = value.toRupiah(),
            color = valueColor,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
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
