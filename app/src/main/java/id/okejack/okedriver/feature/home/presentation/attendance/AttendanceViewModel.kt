package id.okejack.okedriver.feature.home.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.home.domain.usecase.CheckAttendanceUseCase
import id.okejack.okedriver.feature.home.domain.usecase.SubmitAttendanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val checkAttendanceUseCase: CheckAttendanceUseCase,
    private val submitAttendanceUseCase: SubmitAttendanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceUiState())
    val state: StateFlow<AttendanceUiState> = _state

    fun checkStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = checkAttendanceUseCase()) {
                is ApiResponse.Success -> _state.update {
                    it.copy(isLoading = false, hasAttended = result.data)
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun submitAttendance(onSuccess: () -> Unit) {
        if (_state.value.hasAttended) return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, errorMessage = null) }
            when (val result = submitAttendanceUseCase()) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(isSubmitting = false, hasAttended = true) }
                    onSuccess()
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}

data class AttendanceUiState(
    val hasAttended: Boolean = false,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
