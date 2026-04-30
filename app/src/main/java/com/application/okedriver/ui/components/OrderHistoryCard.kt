package com.application.okedriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Premium modernized order history card.
 * Features left status bar, refined typography, and clean surface layout.
 */
@Composable
fun OrderHistoryCard(
    order: OrderHistoryModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = if (order.status == com.application.okedriver.ui.model.OrderStatus.FINISHED)
        OkeSuccess else OkeWarning

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(OkeShapeCard)
            .background(OkeSurface)
            .border(1.dp, OkeCardBorder, OkeShapeCard)
            .clickable { onClick() }
            .height(IntrinsicSize.Min)
    ) {
        // ── Left Status Bar ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .width(5.dp)
                .fillMaxHeight()
                .background(statusColor)
        )

        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OkePrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (order.vehicleType == VehicleType.CAR)
                                Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                            contentDescription = null,
                            tint = OkePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = order.serviceType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OkeTextPrimary
                        )
                        Text(
                            text = "#${order.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OkeTextHint,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(OkeShapeChip)
                        .background(statusColor.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pickup point
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.FiberManualRecord,
                    contentDescription = null,
                    tint = OkePrimary,
                    modifier = Modifier.size(10.dp).padding(top = 4.dp)
                )
                Text(
                    text = order.pickupAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = OkeTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Drop point
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.FiberManualRecord,
                    contentDescription = null,
                    tint = OkeDanger,
                    modifier = Modifier.size(10.dp).padding(top = 4.dp)
                )
                Text(
                    text = order.dropAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = OkeTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = OkeDivider.copy(alpha = 0.6f))

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Distance:",
                        style = MaterialTheme.typography.bodySmall,
                        color = OkeTextHint
                    )
                    Text(
                        text = "${order.distanceKm} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = OkeTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = order.price.toRupiah(),
                    color = OkeOrange,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

internal fun Double.toRupiah(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "Rp ${formatter.format(this)}"
}

@Preview(showBackground = true)
@Composable
private fun OrderHistoryCardPreview() {
    OkedriverTheme {
        Box(modifier = Modifier.padding(16.dp).background(OkeBg)) {
            OrderHistoryCard(
                order = OrderHistorySampleData.orders.first(),
                onClick = {}
            )
        }
    }
}
