package id.okejack.okedriver.feature.order.presentation.qris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.OrderQrisPayment
import id.okejack.okedriver.feature.order.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderQrisViewModel @Inject constructor(private val repository: OrderRepository) : ViewModel() {
    private val _state = MutableStateFlow(OrderQrisUiState())
    val state: StateFlow<OrderQrisUiState> = _state

    fun generate(orderId: Long) {
        if (_state.value.isBusy) return
        _state.value = OrderQrisUiState(isBusy = true)
        viewModelScope.launch {
            handle(repository.generateOrderQris(orderId))
        }
    }

    fun check(orderId: Long) {
        if (_state.value.isBusy || _state.value.isComplete) return
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch { handle(repository.checkOrderQrisStatus(orderId)) }
    }

    fun switchToCash(orderId: Long) {
        if (_state.value.isBusy || _state.value.isComplete) return
        _state.update { it.copy(isBusy = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.switchOrderToCash(orderId)) {
                is ApiResponse.Success -> _state.update { it.copy(isBusy = false, switchedToCash = true) }
                is ApiResponse.Error -> _state.update { it.copy(isBusy = false, errorMessage = result.message) }
                ApiResponse.Loading -> _state.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun handle(result: ApiResponse<OrderQrisPayment>) {
        when (result) {
            is ApiResponse.Success -> _state.update {
                it.copy(
                    isBusy = false,
                    payment = result.data.copy(
                        qrCodeUrl = result.data.qrCodeUrl.ifBlank { it.payment.qrCodeUrl },
                        amount = result.data.amount ?: it.payment.amount
                    ),
                    errorMessage = null
                )
            }
            is ApiResponse.Error -> _state.update { it.copy(isBusy = false, errorMessage = result.message) }
            ApiResponse.Loading -> _state.update { it.copy(isBusy = false) }
        }
    }
}

data class OrderQrisUiState(
    val payment: OrderQrisPayment = OrderQrisPayment(),
    val isBusy: Boolean = false,
    val switchedToCash: Boolean = false,
    val errorMessage: String? = null
) {
    val isComplete: Boolean get() = payment.isPaid || switchedToCash
}
