package id.okejack.okedriver.feature.wallet.presentation

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.designsystem.theme.*
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WalletScreen(
    onBackClick: () -> Unit = {},
    onTopUpClick: () -> Unit = {},
    onWithdrawClick: () -> Unit = {},
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OkeBg)
            .verticalScroll(rememberScrollState())
    ) {
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
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Dompet & Saldo",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = OkeShapeCard,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Total Saldo",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(28.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = (state.balance?.balance ?: BigDecimal.ZERO).toRupiah(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dapat ditarik: ${(state.balance?.withdrawableBalance ?: BigDecimal.ZERO).toRupiah()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (state.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = OkeDangerBg),
                    shape = OkeShapeCard
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = OkeDanger,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.loadBalance() }) {
                            Text("Coba Lagi", color = OkeDanger)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WalletActionButton(
                    label = "Top Up",
                    icon = Icons.Rounded.Add,
                    onClick = onTopUpClick,
                    gradientColors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)),
                    modifier = Modifier.weight(1f)
                )
                WalletActionButton(
                    label = "Tarik Dana",
                    icon = Icons.Rounded.CallMade,
                    onClick = onWithdrawClick,
                    gradientColors = listOf(Color(0xFF00B4DB), Color(0xFF0083B0)),
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = OkeShapeCard,
                colors = CardDefaults.cardColors(containerColor = OkeSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    val menuItems = listOf(
                        Triple(Icons.Rounded.History, "Riwayat Top Up", OkePrimary),
                        Triple(Icons.AutoMirrored.Rounded.ReceiptLong, "Daftar Transaksi", OkeSuccess),
                        Triple(Icons.Rounded.CallMade, "Riwayat Penarikan", OkeWarning),
                        Triple(Icons.Rounded.CardGiftcard, "Voucher & Bonus", Color(0xFFFF4081))
                    )
                    menuItems.forEachIndexed { index, (icon, label, color) ->
                        WalletMenuItem(icon = icon, label = label, iconColor = color, onClick = {})
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
private fun WalletActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(OkeShapeCard)
            .background(Brush.horizontalGradient(gradientColors))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(text = label, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
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
            Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = OkeTextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = OkeTextHint, modifier = Modifier.size(20.dp))
    }
}

private fun BigDecimal.toRupiah(): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 0
    return "Rp ${formatter.format(this)}"
}
