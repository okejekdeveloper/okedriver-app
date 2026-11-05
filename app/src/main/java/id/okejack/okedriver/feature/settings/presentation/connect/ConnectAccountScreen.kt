package id.okejack.okedriver.feature.settings.presentation.connect

import id.okejack.okedriver.core.designsystem.component.DriverOutlinedButton as OutlinedButton
import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.designsystem.component.OkeButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectAccountScreen(
    onBackClick: () -> Unit,
    viewModel: ConnectAccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var showDisconnectConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(state.errorMessage, state.successMessage) {
        (state.errorMessage ?: state.successMessage)?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    if (showDisconnectConfirmation) {
        AlertDialog(
            onDismissRequest = { showDisconnectConfirmation = false },
            title = { Text("Putuskan akun?") },
            confirmButton = {
                TextButton(onClick = {
                    showDisconnectConfirmation = false
                    viewModel.disconnect()
                }) { Text("Putuskan") }
            },
            dismissButton = {
                TextButton(onClick = { showDisconnectConfirmation = false }) { Text("Batal") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Akun Terhubung") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.connectedUser != null) {
                Icon(Icons.Rounded.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(state.connectedUser!!.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                if (state.connectedUser!!.email.isNotBlank()) Text(state.connectedUser!!.email)
                OutlinedButton(
                    onClick = { showDisconnectConfirmation = true },
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Putuskan akun") }
            } else {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OkeButton(
                    text = "Hubungkan akun",
                    onClick = viewModel::connect,
                    isLoading = state.isSubmitting
                )
            }
        }
    }
}
