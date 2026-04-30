package com.application.okedriver.ui.screens.withdraw

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.application.okedriver.core.designsystem.theme.*
import com.application.okedriver.ui.components.GradientButton

/**
 * Withdraw Request screen.
 *
 * - Shows current withdrawable balance
 * - Fillable amount input (numeric)
 * - Inline validation: amount cannot exceed balance
 * - Submit button disabled until amount is valid
 * - Success dialog on submission
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    currentBalance: Double = 68_950.0,      // withdrawable balance passed from wallet
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {}
) {
    var amountInput by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // ── Derived state ─────────────────────────────────────────────────────────
    val enteredAmount = amountInput.toLongOrNull()?.toDouble() ?: 0.0
    val isExceedingBalance = enteredAmount > currentBalance
    val isAmountEmpty = amountInput.isBlank()
    val isValid = !isAmountEmpty && !isExceedingBalance && enteredAmount > 0

    // ── Success dialog ────────────────────────────────────────────────────────
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = {
                Text(text = "✅", fontSize = 40.sp)
            },
            title = {
                Text(
                    text = "Request Submitted",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Your withdraw request of ${enteredAmount.toRupiahDisplay()} has been submitted and is being processed.",
                    textAlign = TextAlign.Center,
                    color = OkeTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onSubmitSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OkePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = OkeSurface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Withdraw Request",
                        fontWeight = FontWeight.SemiBold,
                        color = OkeTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = OkeTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OkeBg)
            )
        },
        containerColor = OkeBg
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Spacer(modifier = Modifier.height(8.dp))

            // ── Withdrawable balance card ─────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OkePrimaryContainer)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OkePrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = OkePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Withdrawable Balance",
                        color = OkePrimary.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currentBalance.toRupiahDisplay(),
                        color = OkePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            // ── Amount input card ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(OkeSurface)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Withdraw Amount",
                    color = OkeTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { value ->
                        // Only allow numeric input, no leading zeros
                        if (value.isEmpty() || (value.all { it.isDigit() } && value.length <= 12)) {
                            amountInput = value.trimStart('0').ifEmpty { if (value == "0") "" else "" }
                                .let { if (value.isEmpty()) "" else value.filter { c -> c.isDigit() } }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(text = "Enter amount (Rp)", color = OkeTextHint)
                    },
                    prefix = {
                        Text(
                            text = "Rp ",
                            color = OkeTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    isError = isExceedingBalance,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OkePrimary,
                        unfocusedBorderColor = OkeInputBorder,
                        errorBorderColor = OkeDanger,
                        focusedLabelColor = OkePrimary,
                        cursorColor = OkePrimary,
                        focusedContainerColor = OkeInputBg,
                        unfocusedContainerColor = OkeInputBg
                    )
                )

                // ── Inline error message ──────────────────────────────────
                AnimatedVisibility(
                    visible = isExceedingBalance,
                    enter = fadeIn() + expandVertically()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(OkeDanger.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = OkeDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Amount exceeds withdrawable balance (${currentBalance.toRupiahDisplay()})",
                            color = OkeDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ── Remaining balance preview ─────────────────────────────
                AnimatedVisibility(
                    visible = isValid,
                    enter = fadeIn() + expandVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Balance after withdrawal",
                            color = OkeTextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = (currentBalance - enteredAmount).toRupiahDisplay(),
                            color = OkeSuccess,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ── Quick amount chips ────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Quick Amount",
                    color = OkeTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickAmounts = listOf(10_000L, 25_000L, 50_000L, 100_000L)
                    quickAmounts.forEach { amount ->
                        val isEnabled = amount <= currentBalance
                        FilterChip(
                            selected = amountInput == amount.toString(),
                            onClick = { if (isEnabled) amountInput = amount.toString() },
                            label = {
                                Text(
                                    text = "Rp ${amount / 1000}K",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            enabled = isEnabled,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OkePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = OkeSurface,
                                labelColor = OkeTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = isEnabled,
                                selected = amountInput == amount.toString(),
                                borderColor = OkeInputBorder,
                                selectedBorderColor = OkePrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Submit button ─────────────────────────────────────────────
            GradientButton(
                text = "Submit Withdraw Request",
                onClick = { showSuccessDialog = true },
                modifier = Modifier.fillMaxWidth(),
                isLoading = false,
                gradientColors = if (isValid)
                    listOf(OkeWithdrawGradientStart, OkeWithdrawGradientEnd)
                else
                    listOf(OkeTextHint, OkeTextHint)
            )

            // Disabled hint
            if (!isValid && !isAmountEmpty) {
                Text(
                    text = if (isExceedingBalance)
                        "Please enter an amount within your available balance"
                    else
                        "Please enter a valid amount",
                    color = OkeTextHint,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

private fun Double.toRupiahDisplay(): String {
    val long = this.toLong()
    val formatted = StringBuilder()
    val str = long.toString()
    str.reversed().forEachIndexed { i, c ->
        if (i > 0 && i % 3 == 0) formatted.append('.')
        formatted.append(c)
    }
    return "Rp ${formatted.reverse()}"
}

@Preview(showSystemUi = true)
@Composable
private fun WithdrawScreenPreview() {
    OkedriverTheme { WithdrawScreen(currentBalance = 68_950.0) }
}
