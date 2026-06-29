package id.okejack.okedriver.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.usecase.LogoutUseCase
import id.okejack.okedriver.feature.home.domain.model.HomeDriver
import id.okejack.okedriver.feature.home.domain.usecase.GetDriverInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getDriverInfoUseCase: GetDriverInfoUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getDriverInfoUseCase()) {
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
        viewModelScope.launch {
            logoutUseCase()
            onDone()
        }
    }
}

data class ProfileUiState(
    val driver: HomeDriver? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
