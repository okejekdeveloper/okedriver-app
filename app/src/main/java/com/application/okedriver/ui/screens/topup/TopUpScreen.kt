package com.application.okedriver.ui.screens.topup

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.AmountChip
import com.application.okedriver.ui.components.BankCard
import com.application.okedriver.ui.components.GradientButton

private data class AmountOption(val display: String, val value: Long)
private data class Bank(
    val name: String,
    val shortName: String,
    val color: androidx.compose.ui.graphics.Color
)

/**
 * Top Up Deposit screen.
 *
 * - Amount picker card with displayed selected amount
 * - 2-column amount chip grid (6 options)
 * - Horizontal bank / payment method carousel
 * - Confirm button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpScreen(
    onBackClick: () -> Unit = {},
    onConfirmClick: (Long) -> Unit = {}
) {
    val amounts = listOf(
        AmountOption("Rp 50.000", 50_000),
        AmountOption("Rp 100.000", 100_000),
        AmountOption("Rp 150.000", 150_000),
        AmountOption("Rp 200.000", 200_000),
        AmountOption("Rp 500.000", 500_000),
        AmountOption("Rp 1.000.000", 1_000_000)
    )

    val banks = listOf(
        Bank("Bank Central Asia", "BCA", OkeBankBca),
        Bank("Bank Mandiri", "Mandiri", OkeBankMandiri),
        Bank("Bank Negara Indonesia", "BNI", OkeBankBni),
        Bank("Bank Rakyat Indonesia", "BRI", OkeBankBri),
        Bank("Dana", "DANA", OkeBankDana),
        Bank("OVO", "OVO", OkeBankOvo)
    )

    var selectedAmount by remember { mutableStateOf(amounts[1]) }
    var selectedBank by remember { mutableStateOf(banks[0]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Top Up Deposit",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OkeBg)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                GradientButton(
                    text = "Confirm Payment — ${selectedAmount.display}",
                    onClick = { onConfirmClick(selectedAmount.value) },
                    gradientColors = listOf(OkeCardGradientStart, OkeCardGradientEnd)
                )
            }
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
            // ── Amount picker card ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(OkeSurface)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = OkePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Amount Picker",
                        color = OkeTextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = selectedAmount.display,
                    color = OkeTextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // ── Amount grid (2 columns) ───────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Choose Amount",
                    color = OkeTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                amounts.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { option ->
                            AmountChip(
                                amount = option.display,
                                isSelected = selectedAmount == option,
                                onClick = { selectedAmount = option },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Fill empty cell if odd row
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // ── Payment method ────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Payment Method",
                    color = OkeTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    banks.forEach { bank ->
                        BankCard(
                            bankName = bank.name,
                            bankShortName = bank.shortName,
                            brandColor = bank.color,
                            isSelected = selectedBank == bank,
                            onClick = { selectedBank = bank }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun TopUpScreenPreview() {
    OkedriverTheme { TopUpScreen() }
}
