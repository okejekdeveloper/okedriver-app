package com.application.okedriver.ui.screens.order

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.CountdownTimer
import com.application.okedriver.ui.components.GradientButton
import com.application.okedriver.ui.components.OrderRouteCard

/**
 * Incoming Order Request screen.
 *
 * - Blurred dark map background (Canvas-drawn)
 * - Frosted-glass card overlay with:
 *   - Countdown timer (15s)
 *   - Car emoji illustration
 *   - Estimated fare + distance info row
 *   - Route card (pickup → drop-off)
 *   - Decline / Accept button pair
 */
@Composable
fun IncomingOrderScreen(
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ── Dark blurred map background ───────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .blur(6.dp)
        ) {
            drawRect(OkeDarkBg)
            val gridSpacing = 50.dp.toPx()
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = OkeMapRoad,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
                y += gridSpacing
            }
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = OkeMapRoad,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.dp.toPx()
                )
                x += gridSpacing
            }
            val path = Path().apply {
                moveTo(size.width * 0.10f, size.height * 0.80f)
                lineTo(size.width * 0.10f, size.height * 0.45f)
                lineTo(size.width * 0.55f, size.height * 0.45f)
                lineTo(size.width * 0.55f, size.height * 0.20f)
                lineTo(size.width * 0.90f, size.height * 0.20f)
            }
            drawPath(
                path = path,
                color = OkePrimary.copy(alpha = 0.60f),
                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        // Dark gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            OkeDarkBg.copy(alpha = 0.40f),
                            OkeDarkBg.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        // ── Order card ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White.copy(alpha = 0.96f))
                    .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NEW ORDER REQUEST",
                        style = MaterialTheme.typography.labelMedium,
                        color = OkePrimary,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Incoming Food Delivery",
                        style = MaterialTheme.typography.titleLarge,
                        color = OkeTextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }

                // ── Countdown + car ───────────────────────────────────────
                Box(contentAlignment = Alignment.Center) {
                    CountdownTimer(
                        totalSeconds = 15,
                        onTimeout = onDecline,
                        size = 160.dp,
                        trackColor = OkePrimaryContainer.copy(alpha = 0.5f),
                        progressColor = OkePrimary
                    )
                    
                    // Center Decoration
//                    Box(
//                        modifier = Modifier
//                            .size(80.dp)
//                            .clip(CircleShape)
//                            .background(OkePrimary.copy(alpha = 0.05f)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "🍕", // Dynamic based on type? Let's use food/package based on "Food Delivery"
//                            fontSize = 40.sp
//                        )
//                    }
                }

                // ── Fare + Distance row ───────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(OkeBg.copy(alpha = 0.5f))
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OrderInfoChip(label = "Estimated fare", value = "Rp 25.000", valueColor = OkeSuccess)
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(OkeDivider)
                    )
                    OrderInfoChip(label = "Distance", value = "4.2 km", valueColor = OkePrimary)
                }

                // ── Route ─────────────────────────────────────────────────
                OrderRouteCard(
                    pickupAddress = "Jl. Jend. Sudirman No. 123",
                    dropoffAddress = "Grand Indonesia Mall"
                )

                // ── Action buttons ────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OkeDanger
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, OkeDanger.copy(alpha = 0.3f))
                    ) {
                        Text("Decline", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    GradientButton(
                        text = "Accept Order",
                        onClick = onAccept,
                        modifier = Modifier.weight(1.5f),
                        height = 56.dp,
                        gradientColors = listOf(Color(0xFF10B981), Color(0xFF059669))
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderInfoChip(label: String, value: String, valueColor: Color = OkeTextPrimary) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = OkeTextHint,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun IncomingOrderScreenPreview() {
    OkedriverTheme { IncomingOrderScreen() }
}
