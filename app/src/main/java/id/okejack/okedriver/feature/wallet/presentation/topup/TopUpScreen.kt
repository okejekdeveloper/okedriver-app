package id.okejack.okedriver.feature.wallet.presentation.topup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.feature.wallet.domain.model.TopUpCatalog
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import kotlinx.coroutines.delay

@Composable
fun TopUpScreen(onBackClick: () -> Unit, onTopUpCreated: (TopUpDeposit) -> Unit,
    viewModel: TopUpViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var time by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { time = System.currentTimeMillis(); delay(30_000) } }
    Scaffold(topBar = { OkeTopBar("Isi saldo", onBackClick) }, bottomBar = {
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 6.dp) {
            Box(Modifier.fillMaxWidth().navigationBarsPadding().imePadding(), contentAlignment = Alignment.Center) {
                Column(Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${CurrencyFormatter.rupiah(state.amount)} · ${state.selectedPaymentMethod?.name ?: "Pilih cara bayar"}",
                        style = MaterialTheme.typography.bodyMedium)
                    OkeButton("Lanjut ke pembayaran", { viewModel.submit(onTopUpCreated) },
                        enabled = state.amount > 0 && state.selectedPaymentMethod?.let { TopUpCatalog.isAvailable(it.id, time) } == true,
                        isLoading = state.isSubmitting)
                }
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            DriverContent {
                state.errorMessage?.let { DriverNotice(it, isError = true) }
                DriverCard {
                    Text("1. Berapa yang ingin diisi?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(if (state.amount > 0) state.amount.toString() else "",
                        { viewModel.selectAmount(it.filter(Char::isDigit).toLongOrNull() ?: 0) },
                        label = { Text("Jumlah isi saldo") }, prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                        enabled = !state.isSubmitting, modifier = Modifier.fillMaxWidth())
                    listOf(50_000L, 100_000L, 150_000L, 200_000L, 500_000L, 1_000_000L).chunked(2).forEach { amounts ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            amounts.forEach { amount ->
                                OkeOutlinedButton(CurrencyFormatter.rupiah(amount), { viewModel.selectAmount(amount) },
                                    Modifier.weight(1f), enabled = !state.isSubmitting)
                            }
                        }
                    }
                }
                Text("2. Pilih cara bayar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("QRIS dan Virtual Account tersedia 24 jam. Transfer bank tersedia pukul 05.00–21.00 WIB.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                state.paymentMethods.forEach { method ->
                    val available = TopUpCatalog.isAvailable(method.id, time)
                    val selected = state.selectedPaymentMethod?.id == method.id
                    Surface(color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().selectable(
                            selected, enabled = available && !state.isSubmitting, role = Role.RadioButton,
                            onClick = { viewModel.selectPaymentMethod(method) })) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            RadioButton(selected, onClick = null, enabled = available && !state.isSubmitting)
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(method.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(TopUpCatalog.description(method.id), style = MaterialTheme.typography.bodyMedium)
                                if (!available) Text("Tersedia lagi pukul 05.00 WIB", style = MaterialTheme.typography.bodyMedium)
                                if (method.accountNumber.isNotBlank()) Text(method.accountNumber, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
