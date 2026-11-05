package id.okejack.okedriver.feature.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import id.okejack.okedriver.R
import id.okejack.okedriver.core.designsystem.theme.OkeSuccess
import id.okejack.okedriver.core.service.DriverLatLng

private const val DRIVER_MAP_ZOOM = 15f
private val INDONESIA_CENTER = LatLng(-2.5, 118.0)
private const val INDONESIA_ZOOM = 4.5f

/**
 * Map inside the home dashboard, including when a position is not yet known.
 *
 * [showMyLocation] must only be true when location permission and consent are both held.
 * [padding] reserves space for any controls placed over the map by its caller.
 */
@Composable
fun HomeMapSurface(location: DriverLatLng?, showMyLocation: Boolean, refreshKey: Int, padding: PaddingValues) {
    val camera = rememberCameraPositionState {
        position = location?.let { CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), DRIVER_MAP_ZOOM) }
            ?: CameraPosition.fromLatLngZoom(INDONESIA_CENTER, INDONESIA_ZOOM)
    }
    // refreshKey is also bumped by the refresh button, so refreshing re-centres on the driver.
    LaunchedEffect(location, refreshKey) {
        location?.let { camera.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), DRIVER_MAP_ZOOM)) }
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camera,
        contentPadding = padding,
        properties = MapProperties(isMyLocationEnabled = showMyLocation),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false)
    )
}

/** Greeting and refresh, floating over the map. */
@Composable
fun BoxScope.HomeMapOverlay(state: HomePresentation, actions: HomeActions, showLogo: Boolean) {
    Row(
        Modifier.align(Alignment.TopStart).fillMaxWidth().statusBarsPadding().padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp,
            modifier = Modifier.weight(1f, fill = false)) {
            Row(Modifier.padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (showLogo) Image(painterResource(R.drawable.oke_driver_mark), null, Modifier.size(28.dp))
                val online = state.driver?.isOnline == true
                Box(Modifier.size(10.dp).background(if (online) OkeSuccess else MaterialTheme.colorScheme.outline, CircleShape))
                Column {
                    Text(state.driver?.name?.takeIf(String::isNotBlank)?.let { "Halo, ${it.substringBefore(' ')}" } ?: "Oke Driver",
                        style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(when { state.driver == null -> "Memuat akun"; online -> "Online"; else -> "Offline" },
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, shadowElevation = 4.dp) {
            IconButton(onClick = actions.onRefresh, enabled = !state.isLoading, modifier = Modifier.size(48.dp)) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else Icon(Icons.Rounded.Refresh, "Perbarui beranda")
            }
        }
    }
}
