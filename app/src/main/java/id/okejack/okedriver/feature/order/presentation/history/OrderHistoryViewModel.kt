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
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getOrdersUseCase: GetOrdersUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OrderHistoryUiState())
    val state: StateFlow<OrderHistoryUiState> = _state
    private var loadJob: Job? = null
    private var nextPage = 1

    init {
        loadOrders()
    }

    fun loadOrders(inProgress: Boolean = _state.value.inProgress) {
        loadJob?.cancel()
        nextPage = 1
        _state.update { it.copy(inProgress = inProgress, hasMore = true, isLoading = true, isLoadingMore = false, errorMessage = null) }
        loadPage(1, inProgress)
    }

    fun loadMore() {
        if (_state.value.isLoading || _state.value.isLoadingMore || !_state.value.hasMore) return
        _state.update { it.copy(isLoadingMore = true) }
        loadPage(nextPage, _state.value.inProgress)
    }

    private fun loadPage(page: Int, inProgress: Boolean) {
        loadJob = viewModelScope.launch {
            when (val result = getOrdersUseCase(page, inProgress)) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(
                            orders = if (page == 1) result.data else (it.orders + result.data).distinctBy { order -> order.id },
                            isLoading = false,
                            errorMessage = null,
                            isLoadingMore = false,
                            hasMore = result.data.isNotEmpty()
                        )
                    }
                    nextPage = page + 1
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false, isLoadingMore = false,
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
    val errorMessage: String? = null,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val inProgress: Boolean = false
) {
    val inProgressOrders: List<DriverOrder>
        get() = orders.filter { it.status != DriverOrder.STATUS_ARRIVED && it.status != DriverOrder.STATUS_CANCELLED }

    val finishedOrders: List<DriverOrder>
        get() = orders.filter { it.status == DriverOrder.STATUS_ARRIVED || it.status == DriverOrder.STATUS_CANCELLED }
}
