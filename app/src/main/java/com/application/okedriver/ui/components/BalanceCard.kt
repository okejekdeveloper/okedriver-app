package com.application.okedriver.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*

/**
 * Premium glassmorphism-inspired balance card.
 * Features decorative background art, balance visibility toggle, and pill badge.
 */
@Composable
fun BalanceCard(
    currentBalance: String,
    withdrawableBalance: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(OkeCardGradientStart, OkeCardGradientEnd)
) {
    var isVisible by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(OkeShapeCard)
            .background(brush = Brush.linearGradient(gradientColors))
    ) {
        // ── Decorative Background Art ─────────────────────────────────────
        Canvas(modifier = Modifier.matchParentSize()) {
            translate(left = size.width * 0.7f, top = -size.height * 0.2f) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.6f
                )
            }
            translate(left = size.width * 0.9f, top = size.height * 0.6f) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = size.minDimension * 0.4f
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Main Balance",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Icon(
                            imageVector = if (isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            contentDescription = "Toggle Visibility",
                            tint = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { isVisible = !isVisible }
                        )
                    }
                    Text(
                        text = if (isVisible) currentBalance else "Rp ••••••••",
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(OkeShapeSmall)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Withdrawable Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(OkeShapeSmall)
                    .background(Color.Black.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Withdrawable",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = if (isVisible) withdrawableBalance else "Rp •••.•••",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceCardPreview() {
    OkedriverTheme {
        Box(modifier = Modifier.padding(20.dp).background(OkeBg)) {
            BalanceCard(
                currentBalance = "Rp 300.000",
                withdrawableBalance = "Rp 68.950"
            )
        }
    }
}
