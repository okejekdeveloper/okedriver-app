package id.okejack.okedriver.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.WalletBalance
import id.okejack.okedriver.feature.wallet.domain.usecase.GetBalanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = _state

    init {
        loadBalance()
    }

    fun loadBalance() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getBalanceUseCase()) {
                is ApiResponse.Success -> {
                    _state.update {
                        it.copy(balance = result.data, isLoading = false, errorMessage = null)
                    }
                }
                is ApiResponse.Error -> {
                    _state.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class WalletUiState(
    val balance: WalletBalance? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
