package id.okejack.okedriver.feature.home.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.OkeButton
import id.okejack.okedriver.core.designsystem.component.OkeOutlinedButton
import id.okejack.okedriver.core.service.LocationHelper
import id.okejack.okedriver.feature.home.domain.model.HomeDriver
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo

@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var readinessRefreshKey by remember { mutableIntStateOf(0) }
    val hasLocationPermission = remember(readinessRefreshKey, state.driver?.isOnline) {
        LocationHelper.hasForegroundLocationPermission(context)
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
        if (isGranted) {
            if (
                LocationHelper.hasForegroundLocationPermission(context) &&
                LocationHelper.isLocationEnabled(context)
            ) {
                toggleOnlineStatusWithService()
            } else {
                viewModel.showError("Lokasi belum siap")
            }
        } else {
            viewModel.showError("Izin notifikasi diperlukan untuk status online")
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        readinessRefreshKey++
        val hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasLocationPermission) {
            if (!LocationHelper.isLocationEnabled(context)) {
                viewModel.showError("GPS belum aktif")
            } else if (
                !LocationHelper.hasNotificationPermission(context) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                toggleOnlineStatusWithService()
            }
        } else {
            viewModel.showError("Izin lokasi diperlukan untuk online")
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                readinessRefreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.driver?.isOnline) {
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

    fun requestOnlineToggle() {
        val driver = state.driver ?: return
        if (driver.isOnline) {
            toggleOnlineStatusWithService()
            return
        }

        if (!isLocationEnabled) {
            viewModel.showError("GPS belum aktif")
            return
        }

        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(locationPermissions())
            return
        }

        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        toggleOnlineStatusWithService()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeHeader(
            isLoading = state.isLoading || state.isLoadingSupport,
            onRefresh = {
                viewModel.refreshDriverInfo()
                viewModel.refreshSupportInfo()
            }
        )

        state.errorMessage?.let { message ->
            ErrorBanner(message = message)
        }

        if (state.isLoading && state.driver == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val driver = state.driver
            if (driver != null) {
                ReadinessCard(
                    hasLocationPermission = hasLocationPermission,
                    isLocationEnabled = isLocationEnabled,
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestLocationPermission = {
                        locationPermissionLauncher.launch(locationPermissions())
                    },
                    onOpenLocationSettings = {
                        LocationHelper.openLocationSettings(context)
                    },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                if (state.supportInfo.motd.isNotBlank()) {
                    MotdCard(message = state.supportInfo.motd)
                }
                if (state.supportInfo.hasActiveOrder) {
                    ActiveOrderCard(orderId = state.supportInfo.activeOrderId ?: 0)
                }
                if (state.supportInfo.hasQueueInfo) {
                    QueueCard(supportInfo = state.supportInfo)
                }
                DriverSummaryCard(driver = driver)
                StatusCard(
                    driver = driver,
                    isTogglingStatus = state.isTogglingStatus,
                    onToggle = ::requestOnlineToggle
                )
                VehicleCard(driver = driver)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OkeOutlinedButton(
            text = if (state.isLoggingOut) "Keluar..." else "Keluar",
            onClick = { viewModel.logout(onLoggedOut) },
            enabled = !state.isLoggingOut
        )
    }
}

@Composable
private fun ReadinessCard(
    hasLocationPermission: Boolean,
    isLocationEnabled: Boolean,
    hasNotificationPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    if (hasLocationPermission && isLocationEnabled && hasNotificationPermission) return

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Kesiapan Online",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ReadinessRow(
                icon = if (hasLocationPermission) Icons.Rounded.LocationOn else Icons.Rounded.LocationOff,
                title = "Izin lokasi",
                value = if (hasLocationPermission) "Aktif" else "Belum aktif",
                actionText = if (hasLocationPermission) null else "Izinkan",
                onAction = onRequestLocationPermission
            )
            ReadinessRow(
                icon = if (isLocationEnabled) Icons.Rounded.LocationOn else Icons.Rounded.Settings,
                title = "GPS",
                value = if (isLocationEnabled) "Aktif" else "Belum aktif",
                actionText = if (isLocationEnabled) null else "Buka",
                onAction = onOpenLocationSettings
            )
            ReadinessRow(
                icon = Icons.Rounded.NotificationsActive,
                title = "Notifikasi",
                value = if (hasNotificationPermission) "Aktif" else "Belum aktif",
                actionText = if (hasNotificationPermission) null else "Izinkan",
                onAction = onRequestNotificationPermission
            )
        }
    }
}

@Composable
private fun ReadinessRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(8.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        actionText?.let {
            TextButton(onClick = onAction) {
                Text(text = it)
            }
        }
    }
}

private fun locationPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

@Composable
private fun ActiveOrderCard(orderId: Long) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Order aktif #$orderId",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Detail order akan dimigrasikan di Fase 3",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MotdCard(message: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Campaign,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Info Driver",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QueueCard(supportInfo: HomeSupportInfo) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (supportInfo.queueNumber) {
                        null -> "Antrean belum tersedia"
                        0 -> "Belum masuk antrean"
                        else -> "Nomor antrean ${supportInfo.queueNumber}"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = supportInfo.queueTerminal?.takeIf { it.isNotBlank() } ?: "Terminal belum dipilih",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Status driver dan saldo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onRefresh,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
    }
}

@Composable
private fun DriverSummaryCard(driver: HomeDriver) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = driver.name.ifBlank { "Driver" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = driver.phone.ifBlank { driver.email.ifBlank { "Profil belum lengkap" } },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BalanceItem(
                    title = "Saldo",
                    value = CurrencyFormatter.rupiah(driver.balance),
                    modifier = Modifier.weight(1f)
                )
                BalanceItem(
                    title = "Bisa ditarik",
                    value = CurrencyFormatter.rupiah(driver.withdrawableBalance),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusCard(
    driver: HomeDriver,
    isTogglingStatus: Boolean,
    onToggle: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Status Operasional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (driver.isOnline) "Online menerima order" else "Offline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusPill(isOnline = driver.isOnline)
            }
            OkeButton(
                text = if (driver.isOnline) "Go Offline" else "Go Online",
                onClick = onToggle,
                enabled = !isTogglingStatus,
                isLoading = isTogglingStatus
            )
        }
    }
}

@Composable
private fun StatusPill(isOnline: Boolean) {
    val container = if (isOnline) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val content = if (isOnline) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = container
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.PowerSettingsNew,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.labelMedium,
                color = content,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VehicleCard(driver: HomeDriver) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = driver.vehiclePlate.ifBlank { "Kendaraan" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tipe kendaraan ${driver.vehicleType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}
