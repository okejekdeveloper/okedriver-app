package id.okejack.okedriver.feature.settings.presentation

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Palette
import id.okejack.okedriver.core.designsystem.theme.LocalDriverAppearance
import id.okejack.okedriver.feature.settings.presentation.appearance.AppearancePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Switch
import id.okejack.okedriver.core.designsystem.component.LocalLocationConsent
import id.okejack.okedriver.core.service.LocationAccess
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import id.okejack.okedriver.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onDriverBidClick: () -> Unit,
    onConnectAccountClick: () -> Unit
) {
    val context = LocalContext.current
    val locationConsent = LocalLocationConsent.current
    val backgroundPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var showBackgroundDisclosure by remember { mutableStateOf(false) }
    var showAppearance by remember { mutableStateOf(false) }
    val appearance = LocalDriverAppearance.current
    if (showAppearance) AppearancePicker { showAppearance = false }
    // Prominent disclosure must be shown, and accepted, before the background permission is requested.
    if (showBackgroundDisclosure) {
        AlertDialog(
            onDismissRequest = { showBackgroundDisclosure = false },
            title = { Text(stringResource(R.string.background_location_disclosure_title)) },
            text = {
                Text(
                    stringResource(R.string.background_location_disclosure),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackgroundDisclosure = false
                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) backgroundPermission.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    else context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
                }) { Text(stringResource(R.string.action_agree)) }
            },
            dismissButton = {
                TextButton(onClick = { showBackgroundDisclosure = false }) { Text(stringResource(R.string.action_deny)) }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            SettingsRow(Icons.Rounded.Palette, "Tampilan aplikasi · ${appearance.style.label}") { showAppearance = true }
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            SettingsRow(Icons.Rounded.Tune, "Preferensi order", onDriverBidClick)
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            SettingsRow(Icons.Rounded.Link, "Akun terhubung", onConnectAccountClick)
            HorizontalDivider(modifier = Modifier.padding(start = 64.dp))
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Penggunaan lokasi", style = MaterialTheme.typography.bodyLarge)
                    Text("Matikan untuk mencabut persetujuan dan menghentikan pelacakan.", style = MaterialTheme.typography.bodySmall)
                }
                Switch(checked = locationConsent.granted, onCheckedChange = { enabled ->
                    if (enabled) locationConsent.request {} else LocationAccess.revoke(context)
                })
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                SettingsRow(Icons.Rounded.Tune, "Izin lokasi untuk pemulihan setelah restart") {
                    locationConsent.request { showBackgroundDisclosure = true }
                }
                Text("Pilih izin lokasi selalu jika Anda ingin layanan online dipulihkan setelah perangkat restart.",
                    modifier = Modifier.padding(horizontal = 20.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null)
    }
}
