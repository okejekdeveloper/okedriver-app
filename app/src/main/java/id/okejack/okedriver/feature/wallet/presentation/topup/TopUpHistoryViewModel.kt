package id.okejack.okedriver.feature.wallet.presentation.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import id.okejack.okedriver.feature.wallet.domain.usecase.GetTopUpHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Job

@HiltViewModel
class TopUpHistoryViewModel @Inject constructor(
    private val getHistory: GetTopUpHistoryUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(TopUpHistoryUiState())
    val state: StateFlow<TopUpHistoryUiState> = _state
    private var nextPage = 1
    private var loadJob: Job? = null

    init { load() }

    fun load() {
        loadJob?.cancel()
        nextPage = 1
        _state.update { it.copy(isLoading = true, isLoadingMore = false, hasMore = true, errorMessage = null) }
        loadPage(1)
    }

    fun applyFilters(search: String, dateFrom: String = _state.value.dateFrom, dateTo: String = _state.value.dateTo) {
        _state.update { it.copy(search = search.trim(), dateFrom = dateFrom, dateTo = dateTo) }
        load()
    }

    fun loadMore() {
        if (_state.value.isLoading || _state.value.isLoadingMore || !_state.value.hasMore) return
        _state.update { it.copy(isLoadingMore = true, errorMessage = null) }
        loadPage(nextPage)
    }

    private fun loadPage(page: Int) {
        val query = _state.value
        loadJob = viewModelScope.launch {
            when (val result = getHistory(page, query.dateFrom, query.dateTo, query.search)) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(isLoading = false, isLoadingMore = false,
                        deposits = (if (page == 1) result.data else it.deposits + result.data).distinctBy { deposit -> deposit.id },
                        hasMore = result.data.isNotEmpty()) }
                    nextPage = page + 1
                }
                is ApiResponse.Error -> _state.update { it.copy(isLoading = false, isLoadingMore = false, errorMessage = result.message) }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class TopUpHistoryUiState(
    val deposits: List<TopUpDeposit> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val dateFrom: String = "",
    val dateTo: String = "",
    val search: String = ""
)
