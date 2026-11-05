package id.okejack.okedriver.feature.profile.presentation

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import id.okejack.okedriver.BuildConfig
import id.okejack.okedriver.core.designsystem.component.*
import id.okejack.okedriver.core.designsystem.theme.LocalDriverUiStyle
import id.okejack.okedriver.core.designsystem.theme.LocalDriverAppearance
import id.okejack.okedriver.core.designsystem.theme.DriverUiStyle
import id.okejack.okedriver.feature.profile.presentation.dynamic.DynamicProfileHeader
import id.okejack.okedriver.feature.settings.presentation.appearance.AppearancePicker

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {}, onEditProfileClick: () -> Unit = {}, onReferralClick: () -> Unit = {},
    onConnectAccountClick: () -> Unit = {}, onLogoutClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(), isRoot: Boolean = false,
    onSettingsClick: () -> Unit = {}, onNewsClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    OnScreenResume { viewModel.loadProfile() }
    ProfileContent(state, if (isRoot) null else onBackClick, viewModel::loadProfile, onEditProfileClick,
        onReferralClick, onConnectAccountClick, onSettingsClick, onNewsClick, { viewModel.logout(onLogoutClick) })
}

@Composable
fun ProfileContent(
    state: ProfileUiState, onBack: (() -> Unit)?, onRefresh: () -> Unit, onEdit: () -> Unit,
    onReferral: () -> Unit, onConnect: () -> Unit, onSettings: () -> Unit, onNews: () -> Unit, onLogout: () -> Unit
) {
    var showGuide by remember { mutableStateOf(false) }
    var confirmLogout by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    val appearance = LocalDriverAppearance.current
    if (showAppearance) AppearancePicker { showAppearance = false }
    if (showGuide) AlertDialog(onDismissRequest = { showGuide = false }, title = { Text("Cara menggunakan Oke Driver") },
        text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("1. Mulai bekerja\nBuka Beranda, lalu ketuk Mulai terima order. Izinkan lokasi dan notifikasi jika diminta.")
            Text("2. Terima dan jalankan order\nBaca alamat jemput, tujuan, dan pembayaran. Ketuk Terima order. Gunakan Buka petunjuk jalan untuk menuju pelanggan.")
            Text("3. Hubungi pelanggan\nDi halaman order, gunakan Pesan atau Telepon. Anda bisa memilih pesan cepat sebelum mengirimnya.")
            Text("4. Mulai dan selesaikan perjalanan\nGeser tombol di bawah halaman order setelah siap. Periksa pembayaran pelanggan sebelum menyelesaikan order.")
            Text("5. Cek uang Anda\nBuka Saldo untuk isi saldo, menarik uang ke bank, atau memeriksa transaksi. Buka Riwayat untuk melihat order sebelumnya.")
            Text("Order belum masuk?\nPeriksa status aktif, GPS, izin lokasi, notifikasi, dan koneksi internet. Pengaturan izin tersedia di menu Pengaturan.")
        } }, confirmButton = { TextButton(onClick = { showGuide = false }) { Text("Mengerti") } })
    if (confirmLogout) AlertDialog(onDismissRequest = { confirmLogout = false }, title = { Text("Keluar dari akun?") },
        text = { Text("Anda perlu masuk lagi untuk menerima order. Pastikan pekerjaan Anda sudah selesai.") },
        confirmButton = { TextButton(onClick = { confirmLogout = false; onLogout() }) { Text("Ya, keluar") } },
        dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Tetap masuk") } })
    Scaffold(topBar = { OkeTopBar("Akun", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            DriverContent {
                state.errorMessage?.let { DriverNotice(it, "Coba lagi", onRefresh, true) }
                if (LocalDriverUiStyle.current == DriverUiStyle.DYNAMIC) DynamicProfileHeader(state, onEdit)
                else DriverCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        val photo = state.driver?.avatarUrl
                        if (!photo.isNullOrBlank()) AsyncImage(photo, "Foto profil", Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        else Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                            Icon(Icons.Rounded.Person, null, Modifier.padding(16.dp).size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(state.driver?.name?.ifBlank { "Mitra driver" } ?: if (state.isLoading) "Memuat akun..." else "Akun belum dimuat",
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            state.driver?.let { Text(it.phone, style = MaterialTheme.typography.bodyLarge) }
                        }
                    }
                    state.driver?.let { driver ->
                        HorizontalDivider()
                        Text(driver.vehiclePlate.ifBlank { "Nomor kendaraan belum diisi" }, fontWeight = FontWeight.Bold)
                        Text(listOf(driver.vehicleBrand, driver.vehicleColor).filter(String::isNotBlank).joinToString(" · "),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OkeOutlinedButton("Ubah profil dan rekening", onEdit)
                }
                Surface(shape = MaterialTheme.shapes.medium) { Column {
                    DriverMenuRow(Icons.Rounded.Palette, "Tampilan aplikasi", appearance.style.label, { showAppearance = true })
                    HorizontalDivider()
                    DriverMenuRow(Icons.Rounded.MenuBook, "Cara menggunakan aplikasi", "Panduan singkat untuk mulai bekerja", { showGuide = true })
                    HorizontalDivider()
                    DriverMenuRow(Icons.Rounded.Tune, "Pengaturan", "Preferensi order dan izin aplikasi", onSettings)
                    HorizontalDivider()
                    DriverMenuRow(Icons.Rounded.Newspaper, "Info dari kantor", "Berita dan pengumuman", onNews)
                    HorizontalDivider()
                    DriverMenuRow(Icons.Rounded.GroupAdd, "Ajak driver lain", "Lihat kode dan daftar referral", onReferral)
                    HorizontalDivider()
                    DriverMenuRow(Icons.Rounded.Link, "Hubungkan akun pelanggan", "Gunakan akun pelanggan Anda", onConnect)
                } }
                OkeOutlinedButton(if (state.isLoggingOut) "Sedang keluar..." else "Keluar dari akun",
                    { confirmLogout = true }, enabled = !state.isLoggingOut)
                Text("Oke Driver ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
