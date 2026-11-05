package id.okejack.okedriver.feature.order.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.feature.order.presentation.components.OrderHistoryCard

@Composable
fun OrderHistoryScreen(
    onBackClick: () -> Unit = {}, onOrderClick: (Long) -> Unit = {},
    viewModel: OrderHistoryViewModel = hiltViewModel(), isRoot: Boolean = false
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnScreenResume { viewModel.loadOrders() }
    OrderHistoryContent(state, if (isRoot) null else onBackClick, { viewModel.loadOrders(it) },
        viewModel::loadMore, onOrderClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryContent(state: OrderHistoryUiState, onBack: (() -> Unit)?, onRefresh: (Boolean) -> Unit,
    onLoadMore: () -> Unit, onOrder: (Long) -> Unit) {
    Scaffold(topBar = { OkeTopBar("Riwayat order", onBack) {
        IconButton(onClick = { onRefresh(state.inProgress) }, enabled = !state.isLoading) { Icon(Icons.Rounded.Refresh, "Perbarui riwayat") }
    } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = if (state.inProgress) 0 else 1, containerColor = MaterialTheme.colorScheme.surface) {
                listOf("Sedang berjalan", "Riwayat").forEachIndexed { index, label ->
                    Tab(selected = state.inProgress == (index == 0), onClick = { if (state.inProgress != (index == 0)) onRefresh(index == 0) },
                        text = { Text(label, style = MaterialTheme.typography.labelLarge) })
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                if (state.isLoading && state.orders.isEmpty()) CircularProgressIndicator(Modifier.align(Alignment.Center))
                else LazyColumn(Modifier.widthIn(max = 680.dp).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.errorMessage?.let { message -> item {
                        DriverNotice(message, "Coba lagi", { onRefresh(state.inProgress) }, true)
                    } }
                    if (state.orders.isEmpty() && state.errorMessage == null) item {
                        DriverEmptyState(if (state.inProgress) "Tidak ada order berjalan" else "Belum ada riwayat order",
                            if (state.inProgress) "Order yang Anda terima akan muncul di sini." else "Order yang selesai atau dibatalkan akan muncul di sini.")
                    }
                    items(state.orders, key = { it.id }) { order -> OrderHistoryCard(order, { onOrder(order.id) }) }
                    if (state.hasMore && state.orders.isNotEmpty()) item {
                        OkeOutlinedButton(if (state.isLoadingMore) "Memuat..." else "Lihat order berikutnya", onLoadMore,
                            enabled = !state.isLoadingMore && !state.isLoading)
                    }
                }
            }
        }
    }
}
