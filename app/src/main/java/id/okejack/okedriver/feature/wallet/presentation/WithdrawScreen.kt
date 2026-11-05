package id.okejack.okedriver.feature.wallet.presentation

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*

@Composable
fun WithdrawScreen(onBackClick: () -> Unit, viewModel: WithdrawViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmConversion by remember { mutableStateOf<Boolean?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewModel.loadData() }
    confirmConversion?.let { conversion ->
        AlertDialog(onDismissRequest = { confirmConversion = null },
            title = { Text(if (conversion) "Pindahkan ke saldo Oke Driver?" else "Ajukan penarikan ke bank?") },
            text = { Text("${CurrencyFormatter.rupiah(state.amountValue)}\n\n" + if (conversion)
                "Uang akan dipindahkan ke saldo operasional Oke Driver."
                else "Uang akan diajukan ke rekening bank yang terdaftar di akun Anda. Pastikan rekening sudah benar.") },
            confirmButton = { TextButton(onClick = {
                confirmConversion = null
                viewModel.submit(convertToBalance = conversion, onSuccess = { showSuccess = true })
            }) { Text(if (conversion) "Ya, pindahkan" else "Ya, ajukan penarikan") } },
            dismissButton = { TextButton(onClick = { confirmConversion = null }) { Text("Periksa lagi") } })
    }
    if (showSuccess) AlertDialog(onDismissRequest = {}, title = { Text(if (state.wasConversion) "Saldo berhasil dipindahkan" else "Permintaan terkirim") },
        text = { Text(if (state.wasConversion) "Periksa saldo operasional Anda di halaman Saldo." else "Penarikan Anda sedang diproses. Statusnya tersedia di riwayat penarikan.") },
        confirmButton = { TextButton(onClick = onBackClick) { Text("Kembali ke saldo") } })
    Scaffold(topBar = { OkeTopBar("Tarik saldo", onBackClick) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState())) {
            DriverContent {
                if (state.isLoading) CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                state.errorMessage?.let { DriverNotice(it, if (!state.hasLoadedData) "Coba lagi" else null, viewModel::loadData, true) }
                DriverCard {
                    Text("Saldo yang bisa ditarik", style = MaterialTheme.typography.bodyLarge)
                    Text(if (state.hasLoadedData) CurrencyFormatter.rupiah(state.withdrawableBalance) else "Belum tersedia",
                        style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
                if (state.hasPendingRequest) {
                    DriverNotice("Ada ${state.pendingCount} permintaan penarikan yang masih diproses. Tunggu sampai selesai sebelum membuat permintaan baru.")
                    OkeOutlinedButton("Kembali ke saldo", onBackClick)
                } else if (state.hasLoadedData) {
                    DriverCard {
                        Text("Berapa yang ingin ditarik?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(state.amount, viewModel::onAmountChange, Modifier.fillMaxWidth(), label = { Text("Jumlah penarikan") },
                            prefix = { Text("Rp ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, enabled = !state.isSubmitting)
                        listOf(50_000L, 100_000L, 250_000L, 500_000L).chunked(2).forEach { amounts ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { amounts.forEach { amount ->
                                OkeOutlinedButton(CurrencyFormatter.rupiah(amount), { viewModel.onAmountChange(amount.toString()) },
                                    Modifier.weight(1f), enabled = !state.isSubmitting)
                            } }
                        }
                        Text("Maksimal Rp 1.000.000 per penarikan. Rekening tujuan dapat diperiksa melalui Akun → Ubah profil dan rekening.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OkeButton("Tarik ke rekening bank", { confirmConversion = false },
                            enabled = state.amountValue.signum() > 0, isLoading = state.isSubmitting)
                    }
                    DriverCard {
                        Text("Ingin dipakai untuk operasional?", style = MaterialTheme.typography.titleMedium)
                        Text("Jumlah di atas juga bisa dipindahkan ke saldo Oke Driver. Minimal Rp 100.",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OkeOutlinedButton("Pindahkan ke saldo Oke Driver", { confirmConversion = true },
                            enabled = !state.isSubmitting && state.amountValue.signum() > 0)
                    }
                }
            }
        }
    }
}
