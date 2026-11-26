package id.okejack.okedriver.feature.news.presentation.detail

import id.okejack.okedriver.core.designsystem.component.DriverButton as Button
import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.feature.news.presentation.NewsArtwork
import id.okejack.okedriver.feature.news.presentation.NewsMetadata
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    newsId: Int,
    onBackClick: () -> Unit,
    viewModel: NewsDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(newsId) { viewModel.load(newsId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Berita") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.news == null -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.errorMessage ?: "Berita tidak ditemukan")
                    TextButton(onClick = { viewModel.load(newsId, force = true) }) { Text("Coba lagi") }
                }
                else -> {
                    val news = state.news!!
                    Column(
                        modifier = Modifier.widthIn(max = 680.dp).fillMaxSize()
                            .align(Alignment.TopCenter).verticalScroll(rememberScrollState()).padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (news.imageUrl.isNotBlank()) {
                            NewsArtwork(news.imageUrl,
                                Modifier.padding(horizontal = 20.dp).fillMaxWidth().aspectRatio(16f / 9f))
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            NewsMetadata(news)
                            Text(
                                text = news.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            SelectionContainer {
                                Text(
                                    text = news.readableContent(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            if (news.actionUrl.isNotBlank()) {
                                Button(
                                    onClick = {
                                        runCatching { uriHandler.openUri(news.actionUrl) }.onFailure {
                                            scope.launch { snackbar.showSnackbar("Tautan tidak dapat dibuka") }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                                    Text("Buka tautan", modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun id.okejack.okedriver.feature.news.domain.model.DriverNews.readableContent(): String {
    val source = content.ifBlank { summary }
    return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
}
