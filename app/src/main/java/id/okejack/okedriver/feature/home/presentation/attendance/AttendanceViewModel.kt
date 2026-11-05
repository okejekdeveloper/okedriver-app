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
import id.okejack.okedriver.core.service.FreshLocationProvider
import id.okejack.okedriver.core.network.api.DriverApiService
import id.okejack.okedriver.core.datastore.SessionDataStore
import id.okejack.okedriver.core.network.model.safeApiCall
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.first

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val checkAttendanceUseCase: CheckAttendanceUseCase,
    private val submitAttendanceUseCase: SubmitAttendanceUseCase,
    private val freshLocationProvider: FreshLocationProvider,
    private val driverApi: DriverApiService,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceUiState())
    val state: StateFlow<AttendanceUiState> = _state

    fun checkStatus() {
        if (_state.value.isLoading || _state.value.isSubmitting) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = checkAttendanceUseCase()) {
                is ApiResponse.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        hasAttended = result.data.hasAttended,
                        queueNumber = result.data.queueNumber,
                        queueTerminal = result.data.queueTerminal,
                        queueTerminalTotal = result.data.queueTerminalTotal,
                        queueMessage = result.data.queueMessage
                    )
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun submitAttendance(onSuccess: () -> Unit) {
        if (_state.value.hasAttended || _state.value.isLoading || _state.value.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val location = when (val result = freshLocationProvider.get()) {
                is ApiResponse.Success -> result.data
                is ApiResponse.Error -> {
                    _state.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                    return@launch
                }
                ApiResponse.Loading -> { _state.update { it.copy(isSubmitting = false) }; return@launch }
            }
            val token = sessionDataStore.token.first().orEmpty()
            if (token.isBlank()) {
                _state.update { it.copy(isSubmitting = false, errorMessage = "Session tidak valid") }
                return@launch
            }
            val pushResult = safeApiCall {
                driverApi.updateLocation(JsonObject().apply { addProperty("last_position", location.asApiValue()) }, token)
            }
            if (pushResult !is ApiResponse.Success || !pushResult.data.isSuccessful) {
                val error = (pushResult as? ApiResponse.Error)?.message
                    ?: (pushResult as? ApiResponse.Success)?.data?.message?.ifBlank { null }
                    ?: "Server gagal memperbarui lokasi. Coba lagi."
                _state.update { it.copy(isSubmitting = false, errorMessage = error) }
                return@launch
            }
            when (val result = submitAttendanceUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            hasAttended = result.data.hasAttended,
                            queueNumber = result.data.queueNumber,
                            queueTerminal = result.data.queueTerminal,
                            queueTerminalTotal = result.data.queueTerminalTotal,
                            queueMessage = result.data.queueMessage
                        )
                    }
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
    val queueNumber: Int? = null,
    val queueTerminal: String? = null,
    val queueTerminalTotal: Int? = null,
    val queueMessage: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
) {
    val hasQueueNumber: Boolean get() = queueNumber != null && queueNumber > 0
}
