package id.okejack.okedriver.feature.home.presentation.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.okejack.okedriver.core.common.CurrencyFormatter
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.core.designsystem.component.dynamic.*
import id.okejack.okedriver.core.designsystem.theme.balanceAccent
import id.okejack.okedriver.core.designsystem.theme.menuTitleAccent
import id.okejack.okedriver.feature.home.domain.model.QueueLocations
import id.okejack.okedriver.feature.home.presentation.HomeActions
import id.okejack.okedriver.feature.home.presentation.HomeDashboardLayout
import id.okejack.okedriver.feature.home.presentation.HomePresentation

/** The dynamic palette on a work dashboard, without a draggable sheet or decorative hero. */
@Composable
fun DynamicHomeContent(state: HomePresentation, actions: HomeActions, mapContent: @Composable (PaddingValues) -> Unit) {
    with(state) { with(actions) {
        HomeDashboardLayout(
            state = state,
            actions = actions,
            map = mapContent,
            status = {
                if (supportInfo.hasActiveOrder) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Route, null, tint = MaterialTheme.colorScheme.primary)
                        Text("Order Anda sedang berjalan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    OkeButton("Lanjutkan order", onOpenCurrentOrder)
                }
                errorMessage?.let { DriverNotice(it, "Coba lagi", onRefresh, true) }
                when {
                    !hasLocationPermission -> DriverNotice("Izinkan lokasi untuk menemukan order di sekitar Anda.", "Izinkan lokasi", onRequestLocationPermission)
                    !isLocationEnabled -> DriverNotice("GPS belum aktif. Nyalakan sebelum mulai menerima order.", "Nyalakan GPS", onOpenLocationSettings)
                    !hasNotificationPermission -> DriverNotice("Aktifkan notifikasi agar order masuk tidak terlewat.", "Aktifkan notifikasi", onRequestNotificationPermission)
                }
                Text(when {
                    driver == null -> "Memuat akun Anda"
                    driver.isOnline -> "Anda aktif menerima order"
                    else -> "Anda belum aktif"
                }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(if (driver?.isOnline == true) {
                    if (hasLocationPermission && isLocationEnabled && hasNotificationPermission) "Siap menerima order baru"
                    else "Lengkapi izin dan GPS"
                } else "Aktifkan saat Anda siap bekerja",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (driver?.isOnline == true) OkeOutlinedButton(
                    if (isTogglingStatus) "Mengubah status..." else "Berhenti menerima order",
                    onToggle, enabled = !isTogglingStatus && !isLoading)
                else OkeButton("Mulai terima order", onToggle,
                    enabled = driver != null && !isLoading, isLoading = isTogglingStatus)
            },
            details = {
                Surface(onClick = onOpenBalance, shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Saldo Anda", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(driver?.balance?.let(CurrencyFormatter::rupiah) ?: "Belum tersedia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.balanceAccent)
                        }
                        Icon(Icons.Rounded.ChevronRight, "Lihat saldo", tint = MaterialTheme.colorScheme.primary)
                    }
                }
                DynamicWorkMenu(state, actions)
                if (supportInfo.hasQueueInfo) DynamicDriverCard {
                    Text(if ((supportInfo.queueNumber ?: 0) > 0) "Antrean Anda: ${supportInfo.queueNumber}" else "Antrean driver",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(supportInfo.queueTerminal?.takeIf(String::isNotBlank)
                        ?: if (supportInfo.hasAttended) "Pilih lokasi antrean Anda" else "Isi absensi sebelum mendaftar antrean")
                    supportInfo.queueTerminalTotal?.takeIf { it > 0 }?.let { Text("Dari $it driver dalam antrean") }
                    if (supportInfo.queueMessage.isNotBlank()) Text(supportInfo.queueMessage)
                    if (supportInfo.hasAttended && (supportInfo.queueNumber ?: 0) <= 0) QueueLocations.all.forEach { terminal ->
                        OkeOutlinedButton(if (isRegisteringQueue) "Mendaftar..." else terminal, { onRegisterQueue(terminal) }, enabled = !isRegisteringQueue)
                    }
                }
                if (supportInfo.motd.isNotBlank()) DriverNotice(supportInfo.motd)
                if (updateLabel != null) DriverNotice("Versi baru tersedia: $updateLabel", "Perbarui aplikasi", onUpdate)
            }
        )
    } }
}

@Composable
private fun DynamicWorkMenu(state: HomePresentation, actions: HomeActions) {
    data class Item(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: String, val action: () -> Unit)
    val items = buildList {
        add(Item(Icons.Rounded.AccountBalanceWallet, "Saldo", actions.onOpenBalance))
        add(Item(Icons.Rounded.Explore, "Peta order", actions.onOpenRecentOrders))
        if (state.supportInfo.attendanceEnabled || state.driver?.isAttendanceEnabled == true) {
            add(Item(Icons.Rounded.FactCheck, if (state.supportInfo.hasAttended) "Sudah absen" else "Absensi", actions.onOpenAttendance))
        }
        if (state.driver?.isOkefastEnabled == true) add(Item(Icons.Rounded.LocalShipping, "OkeFast", actions.onOpenOkeFast))
        add(Item(Icons.Rounded.Newspaper, "Info kantor", actions.onOpenNews))
        add(Item(Icons.Rounded.Tune, "Pengaturan", actions.onOpenSettings))
    }
    Text("Menu kerja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface)
    val menuAccent = MaterialTheme.colorScheme.menuTitleAccent
    Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.surface) {
        Column {
            items.forEach { item -> DriverMenuRow(item.icon, item.title, onClick = item.action, titleColor = menuAccent) }
        }
    }
}
