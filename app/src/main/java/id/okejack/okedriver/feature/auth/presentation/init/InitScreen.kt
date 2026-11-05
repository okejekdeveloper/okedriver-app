package id.okejack.okedriver.feature.auth.presentation.init

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InitScreen(
    onLoggedIn: () -> Unit,
    onNotLoggedIn: () -> Unit,
) {
    val viewModel: InitViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    (state as? InitUiState.UpdateAvailable)?.let { update ->
        AlertDialog(
            onDismissRequest = { if (!update.mandatory) viewModel.skipOptionalUpdate() },
            properties = DialogProperties(dismissOnBackPress = !update.mandatory, dismissOnClickOutside = !update.mandatory),
            title = { Text(if (update.mandatory) "Update Diperlukan" else "Update Tersedia") },
            text = { Text(if (update.mandatory) "Versi aplikasi kamu sudah tidak didukung. Harap update untuk melanjutkan."
                else "Versi baru aplikasi tersedia. Update sekarang untuk mendapatkan fitur dan perbaikan terbaru.") },
            confirmButton = {
                TextButton(onClick = {
                    val url = "https://play.google.com/store/apps/details?id=${context.packageName}"
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                        .onFailure { Toast.makeText(context, "Tidak dapat membuka halaman update", Toast.LENGTH_SHORT).show() }
                }) { Text("Update Sekarang") }
            },
            dismissButton = { if (!update.mandatory) TextButton(onClick = viewModel::skipOptionalUpdate) { Text("Nanti") } }
        )
    }

    LaunchedEffect(state) {
        when (state) {
            is InitUiState.LoggedIn -> onLoggedIn()
            is InitUiState.NotLoggedIn -> onNotLoggedIn()
            else -> Unit
        }
    }

    // Splash / loading sementara session dicek
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
    }
}
