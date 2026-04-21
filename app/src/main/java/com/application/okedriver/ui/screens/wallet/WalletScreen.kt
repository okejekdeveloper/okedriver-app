package com.application.okedriver.ui.screens.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.BalanceCard
import com.application.okedriver.ui.components.QuickActionButton

/**
 * Premium modernized Wallet & Balance screen.
 * Features a high-end gradient header, custom progress visualization, and refined layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBackClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkeBg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Premium Gradient Top Bar ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(OkeLoginGradientTop, OkePrimaryDark)
                    )
                )
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Wallet & Balance",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    BalanceCard(
                        currentBalance = "Rp 300.000",
                        withdrawableBalance = "Rp 68.950"
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Action Grid ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionButton(
                    label = "Top Up",
                    icon = Icons.Rounded.Add,
                    onClick = onTopUpClick,
                    gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Withdraw",
                    icon = Icons.Rounded.CallMade,
                    onClick = onWithdrawClick,
                    gradientColors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0)),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Premium Progress Section ──────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OkeShapeCard,
                colors = CardDefaults.cardColors(containerColor = OkeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Goals",
                                style = MaterialTheme.typography.titleMedium,
                                color = OkeTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Drive more to earn rewards",
                                style = MaterialTheme.typography.labelSmall,
                                color = OkeTextHint
                            )
                        }
                        Text(
                            text = "75%",
                            style = MaterialTheme.typography.titleLarge,
                            color = OkePrimary,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    LinearProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = OkePrimary,
                        trackColor = OkePrimaryContainer,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rp 150.000 earned",
                            style = MaterialTheme.typography.bodySmall,
                            color = OkeTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target: Rp 200.000",
                            style = MaterialTheme.typography.bodySmall,
                            color = OkeTextHint
                        )
                    }
                }
            }

            // ── Menu Options ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OkeShapeCard,
                colors = CardDefaults.cardColors(containerColor = OkeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    val menuItems = listOf(
                        Triple(Icons.Rounded.History, "Refill History", OkePrimary),
                        Triple(Icons.AutoMirrored.Rounded.ReceiptLong, "Transaction List", OkeSuccess),
                        Triple(Icons.Rounded.CallMade, "Payout History", OkeWarning),
                        Triple(Icons.Rounded.CardGiftcard, "Vouchers & Bonuses", Color(0xFFFF4081))
                    )

                    menuItems.forEachIndexed { index, (icon, label, color) ->
                        WalletMenuItem(
                            icon = icon,
                            label = label,
                            iconColor = color,
                            onClick = {}
                        )
                        if (index != menuItems.lastIndex) {
                            HorizontalDivider(
                                color = OkeDivider,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun WalletMenuItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = OkeTextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = OkeTextHint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun WalletScreenPreview() {
    OkedriverTheme { WalletScreen() }
}
