package id.okejack.okedriver.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.usecase.LogoutUseCase
import id.okejack.okedriver.feature.profile.domain.model.ProfileDriver
import id.okejack.okedriver.feature.profile.domain.usecase.GetProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getProfileUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(driver = result.data, isLoading = false, errorMessage = null)
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            logoutUseCase()
            onDone()
        }
    }
}

data class ProfileUiState(
    val driver: ProfileDriver? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggingOut: Boolean = false
)
