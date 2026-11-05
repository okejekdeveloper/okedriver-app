package id.okejack.okedriver.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.auth.domain.usecase.LogoutUseCase
import id.okejack.okedriver.feature.home.domain.model.HomeDriver
import id.okejack.okedriver.feature.home.domain.model.HomeSupportInfo
import id.okejack.okedriver.feature.home.domain.usecase.GetDriverInfoUseCase
import id.okejack.okedriver.feature.home.domain.usecase.GetHomeSupportInfoUseCase
import id.okejack.okedriver.feature.home.domain.usecase.RegisterQueueUseCase
import id.okejack.okedriver.feature.home.domain.usecase.ToggleOnlineStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDriverInfoUseCase: GetDriverInfoUseCase,
    private val getHomeSupportInfoUseCase: GetHomeSupportInfoUseCase,
    private val registerQueueUseCase: RegisterQueueUseCase,
    private val toggleOnlineStatusUseCase: ToggleOnlineStatusUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        refreshDriverInfo()
        refreshSupportInfo()
    }

    fun refreshDriverInfo() {
        if (_state.value.isLoading || _state.value.isTogglingStatus || _state.value.isLoggingOut) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getDriverInfoUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            driver = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun refreshSupportInfo() {
        if (_state.value.isLoadingSupport || _state.value.isLoggingOut) return
        _state.update { it.copy(isLoadingSupport = true) }
        viewModelScope.launch {
            _state.update { it.copy(isLoadingSupport = true) }
            when (val result = getHomeSupportInfoUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            supportInfo = result.data,
                            isLoadingSupport = false
                        )
                    }
                }
                is ApiResponse.Error -> {
                    _state.update { it.copy(isLoadingSupport = false) }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun toggleOnlineStatus(onStatusChanged: (Boolean) -> Unit = {}) {
        if (_state.value.isTogglingStatus || _state.value.isLoading || _state.value.isLoggingOut) return
        _state.update { it.copy(isTogglingStatus = true, errorMessage = null) }
        viewModelScope.launch {
            _state.update { it.copy(isTogglingStatus = true, errorMessage = null) }
            when (val result = toggleOnlineStatusUseCase()) {
                is ApiResponse.Success -> {
                    // Hanya driver/info yang mengirim enable_*; dari toggle-status cukup ambil status online.
                    _state.update {
                        it.copy(
                            driver = it.driver?.copy(isOnline = result.data.isOnline) ?: result.data,
                            isTogglingStatus = false,
                            errorMessage = null
                        )
                    }
                    onStatusChanged(result.data.isOnline)
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isTogglingStatus = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun registerQueue(latLng: String, terminal: String) {
        if (_state.value.isRegisteringQueue) return
        val coordinate = latLng.split(",").map { it.trim().toDoubleOrNull() }
        if (coordinate.size != 2 || coordinate[0] == null || coordinate[1] == null ||
            coordinate[0]!! !in -90.0..90.0 || coordinate[1]!! !in -180.0..180.0 ||
            (coordinate[0] == 0.0 && coordinate[1] == 0.0)
        ) {
            showError("Lokasi GPS belum tersedia. Tunggu lokasi terbaru dan coba lagi.")
            return
        }
        _state.update { it.copy(isRegisteringQueue = true, errorMessage = null) }
        viewModelScope.launch {
            _state.update { it.copy(isRegisteringQueue = true, errorMessage = null) }
            when (val result = registerQueueUseCase(latLng, terminal)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            supportInfo = it.supportInfo.copy(
                                queueNumber = result.data.queueNumber,
                                queueTerminal = result.data.queueTerminal,
                                queueTerminalTotal = result.data.queueTerminalTotal,
                                queueMessage = result.data.queueMessage
                            ),
                            isRegisteringQueue = false,
                            errorMessage = null
                        )
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isRegisteringQueue = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun showError(message: String) {
        _state.update { it.copy(errorMessage = message) }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoggingOut = true, errorMessage = null) }
            try {
                logoutUseCase()
            } finally {
                _state.update { it.copy(isLoggingOut = false) }
                onComplete()
            }
        }
    }
}

data class HomeUiState(
    val driver: HomeDriver? = null,
    val supportInfo: HomeSupportInfo = HomeSupportInfo(),
    val isLoading: Boolean = false,
    val isLoadingSupport: Boolean = false,
    val isTogglingStatus: Boolean = false,
    val isRegisteringQueue: Boolean = false,
    val isLoggingOut: Boolean = false,
    val errorMessage: String? = null
)
