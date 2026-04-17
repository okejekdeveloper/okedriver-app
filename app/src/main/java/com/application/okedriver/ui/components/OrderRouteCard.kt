package com.application.okedriver.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*

/**
 * Route info card showing pickup → drop-off addresses
 * with colour-coded location dots and a dashed connector line.
 */
@Composable
fun OrderRouteCard(
    pickupAddress: String,
    dropoffAddress: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dot + line column
        Column(
            modifier = Modifier.width(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = OkePickupDot, radius = size.minDimension / 2f)
            }

            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .height(32.dp)
            ) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.4f),
                    start = Offset(size.width / 2f, 0f),
                    end = Offset(size.width / 2f, size.height),
                    strokeWidth = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
            }

            Canvas(modifier = Modifier.size(12.dp)) {
                drawCircle(color = OkeDropoffDot, radius = size.minDimension / 2f)
            }
        }

        // Address column
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(
                    text = "Pickup",
                    color = OkeTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = pickupAddress,
                    color = OkeTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Column {
                Text(
                    text = "Drop-off",
                    color = OkeTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dropoffAddress,
                    color = OkeTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
