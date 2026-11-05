package id.okejack.okedriver.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.feature.auth.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordVisible: Boolean = false,
    val loginSuccess: Boolean = false,
    val loggedInDriver: AuthDriver? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, emailError = null, passwordError = null, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, passwordError = null, errorMessage = null) }
    fun togglePasswordVisibility() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun login() {
        if (_state.value.isLoading) return
        val input = _state.value
        val emailError = if (input.email.isBlank()) "Masukkan email Anda" else null
        val passwordError = if (input.password.isBlank()) "Masukkan kata sandi Anda" else null
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError, errorMessage = null) }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, emailError = null, passwordError = null) }
        viewModelScope.launch {
            when (val result = loginUseCase(input.email, input.password)) {
                is ApiResponse.Success -> _state.update { it.copy(isLoading = false, loginSuccess = true, loggedInDriver = result.data) }
                is ApiResponse.Error -> _state.update {
                    val credentialsRejected = result.errorCode == "bad_auth" ||
                        (result.code == 401 && result.errorCode.isNullOrBlank())
                    it.copy(isLoading = false,
                        passwordError = if (credentialsRejected) "Email atau kata sandi tidak sesuai." else null,
                        errorMessage = if (credentialsRejected) null else result.message)
                }
                ApiResponse.Loading -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showError(message: String) = _state.update { it.copy(errorMessage = message) }
}
