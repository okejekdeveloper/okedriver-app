package id.okejack.okedriver.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.Transaction
import id.okejack.okedriver.feature.wallet.domain.usecase.GetTransactionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionHistoryUiState())
    val state: StateFlow<TransactionHistoryUiState> = _state

    private var currentPage = 1
    private var withdrawableOnly = false
    private var initialized = false

    fun initialize(withdrawableOnly: Boolean) {
        if (initialized && this.withdrawableOnly == withdrawableOnly) return
        initialized = true
        this.withdrawableOnly = withdrawableOnly
        refresh()
    }

    fun refresh() {
        currentPage = 1
        _state.update { it.copy(transactions = emptyList(), hasMore = true, errorMessage = null) }
        loadPage(1)
    }

    fun loadMore() {
        if (_state.value.isLoadingMore || !_state.value.hasMore) return
        loadPage(currentPage)
    }

    private fun loadPage(page: Int) {
        viewModelScope.launch {
            if (page == 1) {
                _state.update { it.copy(isLoading = true, errorMessage = null) }
            } else {
                _state.update { it.copy(isLoadingMore = true) }
            }

            when (val result = getTransactionsUseCase(page, withdrawableOnly)) {
                is ApiResponse.Success -> {
                    val newItems = result.data
                    _state.update { s ->
                        s.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            transactions = if (page == 1) newItems else s.transactions + newItems,
                            hasMore = newItems.isNotEmpty()
                        )
                    }
                    if (newItems.isNotEmpty()) currentPage = page + 1
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isLoading = false, isLoadingMore = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }
}

data class TransactionHistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null
)
