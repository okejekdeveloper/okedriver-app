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
        if (_state.value.isLoading || _state.value.isUpdatingStatus || _state.value.isCancelling || _state.value.showQris) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = getCurrentOrderUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            order = result.data,
                            isLoading = false,
                            hasLoaded = true,
                            errorMessage = null
                        )
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasLoaded = true,
                            order = if (result.errorCode in listOf("order_not_available", "order_cancelled")) null else it.order,
                            errorMessage = result.message
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun updateStatus(onFinished: (Long) -> Unit) {
        val state = _state.value
        if (state.isLoading || state.isUpdatingStatus || state.isCancelling || state.order?.canUpdateStatus != true) return
        if (state.order.requiresQrisPayment) {
            _state.update { it.copy(showQris = true, finishAfterQris = true) }
            return
        }
        _state.update { it.copy(isUpdatingStatus = true, errorMessage = null) }
        viewModelScope.launch {
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
                            errorMessage = result.message,
                            showQris = (result.errorCode == "payment_failed" || result.message == "payment_failed") && it.order?.paymentProvider == "qris",
                            finishAfterQris = (result.errorCode == "payment_failed" || result.message == "payment_failed") && it.order?.paymentProvider == "qris"
                        )
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun cancelOrder(
        reasonCode: Int = DriverOrder.CANCEL_REASON_NO_CONTACT,
        onCancelled: () -> Unit
    ) {
        if (_state.value.isLoading || _state.value.isUpdatingStatus || _state.value.isCancelling || _state.value.order?.canCancel != true) return
        _state.update { it.copy(isCancelling = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = cancelCurrentOrderUseCase(reasonCode)) {
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

    fun openQris() {
        if (_state.value.order?.requiresQrisPayment == true && !_state.value.isUpdatingStatus) {
            _state.update { it.copy(showQris = true, finishAfterQris = false) }
        }
    }

    fun closeQris() {
        _state.update { it.copy(showQris = false, finishAfterQris = false) }
        loadOrder()
    }

    fun onQrisComplete(onFinished: (Long) -> Unit) {
        val expectedOrderId = _state.value.order?.id ?: return
        val finishRequested = _state.value.finishAfterQris
        _state.update { it.copy(showQris = false, finishAfterQris = false, isLoading = true) }
        viewModelScope.launch {
            when (val result = getCurrentOrderUseCase()) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(order = result.data, isLoading = false, errorMessage = null) }
                    // Reload server state before finishing; prepayment only refreshes the screen.
                    if (result.data.id == expectedOrderId && finishRequested && !result.data.requiresQrisPayment) {
                        if (result.data.status == DriverOrder.STATUS_ARRIVED) onFinished(result.data.id)
                        else updateStatus(onFinished)
                    }
                }
                is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                ApiResponse.Loading -> _state.update { it.copy(isLoading = false) }
            }
        }
    }
}

data class CurrentOrderUiState(
    val order: DriverOrder? = null,
    val isLoading: Boolean = false,
    val isUpdatingStatus: Boolean = false,
    val isCancelling: Boolean = false,
    val errorMessage: String? = null,
    val showQris: Boolean = false,
    val finishAfterQris: Boolean = false,
    val hasLoaded: Boolean = false
)
