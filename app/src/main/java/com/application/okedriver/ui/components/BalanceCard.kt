package com.application.okedriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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

/**
 * Gradient balance card shown on the Wallet screen.
 * Displays current balance on the left and withdrawable balance on the right.
 */
@Composable
fun BalanceCard(
    currentBalance: String,
    withdrawableBalance: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(OkeCardGradientStart, OkeCardGradientEnd)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(brush = Brush.horizontalGradient(gradientColors))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Current balance (left)
            Column {
                Text(
                    text = "Current Balance",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = currentBalance,
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Withdrawable balance (right)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Withdrawable",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = withdrawableBalance,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceCardPreview() {
    OkedriverTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BalanceCard(
                currentBalance = "Rp 300.000",
                withdrawableBalance = "Rp 68.950"
            )
        }
    }
}
