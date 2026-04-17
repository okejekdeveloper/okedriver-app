package com.application.okedriver.ui.screens.wallet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
 * Wallet & Balance screen.
 *
 * - Gradient balance card (current + withdrawable)
 * - Top Up / Withdraw action buttons
 * - Monthly progress bar
 * - Menu list: Topup History, Transactions, Withdraw History, Bonuses
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    onBackClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet & Balance",
                        fontWeight = FontWeight.SemiBold,
                        color = OkeTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = OkeTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OkeBg
                )
            )
        },
        containerColor = OkeBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Balance card ──────────────────────────────────────────────
            BalanceCard(
                currentBalance = "Rp 300.000",
                withdrawableBalance = "Rp 68.950"
            )

            // ── Action buttons ────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    label = "Top Up",
                    icon = Icons.Rounded.Add,
                    onClick = onTopUpClick,
                    gradientColors = listOf(OkeTopUpGradientStart, OkeTopUpGradientEnd),
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    label = "Withdraw",
                    icon = Icons.Rounded.Upload,
                    onClick = onWithdrawClick,
                    gradientColors = listOf(OkeWithdrawGradientStart, OkeWithdrawGradientEnd),
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Monthly progress ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OkeSurface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Monthly Progress",
                        fontWeight = FontWeight.SemiBold,
                        color = OkeTextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "75%",
                        fontWeight = FontWeight.Bold,
                        color = OkePrimary,
                        fontSize = 14.sp
                    )
                }

                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = OkePrimary,
                    trackColor = OkePrimaryContainer
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Rp 150.000",
                        color = OkeTextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Rp 200.000 target",
                        color = OkeTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // ── Menu list ─────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OkeSurface)
            ) {
                val menuItems = listOf(
                    Triple(Icons.Rounded.History, "Topup History", true),
                    Triple(Icons.AutoMirrored.Rounded.ReceiptLong, "Transactions", false),
                    Triple(Icons.Rounded.Upload, "Withdraw History", false),
                    Triple(Icons.Rounded.CardGiftcard, "Bonuses", false)
                )

                menuItems.forEachIndexed { index, (icon, label, _) ->
                    WalletMenuItem(
                        icon = icon,
                        label = label,
                        onClick = {}
                    )
                    if (index != menuItems.lastIndex) {
                        HorizontalDivider(
                            color = OkeDivider,
                            modifier = Modifier.padding(start = 56.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WalletMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OkePrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = OkePrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            color = OkeTextPrimary,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = OkeTextHint,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun WalletScreenPreview() {
    OkedriverTheme { WalletScreen() }
}
