package id.okejack.okedriver.feature.auth.presentation.login

import id.okejack.okedriver.core.designsystem.component.DriverTextButton as TextButton

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import id.okejack.okedriver.core.designsystem.component.OkeButton
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.core.designsystem.component.OkeTextField
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import id.okejack.okedriver.core.designsystem.component.DriverNotice
import id.okejack.okedriver.R
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import id.okejack.okedriver.feature.settings.presentation.appearance.AppearancePicker

@Composable
fun LoginScreen(
    onLoginSuccess: (AuthDriver?) -> Unit,
) {
    val viewModel: LoginViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showAppearance by remember { mutableStateOf(false) }
    if (showAppearance) AppearancePicker { showAppearance = false }

    LaunchedEffect(state.loginSuccess) {
        if (state.loginSuccess) onLoginSuccess(state.loggedInDriver)
    }

    LoginContent(state, viewModel::onEmailChange, viewModel::onPasswordChange,
        viewModel::togglePasswordVisibility, viewModel::login,
        onRegister = {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://okejack.id/driver/apply/driver"))) }
                .onFailure { viewModel.showError("Tidak dapat membuka pendaftaran driver") }
        },
        onChooseAppearance = { showAppearance = true })
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    onChooseAppearance: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    Scaffold { paddingValues ->
        Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Image(painterResource(R.drawable.oke_driver_mark), contentDescription = "Logo Oke Driver", modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Oke Driver",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Masuk untuk mulai menerima order",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email / no telp
            OkeTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = "Email",
                placeholder = "contoh@email.com",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                enabled = !state.isLoading,
                isError = state.emailError != null,
                errorMessage = state.emailError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OkeTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Kata sandi",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); onLogin() }),
                enabled = !state.isLoading,
                isError = state.passwordError != null,
                errorMessage = state.passwordError,
                visualTransformation = if (state.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility, enabled = !state.isLoading) {
                        Icon(
                            imageVector = if (state.isPasswordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (state.isPasswordVisible)
                                "Sembunyikan password"
                            else
                                "Tampilkan password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            state.errorMessage?.let {
                DriverNotice(it, isError = true)
                Spacer(Modifier.height(12.dp))
            }

            OkeButton(
                text = "Masuk",
                onClick = {
                    focusManager.clearFocus()
                    onLogin()
                },
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onRegister, enabled = !state.isLoading) { Text("Daftar menjadi driver") }
            TextButton(onClick = onChooseAppearance, enabled = !state.isLoading) { Text("Pilih tampilan aplikasi") }

            Spacer(modifier = Modifier.height(24.dp))
        }
        }
    }
}
