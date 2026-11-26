package id.okejack.okedriver.feature.news.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import id.okejack.okedriver.feature.news.domain.model.DriverNews

@Composable
internal fun NewsArtwork(url: String, modifier: Modifier = Modifier) {
    Box(modifier.clip(MaterialTheme.shapes.medium)) {
        if (url.isBlank()) NewsImagePlaceholder()
        else SubcomposeAsyncImage(model = url, contentDescription = null,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
            loading = { NewsImagePlaceholder() }, error = { NewsImagePlaceholder() })
    }
}

@Composable
private fun NewsImagePlaceholder() {
    Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Newspaper, null, Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NewsMetadata(news: DriverNews) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (news.isPinned) Surface(shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Rounded.PushPin, null, Modifier.size(14.dp))
                Text("Disematkan", style = MaterialTheme.typography.labelSmall)
            }
        }
        if (news.createdAt.isNotBlank()) Text(news.createdAt,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp))
    }
}
