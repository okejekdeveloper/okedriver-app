package id.okejack.okedriver.feature.order.presentation.qris

import id.okejack.okedriver.core.designsystem.component.DriverButton as Button
import id.okejack.okedriver.core.designsystem.component.DriverOutlinedButton as OutlinedButton
import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil.compose.SubcomposeAsyncImage
import id.okejack.okedriver.core.common.CurrencyFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderQrisScreen(
    orderId: Long,
    onBack: () -> Unit,
    onPaymentComplete: () -> Unit,
    viewModel: OrderQrisViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnComplete by rememberUpdatedState(onPaymentComplete)
    var confirmCash by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) { viewModel.generate(orderId) }
    LaunchedEffect(orderId, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (isActive) {
                delay(5_000)
                viewModel.check(orderId)
            }
        }
    }
    LaunchedEffect(state.isComplete) {
        if (state.isComplete) currentOnComplete()
    }

    if (confirmCash) {
        AlertDialog(
            onDismissRequest = { confirmCash = false },
            title = { Text("Ganti ke Pembayaran Tunai") },
            text = { Text("Pastikan customer sudah setuju bayar tunai (cash) sebelum lanjut. Yakin?") },
            confirmButton = {
                TextButton(onClick = { confirmCash = false; viewModel.switchToCash(orderId) }) {
                    Text("Ya, Ganti ke Cash")
                }
            },
            dismissButton = { TextButton(onClick = { confirmCash = false }) { Text("Batal") } }
        )
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Pembayaran QRIS") },
            navigationIcon = { IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Kembali")
            } }
        )
    }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Order #$orderId", style = MaterialTheme.typography.titleLarge)
            state.payment.amount?.let { Text(CurrencyFormatter.rupiah(it), style = MaterialTheme.typography.headlineMedium) }
            Text(if (state.payment.isPaid) "Pembayaran Berhasil" else "Menunggu Pembayaran Customer")
            if (state.payment.qrCodeUrl.isNotBlank() && !state.isComplete) {
                SubcomposeAsyncImage(
                    model = state.payment.qrCodeUrl,
                    contentDescription = "QRIS pembayaran order",
                    modifier = Modifier.size(280.dp).background(Color.White).padding(12.dp),
                    loading = { CircularProgressIndicator() },
                    error = { Text("QRIS gagal dimuat. Periksa koneksi dan coba lagi.", color = Color.Black) }
                )
                Text("Minta customer scan QRIS ini. Status pembayaran diperiksa otomatis.")
            }
            if (state.isBusy) CircularProgressIndicator()
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (!state.isComplete) {
                Button(onClick = { viewModel.check(orderId) }, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                    Text("Cek Pembayaran")
                }
                if (state.payment.qrCodeUrl.isBlank() || state.errorMessage != null) {
                    OutlinedButton(onClick = { viewModel.generate(orderId) }, enabled = !state.isBusy) { Text("Muat ulang QRIS") }
                }
                OutlinedButton(onClick = { confirmCash = true }, enabled = !state.isBusy, modifier = Modifier.fillMaxWidth()) {
                    Text("Ganti ke Pembayaran Tunai")
                }
            }
        }
    }
}
