package id.okejack.okedriver.feature.order.presentation.incoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.usecase.CancelCurrentOrderUseCase
import id.okejack.okedriver.feature.order.domain.usecase.GetPendingOrderUseCase
import id.okejack.okedriver.feature.order.domain.usecase.UpdateCurrentOrderStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomingOrderViewModel @Inject constructor(
    private val getPendingOrderUseCase: GetPendingOrderUseCase,
    private val updateCurrentOrderStatusUseCase: UpdateCurrentOrderStatusUseCase,
    private val cancelCurrentOrderUseCase: CancelCurrentOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IncomingOrderUiState())
    val state: StateFlow<IncomingOrderUiState> = _state

    fun loadOrder() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPendingOrderUseCase()) {
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

    fun acceptOrder(onAccepted: (Long) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isAccepting = true, errorMessage = null) }
            when (val result = updateCurrentOrderStatusUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            order = result.data,
                            isAccepting = false,
                            errorMessage = null
                        )
                    }
                    onAccepted(result.data.id)
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isAccepting = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun declineOrder(onDeclined: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isDeclining = true, errorMessage = null) }
            when (val result = cancelCurrentOrderUseCase(DriverOrder.CANCEL_REASON_REJECTED)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            isDeclining = false,
                            errorMessage = null
                        )
                    }
                    onDeclined()
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isDeclining = false,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class IncomingOrderUiState(
    val order: DriverOrder? = null,
    val isLoading: Boolean = false,
    val isAccepting: Boolean = false,
    val isDeclining: Boolean = false,
    val errorMessage: String? = null
)
