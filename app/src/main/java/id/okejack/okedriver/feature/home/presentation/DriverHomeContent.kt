package id.okejack.okedriver.feature.home.presentation

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.core.designsystem.theme.OkeSuccess
import id.okejack.okedriver.core.designsystem.theme.LocalDriverUiStyle
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.feature.home.presentation.simple.SimpleHomeContent
import id.okejack.okedriver.feature.home.presentation.dynamic.DynamicHomeContent
import id.okejack.okedriver.feature.home.domain.model.HomeDriver
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo
import id.okejack.okedriver.feature.home.domain.model.QueueLocations

@Composable
fun DriverHomeContent(
    driver: HomeDriver?, supportInfo: HomeSupportInfo,
    isLoading: Boolean, isTogglingStatus: Boolean, isRegisteringQueue: Boolean,
    errorMessage: String?, hasLocationPermission: Boolean, isLocationEnabled: Boolean,
    hasNotificationPermission: Boolean,
    onToggle: () -> Unit, onRefresh: () -> Unit, onOpenBalance: () -> Unit,
    onOpenCurrentOrder: () -> Unit, onOpenAttendance: () -> Unit,
    onRegisterQueue: (String) -> Unit, onOpenRecentOrders: () -> Unit, onOpenOkeFast: () -> Unit,
    onOpenNews: () -> Unit, onOpenSettings: () -> Unit,
    onRequestLocationPermission: () -> Unit, onOpenLocationSettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    updateLabel: String? = null, onUpdate: () -> Unit = {},
    mapContent: @Composable (PaddingValues) -> Unit
) {
    var confirmOffline by remember { mutableStateOf(false) }
    var selectedQueue by remember { mutableStateOf<String?>(null) }
    if (confirmOffline) AlertDialog(
        onDismissRequest = { confirmOffline = false },
        title = { Text("Berhenti menerima order?") },
        text = { Text("Anda tidak akan menerima order baru sampai diaktifkan kembali.") },
        confirmButton = { TextButton(onClick = { confirmOffline = false; onToggle() }) { Text("Ya, berhenti") } },
        dismissButton = { TextButton(onClick = { confirmOffline = false }) { Text("Tetap aktif") } }
    )
    selectedQueue?.let { terminal -> AlertDialog(
        onDismissRequest = { selectedQueue = null }, title = { Text("Masuk antrean?") },
        text = { Text("Daftar antrean di $terminal menggunakan lokasi Anda sekarang.") },
        confirmButton = { TextButton(onClick = { selectedQueue = null; onRegisterQueue(terminal) }) { Text("Daftar antrean") } },
        dismissButton = { TextButton(onClick = { selectedQueue = null }) { Text("Kembali") } }
    ) }

    val presentation = HomePresentation(driver, supportInfo, isLoading, isTogglingStatus, isRegisteringQueue,
        errorMessage, hasLocationPermission, isLocationEnabled, hasNotificationPermission, updateLabel)
    val actions = HomeActions(
        onToggle = { if (driver?.isOnline == true) confirmOffline = true else onToggle() },
        onRefresh = onRefresh, onOpenBalance = onOpenBalance, onOpenCurrentOrder = onOpenCurrentOrder,
        onOpenAttendance = onOpenAttendance, onRegisterQueue = { selectedQueue = it },
        onOpenRecentOrders = onOpenRecentOrders, onOpenOkeFast = onOpenOkeFast, onOpenNews = onOpenNews,
        onOpenSettings = onOpenSettings, onRequestLocationPermission = onRequestLocationPermission,
        onOpenLocationSettings = onOpenLocationSettings, onRequestNotificationPermission = onRequestNotificationPermission,
        onUpdate = onUpdate
    )
    when (LocalDriverUiStyle.current) {
        DriverUiStyle.SIMPLE -> SimpleHomeContent(presentation, actions, mapContent)
        DriverUiStyle.DYNAMIC -> DynamicHomeContent(presentation, actions, mapContent)
    }
}

data class HomePresentation(
    val driver: HomeDriver?, val supportInfo: HomeSupportInfo, val isLoading: Boolean,
    val isTogglingStatus: Boolean, val isRegisteringQueue: Boolean, val errorMessage: String?,
    val hasLocationPermission: Boolean, val isLocationEnabled: Boolean, val hasNotificationPermission: Boolean,
    val updateLabel: String?
)

data class HomeActions(
    val onToggle: () -> Unit, val onRefresh: () -> Unit, val onOpenBalance: () -> Unit,
    val onOpenCurrentOrder: () -> Unit, val onOpenAttendance: () -> Unit,
    val onRegisterQueue: (String) -> Unit, val onOpenRecentOrders: () -> Unit, val onOpenOkeFast: () -> Unit,
    val onOpenNews: () -> Unit, val onOpenSettings: () -> Unit, val onRequestLocationPermission: () -> Unit,
    val onOpenLocationSettings: () -> Unit, val onRequestNotificationPermission: () -> Unit, val onUpdate: () -> Unit
)
