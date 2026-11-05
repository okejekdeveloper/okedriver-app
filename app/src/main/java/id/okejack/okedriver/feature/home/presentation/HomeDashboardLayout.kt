package id.okejack.okedriver.feature.home.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.designsystem.component.DriverContent

/**
 * A work dashboard, rather than a sheet over a map. All sections share one ordinary scroll
 * container; scrolling never expands a panel or changes how much of the map is covered.
 * Hallmark app-screen principles: existing theme tokens, restrained surfaces, task first.
 */
@Composable
fun HomeDashboardLayout(
    state: HomePresentation,
    actions: HomeActions,
    map: @Composable (PaddingValues) -> Unit,
    status: @Composable ColumnScope.() -> Unit,
    details: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.fillMaxWidth().statusBarsPadding(), contentAlignment = Alignment.Center) {
                    Row(
                        Modifier.widthIn(max = 680.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Oke Driver", style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            state.driver?.name?.takeIf(String::isNotBlank)?.let {
                                Text("Halo, ${it.substringBefore(' ')}", style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = actions.onRefresh, enabled = !state.isLoading,
                            modifier = Modifier.size(48.dp)) {
                            if (state.isLoading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                            else Icon(Icons.Rounded.Refresh, "Perbarui beranda")
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
            .verticalScroll(rememberScrollState())) {
            DriverContent {
                Column(Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp), content = status)
                details()
                Text("Posisi Anda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Box(Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.medium)) {
                    map(PaddingValues(0.dp))
                }
            }
        }
    }
}
