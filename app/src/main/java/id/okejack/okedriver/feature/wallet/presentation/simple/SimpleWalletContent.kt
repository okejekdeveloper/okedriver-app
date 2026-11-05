package id.okejack.okedriver.feature.wallet.presentation.simple

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*

import id.okejack.okedriver.feature.wallet.presentation.WalletUiState

@Composable
fun SimpleWalletContent(
    state: WalletUiState, onBack: (() -> Unit)?, onRefresh: () -> Unit,
    onTopUp: () -> Unit, onWithdraw: () -> Unit, onTransactions: () -> Unit,
    onWithdrawHistory: () -> Unit, onTopUpHistory: () -> Unit, onBonus: () -> Unit
) {
    Scaffold(topBar = { OkeTopBar("Saldo", onBack) {
        IconButton(onClick = onRefresh, enabled = !state.isLoading) { Icon(Icons.Rounded.Refresh, "Perbarui saldo") }
    } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            DriverContent {
                state.errorMessage?.let { DriverNotice(it, "Coba lagi", onRefresh, true) }
                DriverCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Saldo Anda", style = MaterialTheme.typography.titleMedium)
                    }
                    if (state.isLoading && state.balance == null) CircularProgressIndicator(Modifier.size(28.dp))
                    else Text(state.balance?.balance?.let(CurrencyFormatter::rupiah) ?: "Belum tersedia",
                        style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Text("Gunakan saldo untuk kebutuhan operasional Anda.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OkeButton("Isi saldo", onTopUp)
                }
                DriverCard {
                    Text("Saldo yang bisa ditarik", style = MaterialTheme.typography.titleMedium)
                    Text(state.balance?.withdrawableBalance?.let(CurrencyFormatter::rupiah) ?: "Belum tersedia",
                        style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Periksa rekening tujuan sebelum menarik uang.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OkeOutlinedButton("Tarik ke rekening bank", onWithdraw)
                }
                Text("Catatan uang Anda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                    Column {
                        DriverMenuRow(Icons.AutoMirrored.Rounded.ReceiptLong, "Semua transaksi", "Lihat uang masuk dan keluar", onTransactions)
                        HorizontalDivider()
                        DriverMenuRow(Icons.Rounded.AddCircleOutline, "Riwayat isi saldo", "Cek status pembayaran isi saldo", onTopUpHistory)
                        HorizontalDivider()
                        DriverMenuRow(Icons.Rounded.AccountBalance, "Riwayat penarikan", "Cek proses transfer ke bank", onWithdrawHistory)
                        HorizontalDivider()
                        DriverMenuRow(Icons.Rounded.CardGiftcard, "Bonus", "Lihat dan tukar bonus Anda", onBonus)
                    }
                }
            }
        }
    }
}
