package id.okejack.okedriver.feature.home.presentation.simple

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
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
import id.okejack.okedriver.core.designsystem.theme.balanceAccent
import id.okejack.okedriver.core.designsystem.theme.menuTitleAccent
import id.okejack.okedriver.feature.home.domain.model.QueueLocations
import id.okejack.okedriver.feature.home.presentation.HomeActions
import id.okejack.okedriver.feature.home.presentation.HomeDashboardLayout
import id.okejack.okedriver.feature.home.presentation.HomePresentation

/** Status, balance and work tools in an ordinary scrolling dashboard. */
@Composable
fun SimpleHomeContent(state: HomePresentation, actions: HomeActions, mapContent: @Composable (PaddingValues) -> Unit) {
    with(state) { with(actions) {
        HomeDashboardLayout(
            state = state,
            actions = actions,
            map = mapContent,
            status = {
                if (supportInfo.hasActiveOrder) OkeButton("Lanjutkan order #${supportInfo.activeOrderId}", onOpenCurrentOrder)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.PowerSettingsNew, null,
                        tint = if (driver?.isOnline == true) OkeSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp))
                    Column(Modifier.weight(1f)) {
                        Text(when { driver == null -> "Memuat akun Anda"; driver.isOnline -> "Anda sedang aktif"; else -> "Anda belum aktif" },
                            style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (driver?.isOnline == true) {
                            if (hasLocationPermission && isLocationEnabled && hasNotificationPermission) "Siap menerima order baru"
                            else "Periksa izin dan GPS di bawah ini"
                        } else "Aktifkan saat Anda siap bekerja",
                            color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (errorMessage != null) DriverNotice(errorMessage, "Coba lagi", onRefresh, isError = true)
                // Resolve one prerequisite at a time, rather than showing several competing warnings.
                when {
                    !hasLocationPermission -> DriverNotice("Izinkan lokasi agar posisi penjemputan Anda dapat ditemukan.", "Izinkan lokasi", onRequestLocationPermission)
                    !isLocationEnabled -> DriverNotice("GPS belum aktif. Nyalakan untuk menerima order di sekitar Anda.", "Nyalakan GPS", onOpenLocationSettings)
                    !hasNotificationPermission -> DriverNotice("Aktifkan notifikasi agar Anda mendengar order masuk.", "Aktifkan notifikasi", onRequestNotificationPermission)
                }
                if (driver?.isOnline == true) OkeOutlinedButton(
                    if (isTogglingStatus) "Mengubah status..." else "Berhenti menerima order",
                    onToggle, enabled = !isTogglingStatus && !isLoading)
                else OkeButton("Mulai terima order", onToggle, enabled = driver != null && !isLoading, isLoading = isTogglingStatus)
            },
            details = {
                Surface(onClick = onOpenBalance, shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f)) {
                            Text("Saldo Anda", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(driver?.let { CurrencyFormatter.rupiah(it.balance) } ?: "Belum tersedia",
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.balanceAccent)
                        }
                        Icon(Icons.Rounded.ChevronRight, "Lihat saldo")
                    }
                }
                if (supportInfo.attendanceEnabled || driver?.isAttendanceEnabled == true) DriverCard {
                    DriverMenuRow(Icons.Rounded.FactCheck, if (supportInfo.hasAttended) "Absensi sudah tercatat" else "Absen hari ini",
                        if (supportInfo.hasAttended) "Lihat catatan absensi Anda" else "Ketuk untuk mengisi absensi", onOpenAttendance)
                }
                if (supportInfo.hasQueueInfo) DriverCard {
                    Text(if ((supportInfo.queueNumber ?: 0) > 0) "Nomor antrean ${supportInfo.queueNumber}" else "Antrean driver",
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(supportInfo.queueTerminal?.takeIf(String::isNotBlank)
                        ?: if (supportInfo.hasAttended) "Pilih lokasi antrean Anda" else "Isi absensi sebelum mendaftar antrean",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    supportInfo.queueTerminalTotal?.takeIf { it > 0 }?.let { Text("Dari $it driver dalam antrean") }
                    if (supportInfo.queueMessage.isNotBlank()) Text(supportInfo.queueMessage)
                    if (supportInfo.hasAttended && (supportInfo.queueNumber ?: 0) <= 0) {
                        QueueLocations.all.forEach { terminal ->
                            OkeOutlinedButton(if (isRegisteringQueue) "Mendaftar..." else terminal,
                                { onRegisterQueue(terminal) }, enabled = !isRegisteringQueue)
                        }
                    }
                }
                DriverCard {
                    val menuAccent = MaterialTheme.colorScheme.menuTitleAccent
                    Text("Menu kerja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    DriverMenuRow(Icons.Rounded.Explore, "Peta order sekitar", "Lihat order terbaru di sekitar Anda", onOpenRecentOrders, menuAccent)
                    if (driver?.isOkefastEnabled == true) DriverMenuRow(Icons.Rounded.LocalShipping, "OkeFast", "Buat kode pengiriman", onOpenOkeFast, menuAccent)
                    DriverMenuRow(Icons.Rounded.Newspaper, "Info dari kantor", "Berita dan pengumuman driver", onOpenNews, menuAccent)
                    DriverMenuRow(Icons.Rounded.Tune, "Pengaturan", "Atur order dan izin aplikasi", onOpenSettings, menuAccent)
                }
                if (supportInfo.motd.isNotBlank()) DriverNotice(supportInfo.motd)
                if (updateLabel != null) DriverNotice("Versi baru tersedia: $updateLabel", "Perbarui aplikasi", onUpdate)
            }
        )
    } }
}
