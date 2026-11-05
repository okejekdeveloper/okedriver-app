package id.okejack.okedriver.feature.wallet.presentation.topup

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.common.ClipboardHelper
import id.okejack.okedriver.core.designsystem.component.OkeButton
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import coil.compose.SubcomposeAsyncImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.content.Intent
import android.net.Uri
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import id.okejack.okedriver.core.common.QrImageSaver
import id.okejack.okedriver.feature.wallet.domain.model.TopUpExpiry
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpDetailScreen(
    depositId: Long,
    onBackClick: () -> Unit,
    initialDeposit: TopUpDeposit? = null,
    viewModel: TopUpDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSavingQr by remember { mutableStateOf(false) }
    val expiresAt = remember(state.deposit?.expiresAt) { TopUpExpiry.parseMillis(state.deposit?.expiresAt.orEmpty()) }
    val remainingSeconds by produceState<Long?>(null, expiresAt) {
        if (expiresAt != null) while (true) {
            value = ((expiresAt - System.currentTimeMillis()) / 1000).coerceAtLeast(0)
            if (value == 0L) break
            delay(1000)
        }
    }
    fun saveQr() {
        if (isSavingQr) return
        val deposit = state.deposit ?: return
        isSavingQr = true
        scope.launch {
            try {
                QrImageSaver.save(context, deposit.qrCodeUrl, "QRIS-OkeDriver-${deposit.transactionId}.png")
                Toast.makeText(context, "QRIS berhasil disimpan ke galeri", Toast.LENGTH_SHORT).show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal menyimpan QRIS", Toast.LENGTH_SHORT).show()
            } finally { isSavingQr = false }
        }
    }
    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) saveQr()
        else Toast.makeText(context, "Izin penyimpanan diperlukan untuk mengunduh QRIS", Toast.LENGTH_SHORT).show()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(depositId) { viewModel.load(depositId, initialDeposit) }
    LaunchedEffect(depositId, lifecycleOwner, state.deposit?.isQris, state.deposit?.isTerminal) {
        if (state.deposit?.isQris == true && state.deposit?.isTerminal == false) {
            lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (isActive) { delay(5_000); viewModel.load(depositId) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Top Up") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.deposit?.let { deposit ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(deposit.status.ifBlank { "Menunggu pembayaran" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        DetailRow("No. Order", "DEP-${deposit.transactionId}")
                        DetailRow("Tanggal", deposit.date)
                        DetailRow("Nominal", deposit.amount)
                        DetailRow("Kode unik", deposit.uniqueCode)
                        DetailRow("Biaya admin", deposit.adminFee)
                        DetailRow("Total", deposit.total)
                        DetailRow("Metode", deposit.paymentName)
                        DetailRow("Berlaku sampai", deposit.expiresAt)
                        if (deposit.note.isNotBlank()) Text(deposit.note, style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (deposit.isQris && !deposit.isTerminal && remainingSeconds != 0L) {
                    if (deposit.qrCodeUrl.isNotBlank()) {
                        SubcomposeAsyncImage(
                            model = deposit.qrCodeUrl,
                            contentDescription = "QRIS top up saldo",
                            modifier = Modifier.size(280.dp).align(Alignment.CenterHorizontally).background(Color.White).padding(12.dp),
                            loading = { CircularProgressIndicator() },
                            error = { Text("QRIS gagal dimuat. Periksa koneksi lalu perbarui detail.", color = Color.Black) }
                        )
                        Text("Scan QRIS untuk membayar. Status pembayaran diperiksa otomatis.")
                        remainingSeconds?.let { seconds -> Text("Sisa waktu: ${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}") }
                        OkeButton(text = "Unduh QRIS", isLoading = isSavingQr, onClick = {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                            ) storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            else saveQr()
                        })
                    } else {
                        Text("QRIS belum tersedia. Perbarui detail atau hubungi bantuan.")
                    }
                }
                if (deposit.isQris && !deposit.isPaid && remainingSeconds == 0L) Text("Waktu pembayaran QRIS sudah habis")
            }
            state.virtualAccount?.let { va ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nomor pembayaran", style = MaterialTheme.typography.titleMedium)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(va.number, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                ClipboardHelper.copyText(context, "Nomor pembayaran", va.number)
                                Toast.makeText(context, "Nomor pembayaran disalin", Toast.LENGTH_SHORT).show()
                            }) { Icon(Icons.Rounded.ContentCopy, contentDescription = "Salin") }
                        }
                        DetailRow("Bank", va.bank)
                        DetailRow("Nama", va.accountName)
                        DetailRow("Berlaku sampai", va.expiresAt)
                    }
                }
            }
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            OkeButton(
                text = if (state.deposit?.isQris == true || state.deposit?.isTerminal == true || state.virtualAccount != null) "Perbarui detail" else "Lanjut ke pembayaran",
                onClick = {
                    if (state.virtualAccount == null && state.deposit?.isQris == false && state.deposit?.isTerminal == false) viewModel.createPayment(depositId)
                    else viewModel.load(depositId)
                },
                enabled = state.deposit != null,
                isLoading = state.isLoading || state.isCreatingPayment
            )
            TextButton(onClick = {
                val deposit = state.deposit
                val message = "Halo Admin Oke Jack, saya butuh bantuan top up saldo. No. Order: DEP-${deposit?.transactionId.orEmpty()}, Nominal: ${deposit?.total.orEmpty()}."
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/62817381500?text=${Uri.encode(message)}"))) }
                    .onFailure { Toast.makeText(context, "Tidak dapat membuka bantuan", Toast.LENGTH_SHORT).show() }
            }) { Text("Bantuan / Konfirmasi Pembayaran") }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 16.dp))
    }
}
