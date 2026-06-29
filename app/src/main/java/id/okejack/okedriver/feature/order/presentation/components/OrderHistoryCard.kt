package id.okejack.okedriver.feature.order.presentation.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.okejack.okedriver.core.designsystem.theme.*
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderHistoryCard(
    order: DriverOrder,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFinished = order.status == DriverOrder.STATUS_ARRIVED ||
            order.status == DriverOrder.STATUS_CANCELLED
    val statusColor = when (order.status) {
        DriverOrder.STATUS_CANCELLED -> OkeDanger
        DriverOrder.STATUS_ARRIVED -> OkeSuccess
        else -> OkeWarning
    }
    val isCar = order.type == DriverOrder.TYPE_CAR || order.type == DriverOrder.TYPE_RENTAL_CAR

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(OkeShapeCard)
            .background(OkeSurface)
            .border(1.dp, OkeCardBorder, OkeShapeCard)
            .clickable { onClick() }
            .height(IntrinsicSize.Min)
    ) {
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
                            imageVector = if (isCar) Icons.Rounded.DirectionsCar else Icons.Rounded.TwoWheeler,
                            contentDescription = null,
                            tint = OkePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = order.serviceLabel,
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
                        text = order.statusLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.FiberManualRecord,
                    contentDescription = null,
                    tint = OkePrimary,
                    modifier = Modifier
                        .size(10.dp)
                        .padding(top = 4.dp)
                )
                Text(
                    text = order.originAddress.ifBlank { "Alamat penjemputan tidak tersedia" },
                    style = MaterialTheme.typography.bodySmall,
                    color = OkeTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.FiberManualRecord,
                    contentDescription = null,
                    tint = OkeDanger,
                    modifier = Modifier
                        .size(10.dp)
                        .padding(top = 4.dp)
                )
                Text(
                    text = order.destinationAddress.ifBlank { "Alamat tujuan tidak tersedia" },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Jarak:",
                        style = MaterialTheme.typography.bodySmall,
                        color = OkeTextHint
                    )
                    Text(
                        text = "${order.distance / 1000} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = OkeTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = order.fee.toRupiah(),
                    color = OkeOrange,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

private fun BigDecimal.toRupiah(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "Rp ${formatter.format(this)}"
}
