package id.okejack.okedriver.feature.profile.presentation.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import id.okejack.okedriver.core.designsystem.component.dynamic.*
import id.okejack.okedriver.feature.profile.presentation.ProfileUiState

@Composable
fun DynamicProfileHeader(state: ProfileUiState, onEdit: () -> Unit) {
    DynamicHero {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val photo = state.driver?.avatarUrl
            if (!photo.isNullOrBlank()) AsyncImage(photo, "Foto profil", Modifier.size(68.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            else Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                Icon(Icons.Rounded.Person, null, Modifier.padding(16.dp).size(36.dp), tint = Color.White)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(state.driver?.name?.ifBlank { "Mitra driver" } ?: if (state.isLoading) "Memuat akun..." else "Akun belum dimuat",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                state.driver?.let { driver ->
                    Text(driver.phone, style = MaterialTheme.typography.bodyLarge)
                    Text(if (driver.isOnline) "Sedang online" else "Sedang offline", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        state.driver?.let { driver ->
            if (driver.vehiclePlate.isNotBlank()) Text(driver.vehiclePlate, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            val vehicle = listOf(driver.vehicleBrand, driver.vehicleColor).filter(String::isNotBlank).joinToString(" · ")
            if (vehicle.isNotBlank()) Text(vehicle)
        }
        DynamicHeroButton("Ubah profil dan rekening", onEdit)
    }
}
