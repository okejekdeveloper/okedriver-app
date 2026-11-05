package id.okejack.okedriver.feature.settings.presentation.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.settings.domain.model.ConnectedUser
import id.okejack.okedriver.feature.settings.domain.usecase.ConnectAccountUseCase
import id.okejack.okedriver.feature.settings.domain.usecase.DisconnectAccountUseCase
import id.okejack.okedriver.feature.settings.domain.usecase.GetDriverSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectAccountViewModel @Inject constructor(
    private val getSettings: GetDriverSettingsUseCase,
    private val connectAccount: ConnectAccountUseCase,
    private val disconnectAccount: DisconnectAccountUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(ConnectAccountUiState())
    val state: StateFlow<ConnectAccountUiState> = _state

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = getSettings()) {
            is ApiResponse.Success -> _state.update {
                it.copy(isLoading = false, connectedUser = result.data.connectedUser)
            }
            is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, errorMessage = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, errorMessage = null) }

    fun connect() = viewModelScope.launch {
        _state.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
        when (val result = connectAccount(_state.value.email, _state.value.password)) {
            is ApiResponse.Success -> _state.update {
                it.copy(
                    isSubmitting = false,
                    email = "",
                    password = "",
                    connectedUser = result.data.connectedUser,
                    successMessage = "Akun berhasil dihubungkan"
                )
            }
            is ApiResponse.Error -> _state.update { it.copy(isSubmitting = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun disconnect() = viewModelScope.launch {
        _state.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
        when (val result = disconnectAccount()) {
            is ApiResponse.Success -> _state.update {
                it.copy(isSubmitting = false, connectedUser = result.data.connectedUser, successMessage = "Akun diputuskan")
            }
            is ApiResponse.Error -> _state.update { it.copy(isSubmitting = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun clearMessage() = _state.update { it.copy(errorMessage = null, successMessage = null) }
}

data class ConnectAccountUiState(
    val email: String = "",
    val password: String = "",
    val connectedUser: ConnectedUser? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
