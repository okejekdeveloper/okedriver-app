package com.application.okedriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.model.OrderHistoryModel
import com.application.okedriver.ui.model.OrderHistorySampleData
import com.application.okedriver.ui.model.VehicleType
import java.text.NumberFormat
import java.util.Locale

/**
 * Reusable order history card.
 *
 * Layout:
 *  [Vehicle icon] | [Service + ID + Address] | [Distance]
 *  ──────────────────────────────────────────────────────
 *                                            [Price chip]
 */
@Composable
fun OrderHistoryCard(
    order: OrderHistoryModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(OkeSurface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ── Vehicle icon circle ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(OkePrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (order.vehicleType == VehicleType.CAR)
                        Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                    contentDescription = order.vehicleType.name,
                    tint = OkePrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // ── Middle info ───────────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${order.serviceType} - ${if (order.vehicleType == VehicleType.CAR) "Car" else "Ride"}",
                    color = OkeServiceRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ID ${order.date}/#${order.id}",
                    color = OkeTextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${order.pickupAddress} → ${order.dropAddress}",
                    color = OkeTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
            }

            // ── Distance ──────────────────────────────────────────────────
            Text(
                text = "${order.distanceKm}KM",
                color = OkeTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(
            color = OkeDivider,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        // ── Price chip (right-aligned) ────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = order.status.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                color = if (order.status == com.application.okedriver.ui.model.OrderStatus.FINISHED)
                    OkeSuccess else OkeWarning,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OkeOrangeBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = order.price.toRupiah(),
                    color = OkeOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

internal fun Double.toRupiah(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return "Rp${formatter.format(this)}"
}

@Preview(showBackground = true)
@Composable
private fun OrderHistoryCardPreview() {
    OkedriverTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            OrderHistoryCard(
                order = OrderHistorySampleData.orders.first(),
                onClick = {}
            )
        }
    }
}
