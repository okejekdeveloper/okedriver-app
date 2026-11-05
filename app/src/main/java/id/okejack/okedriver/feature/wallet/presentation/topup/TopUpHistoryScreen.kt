package id.okejack.okedriver.feature.wallet.presentation.topup

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpHistoryScreen(
    onBackClick: () -> Unit,
    onDepositClick: (Long) -> Unit,
    viewModel: TopUpHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var search by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.load() }
    }
    if (showDatePicker) {
        val dates = rememberDateRangePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(enabled = dates.selectedStartDateMillis != null && dates.selectedEndDateMillis != null, onClick = {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
                viewModel.applyFilters(search, format.format(java.util.Date(dates.selectedStartDateMillis!!)), format.format(java.util.Date(dates.selectedEndDateMillis!!)))
                showDatePicker = false
            }) { Text("Terapkan") }
        }, dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }) {
            DateRangePicker(state = dates, modifier = Modifier.height(450.dp), showModeToggle = false)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Top Up") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = search, onValueChange = { search = it }, label = { Text("Cari transaksi") }, singleLine = true, modifier = Modifier.weight(1f))
                TextButton(onClick = { viewModel.applyFilters(search) }) { Text("Cari") }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { showDatePicker = true }) { Text(if (state.dateFrom.isBlank()) "Pilih tanggal" else "${state.dateFrom} – ${state.dateTo}") }
                if (state.dateFrom.isNotBlank()) TextButton(onClick = { viewModel.applyFilters(search, "", "") }) { Text("Semua tanggal") }
            }
        when {
            state.isLoading -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            state.errorMessage != null -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::load) { Text("Coba lagi") }
            }
            state.deposits.isEmpty() -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { Text("Belum ada riwayat top up") }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.deposits, key = { it.id }) { deposit ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onDepositClick(deposit.id) }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(deposit.total.ifBlank { deposit.amount }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(deposit.paymentName.ifBlank { deposit.productDetail })
                            Text(deposit.status, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            Text(deposit.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (state.hasMore) item {
                    TextButton(onClick = viewModel::loadMore, enabled = !state.isLoadingMore, modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.isLoadingMore) "Memuat..." else "Muat lagi")
                    }
                }
            }
        }
        }
    }
}
