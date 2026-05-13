package id.okejack.okedriver.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.model.AuthDriver
import id.okejack.okedriver.feature.auth.domain.usecase.LoginUseCase
import id.okejack.okedriver.feature.auth.domain.usecase.LoginWithGoogleUseCase
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
    val loggedInDriver: AuthDriver? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginWithGoogleUseCase: LoginWithGoogleUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }
    fun togglePasswordVisibility() = _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = loginUseCase(_state.value.email, _state.value.password)) {
                is ApiResponse.Success -> _state.update { it.copy(isLoading = false, loginSuccess = true, loggedInDriver = result.data) }
                is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun loginWithGoogle(email: String, idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = loginWithGoogleUseCase(email, idToken)) {
                is ApiResponse.Success -> _state.update { it.copy(isLoading = false, loginSuccess = true, loggedInDriver = result.data) }
                is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                else -> Unit
            }
        }
    }

    fun onGoogleLoginFailed(message: String?) {
        _state.update {
            it.copy(
                isLoading = false,
                errorMessage = message?.takeIf(String::isNotBlank) ?: "Login Google dibatalkan"
            )
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
