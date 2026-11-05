package id.okejack.okedriver.feature.wallet.presentation.bonus

import id.okejack.okedriver.core.designsystem.component.DriverButton as Button
import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material3.AlertDialog
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
import id.okejack.okedriver.core.common.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BonusBoxScreen(
    onBackClick: () -> Unit,
    viewModel: BonusBoxViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.revealedBox?.let { box ->
        AlertDialog(
            onDismissRequest = viewModel::dismissResult,
            icon = { Icon(Icons.Rounded.CardGiftcard, contentDescription = null) },
            title = { Text("Bonus dibuka") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(CurrencyFormatter.rupiah(box.amount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    if (box.message.isNotBlank()) Text(box.message)
                }
            },
            confirmButton = { TextButton(onClick = viewModel::dismissResult) { Text("Tutup") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bonus") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator() }
            state.boxes.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.errorMessage ?: "Belum ada bonus")
                if (state.errorMessage != null) TextButton(onClick = viewModel::load) { Text("Coba lagi") }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.boxes, key = { it.id }) { box ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.CardGiftcard, contentDescription = null)
                            Text(box.message.ifBlank { if (box.redeemed) "Bonus sudah dibuka" else "Bonus untuk Anda" }, fontWeight = FontWeight.Bold)
                            if (box.redeemed) {
                                Text(CurrencyFormatter.rupiah(box.amount), style = MaterialTheme.typography.titleLarge)
                            } else {
                                Button(
                                    onClick = { viewModel.open(box.id) },
                                    enabled = state.openingId == null
                                ) {
                                    if (state.openingId == box.id) CircularProgressIndicator()
                                    else Text("Buka bonus")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
