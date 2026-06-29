package id.okejack.okedriver.feature.order.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.DriverOrder
import id.okejack.okedriver.feature.order.domain.usecase.GetOrdersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getOrdersUseCase: GetOrdersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrderHistoryUiState())
    val state: StateFlow<OrderHistoryUiState> = _state

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getOrdersUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            orders = result.data,
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
}

data class OrderHistoryUiState(
    val orders: List<DriverOrder> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val inProgressOrders: List<DriverOrder>
        get() = orders.filter { it.status != DriverOrder.STATUS_ARRIVED && it.status != DriverOrder.STATUS_CANCELLED }

    val finishedOrders: List<DriverOrder>
        get() = orders.filter { it.status == DriverOrder.STATUS_ARRIVED || it.status == DriverOrder.STATUS_CANCELLED }
}
