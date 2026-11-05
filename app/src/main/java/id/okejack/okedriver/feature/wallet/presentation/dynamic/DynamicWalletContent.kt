package id.okejack.okedriver.feature.wallet.presentation.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.core.designsystem.component.dynamic.*
import id.okejack.okedriver.feature.wallet.presentation.WalletUiState

@Composable
fun DynamicWalletContent(state: WalletUiState, onBack: (() -> Unit)?, onRefresh: () -> Unit,
    onTopUp: () -> Unit, onWithdraw: () -> Unit, onTransactions: () -> Unit,
    onWithdrawHistory: () -> Unit, onTopUpHistory: () -> Unit, onBonus: () -> Unit) {
    Scaffold(topBar = { OkeTopBar("Saldo", onBack) {
        IconButton(onClick = onRefresh, enabled = !state.isLoading) { Icon(Icons.Rounded.Refresh, "Perbarui saldo") }
    } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            DriverContent {
                state.errorMessage?.let { DriverNotice(it, "Coba lagi", onRefresh, true) }
                DynamicHero {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AccountBalanceWallet, null, tint = Color.White)
                        Text("Saldo Oke Driver", style = MaterialTheme.typography.titleMedium)
                    }
                    if (state.isLoading && state.balance == null) CircularProgressIndicator(Modifier.size(28.dp), color = Color.White)
                    else Text(state.balance?.balance?.let(CurrencyFormatter::rupiah) ?: "Belum tersedia",
                        style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Teman perjalanan Anda setiap hari.", style = MaterialTheme.typography.bodyLarge)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DynamicActionTile(Icons.Rounded.AddCircleOutline, "Isi saldo", onTopUp, Modifier.weight(1f))
                    DynamicActionTile(Icons.Rounded.AccountBalance, "Tarik ke bank", onWithdraw, Modifier.weight(1f), tone = 1)
                }
                DynamicDriverCard {
                    Text("Siap ditarik ke rekening", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(state.balance?.withdrawableBalance?.let(CurrencyFormatter::rupiah) ?: "Belum tersedia",
                        style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    OkeOutlinedButton("Tarik ke rekening bank", onWithdraw)
                }
                Text("Jejak transaksi Anda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) { Column {
                    DriverMenuRow(Icons.AutoMirrored.Rounded.ReceiptLong, "Semua transaksi", "Uang masuk dan keluar", onTransactions)
                    DriverMenuRow(Icons.Rounded.AddCircleOutline, "Riwayat isi saldo", "Cek pembayaran terakhir", onTopUpHistory)
                    DriverMenuRow(Icons.Rounded.AccountBalance, "Riwayat penarikan", "Pantau proses transfer Anda", onWithdrawHistory)
                    DriverMenuRow(Icons.Rounded.CardGiftcard, "Bonus", "Lihat dan tukar bonus", onBonus)
                } }
            }
        }
    }
}
