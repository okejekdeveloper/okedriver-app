package id.okejack.okedriver.feature.order.presentation.current

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.usecase.CancelCurrentOrderUseCase
import id.okejack.okedriver.feature.order.domain.usecase.GetCurrentOrderUseCase
import id.okejack.okedriver.feature.order.domain.usecase.UpdateCurrentOrderStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrentOrderViewModel @Inject constructor(
    private val getCurrentOrderUseCase: GetCurrentOrderUseCase,
    private val updateCurrentOrderStatusUseCase: UpdateCurrentOrderStatusUseCase,
    private val cancelCurrentOrderUseCase: CancelCurrentOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CurrentOrderUiState())
    val state: StateFlow<CurrentOrderUiState> = _state

    fun loadOrder() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getCurrentOrderUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            order = result.data,
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

    fun updateStatus(onFinished: (Long) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
            when (val result = updateCurrentOrderStatusUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            order = result.data,
                            isUpdatingStatus = false,
                            errorMessage = null
                        )
                    }
                    if (result.data.status == DriverOrder.STATUS_ARRIVED) {
                        onFinished(result.data.id)
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isUpdatingStatus = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun cancelOrder(onCancelled: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isCancelling = true, errorMessage = null) }
            when (val result = cancelCurrentOrderUseCase(DriverOrder.CANCEL_REASON_NO_CONTACT)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = null
                        )
                    }
                    onCancelled()
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isCancelling = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class CurrentOrderUiState(
    val order: DriverOrder? = null,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val isCancelling: Boolean = false,
    val errorMessage: String? = null
)
