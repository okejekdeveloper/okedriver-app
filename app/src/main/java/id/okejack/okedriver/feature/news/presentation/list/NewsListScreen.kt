package id.okejack.okedriver.feature.news.presentation.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton
import id.okejack.okedriver.feature.news.domain.model.DriverNews
import id.okejack.okedriver.feature.news.presentation.NewsArtwork
import id.okejack.okedriver.feature.news.presentation.NewsMetadata

@Composable
fun NewsListScreen(
    onBackClick: () -> Unit,
    onNewsClick: (Int) -> Unit,
    viewModel: NewsListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NewsListContent(state, onBackClick, onNewsClick, viewModel::refresh, viewModel::loadNextPage)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsListContent(
    state: NewsListUiState,
    onBackClick: () -> Unit,
    onNewsClick: (Int) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore, state.items.size) {
        if (shouldLoadMore && state.errorMessage == null) onLoadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Berita & Pengumuman") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh,
                        enabled = !state.isLoading && !state.isRefreshing && !state.isLoadingMore) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Perbarui berita")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.items.isEmpty() -> Column(
                    Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.Newspaper, null, Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Text(if (state.errorMessage != null) "Berita belum dapat dimuat" else "Belum ada berita",
                        style = MaterialTheme.typography.titleMedium)
                    Text(state.errorMessage ?: "Pengumuman dan informasi terbaru akan tampil di sini.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onRefresh) {
                        Text(if (state.errorMessage != null) "Coba lagi" else "Perbarui berita")
                    }
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "intro") {
                        Surface(shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer) {
                            Row(Modifier.fillMaxWidth().padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Icon(Icons.Rounded.Newspaper, null, Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Kabar untuk mitra", style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Informasi layanan, program, dan pengumuman dari kantor.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                    itemsIndexed(state.items, key = { _, news -> news.id }) { index, news ->
                        NewsListItem(news, featured = index == 0, onClick = { onNewsClick(news.id) })
                    }
                    if (state.isLoadingMore || state.isRefreshing) {
                        item(key = "loading") {
                            Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                    if (state.errorMessage != null) {
                        item(key = "error") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                                TextButton(onClick = onRefresh) { Text("Muat ulang berita") }
                            }
                        }
                    }
                    if (!state.hasMore) {
                        item(key = "end") {
                            Text("Anda sudah melihat semua berita", Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsListItem(news: DriverNews, featured: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        if (featured) NewsArtwork(news.imageUrl, Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NewsMetadata(news)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(news.title, style = if (featured) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    if (news.summary.isNotBlank()) Text(news.summary,
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
                if (!featured) NewsArtwork(news.imageUrl, Modifier.size(80.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Baca selengkapnya", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary)
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
