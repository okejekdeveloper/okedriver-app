package id.okejack.okedriver.feature.order.presentation.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.common.PolylineDecoder
import id.okejack.okedriver.core.network.api.DirectionsClient
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.core.service.DriverLatLng
import id.okejack.okedriver.core.service.FreshLocationProvider
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderMapViewModel @Inject constructor(
    private val directions: DirectionsClient,
    private val locationProvider: FreshLocationProvider
) : ViewModel() {
    private val _route = MutableStateFlow(emptyList<DriverLatLng>())
    val route: StateFlow<List<DriverLatLng>> = _route
    private var job: Job? = null

    fun load(order: DriverOrder, canUseDeviceLocation: Boolean) {
        job?.cancel()
        _route.value = PolylineDecoder.decode(order.encodedRoute)
        job = viewModelScope.launch {
            val target = coordinate(order.targetLatLng) ?: return@launch
            val device = if (canUseDeviceLocation) (locationProvider.get() as? ApiResponse.Success)?.data else null
            val origin = device ?: coordinate(order.originLatLng) ?: return@launch
            if (_route.value.isEmpty() || device != null) {
                val points = directions.route(origin, target)
                if (points.isNotEmpty()) _route.value = points
            }
        }
    }

    private fun coordinate(value: String): DriverLatLng? {
        val parts = value.split(",").map { it.trim().toDoubleOrNull() }
        if (parts.size != 2) return null
        val lat = parts[0] ?: return null
        val lng = parts[1] ?: return null
        return DriverLatLng(lat, lng).takeIf { lat in -90.0..90.0 && lng in -180.0..180.0 && (lat != 0.0 || lng != 0.0) }
    }
}
