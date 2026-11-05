package id.okejack.okedriver.feature.profile.presentation.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.okejack.okedriver.core.designsystem.component.OkeButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading && state.profile == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    state.profile?.let {
                        Text(
                            text = it.name.ifBlank { "Driver" },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = it.phone.ifBlank { "Nomor belum tersedia" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    MessageBanner(message = state.errorMessage, isError = true)
                    MessageBanner(message = state.successMessage, isError = false)

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = viewModel::onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Email") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password baru") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = state.bankName,
                        onValueChange = viewModel::onBankNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nama bank") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.bankAccount,
                        onValueChange = viewModel::onBankAccountChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nomor rekening") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.bankAccountHolder,
                        onValueChange = viewModel::onBankAccountHolderChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nama pemilik rekening") },
                        singleLine = true
                    )

                    OkeButton(
                        text = "Simpan",
                        onClick = { viewModel.submit() },
                        isLoading = state.isSubmitting
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBanner(message: String?, isError: Boolean) {
    if (message.isNullOrBlank()) return
    val container = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val content = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = container
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(14.dp),
            color = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
