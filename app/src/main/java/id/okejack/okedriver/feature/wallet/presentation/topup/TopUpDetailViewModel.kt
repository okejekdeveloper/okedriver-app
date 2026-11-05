package id.okejack.okedriver.feature.wallet.presentation.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import id.okejack.okedriver.feature.wallet.domain.model.VirtualAccount
import id.okejack.okedriver.feature.wallet.domain.usecase.CreateTopUpPaymentUseCase
import id.okejack.okedriver.feature.wallet.domain.usecase.GetTopUpDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle
import com.google.gson.Gson

@HiltViewModel
class TopUpDetailViewModel @Inject constructor(
    private val getDetail: GetTopUpDetailUseCase,
    private val createPaymentUseCase: CreateTopUpPaymentUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(TopUpDetailUiState(deposit = runCatching {
        Gson().fromJson(savedStateHandle.get<String>("topup_deposit"), TopUpDeposit::class.java)
    }.getOrNull()))
    val state: StateFlow<TopUpDetailUiState> = _state

    fun load(depositId: Long, initialDeposit: TopUpDeposit? = null) = viewModelScope.launch {
        if (_state.value.isLoading || _state.value.isCreatingPayment) return@launch
        if (depositId <= 0) {
            _state.update { it.copy(errorMessage = "Data deposit tidak valid") }
            return@launch
        }
        if (_state.value.deposit == null && initialDeposit?.id == depositId) saveDeposit(initialDeposit)
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = getDetail(depositId)) {
            is ApiResponse.Success -> {
                saveDeposit(result.data.mergeQr(_state.value.deposit))
                _state.update { it.copy(isLoading = false) }
            }
            is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun createPayment(depositId: Long) = viewModelScope.launch {
        if (_state.value.isLoading || _state.value.isCreatingPayment || _state.value.deposit?.isTerminal == true) return@launch
        _state.update { it.copy(isCreatingPayment = true, errorMessage = null) }
        when (val result = createPaymentUseCase(depositId)) {
            is ApiResponse.Success -> _state.update {
                it.copy(isCreatingPayment = false, virtualAccount = result.data)
            }
            is ApiResponse.Error -> _state.update {
                it.copy(isCreatingPayment = false, errorMessage = result.message)
            }
            ApiResponse.Loading -> Unit
        }
    }

    private fun saveDeposit(deposit: TopUpDeposit) {
        savedStateHandle["topup_deposit"] = Gson().toJson(deposit)
        _state.update { it.copy(deposit = deposit) }
    }
}

data class TopUpDetailUiState(
    val deposit: TopUpDeposit? = null,
    val virtualAccount: VirtualAccount? = null,
    val isLoading: Boolean = false,
    val isCreatingPayment: Boolean = false,
    val errorMessage: String? = null
)
