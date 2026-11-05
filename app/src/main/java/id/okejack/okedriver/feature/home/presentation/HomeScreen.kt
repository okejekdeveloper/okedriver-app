package id.okejack.okedriver.feature.home.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import id.okejack.okedriver.core.common.PackageHelper
import id.okejack.okedriver.core.service.DriverLatLng
import id.okejack.okedriver.core.service.LocationHelper
import id.okejack.okedriver.core.service.LocationAccess
import id.okejack.okedriver.core.designsystem.component.LocalLocationConsent
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    onOpenBalance: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenCurrentOrder: (Long) -> Unit,
    onOpenAttendance: () -> Unit = {},
    onOpenRecentOrders: () -> Unit = {},
    onOpenOkeFast: () -> Unit = {},
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val locationConsent = LocalLocationConsent.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var readinessRefreshKey by remember { mutableIntStateOf(0) }
    var mapRefreshKey by remember { mutableIntStateOf(0) }
    var mapLocation by remember { mutableStateOf<DriverLatLng?>(null) }
    var shouldGoOnlineAfterPermission by remember { mutableStateOf(false) }
    val hasLocationPermission = remember(readinessRefreshKey, state.driver?.isOnline, locationConsent.granted) {
        LocationAccess.canAccess(context)
    }
    val isLocationEnabled = remember(readinessRefreshKey, state.driver?.isOnline) {
        LocationHelper.isLocationEnabled(context)
    }
    val hasNotificationPermission = remember(readinessRefreshKey, state.driver?.isOnline) {
        LocationHelper.hasNotificationPermission(context)
    }

    fun toggleOnlineStatusWithService() {
        viewModel.toggleOnlineStatus { isOnline ->
            if (isOnline) {
                LocationHelper.startOnlineDriverService(context)
            } else {
                LocationHelper.stopOnlineDriverService(context)
            }
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        readinessRefreshKey++
        val continueOnline = shouldGoOnlineAfterPermission
        shouldGoOnlineAfterPermission = false
        if (isGranted && continueOnline) {
            if (
                LocationHelper.hasForegroundLocationPermission(context) &&
                LocationHelper.isLocationEnabled(context)
            ) {
                toggleOnlineStatusWithService()
            } else {
                viewModel.showError("Lokasi belum siap")
            }
        } else if (!isGranted && continueOnline) {
            viewModel.showError("Izin notifikasi diperlukan untuk status online")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        readinessRefreshKey++
        mapRefreshKey++
        val continueOnline = shouldGoOnlineAfterPermission
        val hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission && continueOnline) {
            if (!LocationHelper.isLocationEnabled(context)) {
                shouldGoOnlineAfterPermission = false
                viewModel.showError("GPS belum aktif")
            } else if (
                !LocationHelper.hasNotificationPermission(context) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                shouldGoOnlineAfterPermission = false
                toggleOnlineStatusWithService()
            }
        } else {
            shouldGoOnlineAfterPermission = false
        }
        if (!hasLocationPermission && continueOnline) {
            viewModel.showError("Izin lokasi diperlukan untuk online")
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                readinessRefreshKey++
                viewModel.refreshDriverInfo()
                viewModel.refreshSupportInfo()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.driver?.isOnline, hasLocationPermission, isLocationEnabled) {
        when (state.driver?.isOnline) {
            true -> {
                if (
                    hasLocationPermission &&
                    isLocationEnabled
                ) {
                    LocationHelper.startOnlineDriverService(context)
                }
            }
            false -> LocationHelper.stopOnlineDriverService(context)
            null -> Unit
        }
    }

    LaunchedEffect(
        readinessRefreshKey,
        mapRefreshKey,
        hasLocationPermission,
        isLocationEnabled
    ) {
        if (hasLocationPermission && isLocationEnabled) {
            LocationHelper.requestLastKnownLocation(context) { location ->
                mapLocation = location
            }
        } else {
            mapLocation = null
        }
    }

    fun requestOnlineToggle() {
        val driver = state.driver ?: return
        if (driver.isOnline) {
            toggleOnlineStatusWithService()
            return
        }

        if (!LocationAccess.hasConsent(context)) {
            locationConsent.request { readinessRefreshKey++; requestOnlineToggle() }
            return
        }

        if (!isLocationEnabled) {
            viewModel.showError("GPS belum aktif")
            return
        }

        if (!hasLocationPermission) {
            shouldGoOnlineAfterPermission = true
            locationConsent.request { locationPermissionLauncher.launch(locationPermissions()) }
            return
        }

        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            shouldGoOnlineAfterPermission = true
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        toggleOnlineStatusWithService()
    }

    DriverHomeContent(
        driver = state.driver, supportInfo = state.supportInfo,
        isLoading = state.isLoading || state.isLoadingSupport,
        isTogglingStatus = state.isTogglingStatus, isRegisteringQueue = state.isRegisteringQueue,
        errorMessage = state.errorMessage,
        hasLocationPermission = hasLocationPermission, isLocationEnabled = isLocationEnabled,
        hasNotificationPermission = hasNotificationPermission,
        onToggle = ::requestOnlineToggle,
        onRefresh = { viewModel.refreshDriverInfo(); viewModel.refreshSupportInfo(); mapRefreshKey++ },
        onOpenBalance = onOpenBalance,
        onOpenCurrentOrder = { state.supportInfo.activeOrderId?.let(onOpenCurrentOrder) },
        onOpenAttendance = onOpenAttendance,
        onRegisterQueue = { terminal -> LocationHelper.requestLastKnownLatLng(context) { viewModel.registerQueue(it, terminal) } },
        onOpenRecentOrders = onOpenRecentOrders, onOpenOkeFast = onOpenOkeFast,
        onOpenNews = onOpenNews, onOpenSettings = onOpenSettings,
        onRequestLocationPermission = {
            shouldGoOnlineAfterPermission = false
            locationConsent.request { locationPermissionLauncher.launch(locationPermissions()) }
        },
        onOpenLocationSettings = { LocationHelper.openLocationSettings(context) },
        onRequestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                shouldGoOnlineAfterPermission = false
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)))
        },
        updateLabel = if (state.supportInfo.hasAppUpdate(PackageHelper.versionCode(context)))
            state.supportInfo.latestVersionName.ifBlank { "Oke Driver terbaru" } else null,
        onUpdate = { PackageHelper.openPlayStore(context) }
    ) { padding ->
        val canShowLocation = hasLocationPermission && isLocationEnabled
        HomeMapSurface(
            location = if (canShowLocation) mapLocation else null,
            showMyLocation = canShowLocation,
            refreshKey = mapRefreshKey,
            padding = padding
        )
    }
}

private fun locationPermissions(): Array<String> = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
)
