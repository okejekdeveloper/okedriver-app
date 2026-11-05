package id.okejack.okedriver.feature.order.presentation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import id.okejack.okedriver.core.designsystem.component.LocalLocationConsent
import id.okejack.okedriver.core.service.LocationAccess
import id.okejack.okedriver.feature.order.domain.model.DriverOrder

private const val TARGET_ZOOM = 15f
private const val ROUTE_FIT_PADDING_PX = 96
private val INDONESIA_CENTER = LatLng(-2.5, 118.0)
private const val INDONESIA_ZOOM = 4.5f

/**
 * Full-screen map for the active order: destination marker, route, and the driver's own position.
 * [mapPadding] keeps Google's controls and the camera centre clear of the sheet that covers the lower part.
 */
@Composable
fun OrderMapSurface(order: DriverOrder, mapPadding: PaddingValues, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mapViewModel: OrderMapViewModel = hiltViewModel()
    val route by mapViewModel.route.collectAsStateWithLifecycle()
    val locationConsent = LocalLocationConsent.current
    val target = remember(order.targetLatLng) { parseLatLng(order.targetLatLng) }
    // The blue dot needs both runtime permission and the driver's consent; never ask the map for it otherwise.
    val showMyLocation = remember(locationConsent.granted) { LocationAccess.canAccess(context) }
    var mapLoaded by remember { mutableStateOf(false) }

    val camera = rememberCameraPositionState {
        position = if (target != null) CameraPosition.fromLatLngZoom(target, TARGET_ZOOM)
        else CameraPosition.fromLatLngZoom(INDONESIA_CENTER, INDONESIA_ZOOM)
    }

    LaunchedEffect(order.id, order.status, order.targetLatLng, order.encodedRoute, locationConsent.granted) {
        mapViewModel.load(order, locationConsent.granted)
    }
    // newLatLngBounds throws before the first layout pass, so wait for the map to report it has loaded.
    LaunchedEffect(target, route, mapLoaded) {
        if (!mapLoaded || target == null) return@LaunchedEffect
        val points = route.map { LatLng(it.latitude, it.longitude) } + target
        if (points.size > 1) {
            val bounds = LatLngBounds.builder().apply { points.forEach(::include) }.build()
            camera.animate(CameraUpdateFactory.newLatLngBounds(bounds, ROUTE_FIT_PADDING_PX))
        } else {
            camera.animate(CameraUpdateFactory.newLatLngZoom(target, TARGET_ZOOM))
        }
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = camera,
        contentPadding = mapPadding,
        properties = MapProperties(isMyLocationEnabled = showMyLocation),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, mapToolbarEnabled = false),
        onMapLoaded = { mapLoaded = true }
    ) {
        if (route.size > 1) Polyline(points = route.map { LatLng(it.latitude, it.longitude) }, color = MaterialTheme.colorScheme.primary, width = 12f)
        if (target != null) Marker(
            state = remember(target) { MarkerState(position = target) },
            title = order.targetAddress.ifBlank { "Tujuan order" }
        )
    }
}
