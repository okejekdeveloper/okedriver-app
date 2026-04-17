package com.application.okedriver.ui.screens.order

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                Text(
                    text = "Incoming Order Request",
                    color = OkeTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                // ── Countdown + car ───────────────────────────────────────
                Box(contentAlignment = Alignment.TopEnd) {
                    CountdownTimer(
                        totalSeconds = 15,
                        onTimeout = onDecline,
                        trackColor = OkePrimaryContainer,
                        progressColor = OkePrimary
                    )
                    Text(
                        text = "🚗",
                        fontSize = 36.sp,
                        modifier = Modifier.offset(x = 28.dp, y = (-8).dp),
                        color = OkePrimary
                    )
                }

                // ── Fare + Distance row ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OrderInfoChip(label = "Estimated fare", value = "Rp 25.000")
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(OkeDivider)
                    )
                    OrderInfoChip(label = "Distance", value = "4.2 km")
                }

                HorizontalDivider(color = OkeDivider)

                // ── Route ─────────────────────────────────────────────────
                OrderRouteCard(
                    pickupAddress = "Jl. Jend. Sudirman No. 123, Jakarta",
                    dropoffAddress = "Grand Indonesia, Jakarta"
                )

                // ── Action buttons ────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OkeDanger
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, OkeDanger)
                    ) {
                        Text("Decline", fontWeight = FontWeight.SemiBold)
                    }

                    GradientButton(
                        text = "Accept",
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        height = 52.dp,
                        gradientColors = listOf(OkeCardGradientStart, OkeCardGradientEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderInfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = OkeTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = OkeTextSecondary,
            fontSize = 11.sp
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun IncomingOrderScreenPreview() {
    OkedriverTheme { IncomingOrderScreen() }
}
