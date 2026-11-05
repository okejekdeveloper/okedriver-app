package id.okejack.okedriver.feature.order.presentation.incoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.usecase.CancelCurrentOrderUseCase
import id.okejack.okedriver.feature.order.domain.usecase.GetPendingOrderUseCase
import id.okejack.okedriver.feature.order.domain.usecase.GetPushedOrderUseCase
import id.okejack.okedriver.feature.order.domain.usecase.UpdateCurrentOrderStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomingOrderViewModel @Inject constructor(
    private val getPendingOrderUseCase: GetPendingOrderUseCase,
    private val getPushedOrderUseCase: GetPushedOrderUseCase,
    private val updateCurrentOrderStatusUseCase: UpdateCurrentOrderStatusUseCase,
    private val cancelCurrentOrderUseCase: CancelCurrentOrderUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(IncomingOrderUiState())
    val state: StateFlow<IncomingOrderUiState> = _state
    private var expectedOrderId: Long = 0L

    fun loadOrder(orderId: Long = expectedOrderId) {
        expectedOrderId = orderId
        if (_state.value.isLoading || _state.value.isAccepting || _state.value.isDeclining) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = getPendingOrderUseCase()) {
                is ApiResponse.Success -> {
                    if ((expectedOrderId > 0 && result.data.id != expectedOrderId) || result.data.status != DriverOrder.STATUS_PENDING) {
                        _state.update { it.copy(order = null, isLoading = false, errorMessage = "Order ini sudah tidak tersedia. Periksa order aktif Anda.") }
                        return@launch
                    }
                    _state.update {
                        it.copy(
                            order = result.data,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is ApiResponse.Error -> {
                    // The server had no pending order to give us (or could not be reached) but the push itself
                    // offered this order: show that offer, as the legacy app does. An explicit server rejection
                    // (cancelled / not available) is never overridden by the push.
                    val pushed = if (result.errorCode.isNullOrBlank() && expectedOrderId > 0) getPushedOrderUseCase(expectedOrderId) else null
                    if (pushed != null) {
                        _state.update { it.copy(order = pushed, isLoading = false, errorMessage = null) }
                        return@launch
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            order = if (result.errorCode in listOf("order_not_available", "order_cancelled")) null else it.order,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun acceptOrder(onAccepted: (Long) -> Unit) {
        if (_state.value.order == null || _state.value.isLoading || _state.value.isAccepting || _state.value.isDeclining) return
        _state.update { it.copy(isAccepting = true, errorMessage = null) }
        viewModelScope.launch {
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
        if (_state.value.order == null || _state.value.isLoading || _state.value.isAccepting || _state.value.isDeclining) return
        _state.update { it.copy(isDeclining = true, errorMessage = null) }
        viewModelScope.launch {
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
