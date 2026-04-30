package com.application.okedriver.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*

/**
 * Premium route info component for order requests and details.
 * Features custom markers (Circle → Square) and styled dashed connector.
 */
@Composable
fun OrderRouteCard(
    pickupAddress: String,
    dropoffAddress: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // ── Custom Markers & Connector ────────────────────────────────────
        Column(
            modifier = Modifier.width(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Pickup Marker (Circle)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(OkePrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OkePrimary)
                )
            }

            // Connector Line
            Canvas(
                modifier = Modifier
                    .width(1.5.dp)
                    .height(40.dp)
            ) {
                drawLine(
                    color = OkeTextHint.copy(alpha = 0.4f),
                    start = Offset(size.width / 2f, 4.dp.toPx()),
                    end = Offset(size.width / 2f, size.height - 4.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 10f))
                )
            }

            // Drop-off Marker (Square)
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(OkeDanger.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(OkeDanger)
                )
            }
        }

        // ── Address Details ───────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "PICKUP",
                    style = MaterialTheme.typography.labelSmall,
                    color = OkePrimary,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = pickupAddress,
                    style = MaterialTheme.typography.titleMedium,
                    color = OkeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "DESTINATION",
                    style = MaterialTheme.typography.labelSmall,
                    color = OkeDanger,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = dropoffAddress,
                    style = MaterialTheme.typography.titleMedium,
                    color = OkeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
