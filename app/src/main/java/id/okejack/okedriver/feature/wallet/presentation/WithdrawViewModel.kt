package id.okejack.okedriver.feature.wallet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.usecase.GetBalanceUseCase
import id.okejack.okedriver.feature.wallet.domain.usecase.GetPendingWithdrawUseCase
import id.okejack.okedriver.feature.wallet.domain.usecase.SubmitWithdrawUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val getBalanceUseCase: GetBalanceUseCase,
    private val getPendingWithdrawUseCase: GetPendingWithdrawUseCase,
    private val submitWithdrawUseCase: SubmitWithdrawUseCase
) : ViewModel() {

    companion object {
        val MINIMUM_WITHDRAW = BigDecimal.ONE
        val MINIMUM_CONVERT = BigDecimal(100)
        val MAXIMUM_WITHDRAW = BigDecimal(1_000_000)
    }

    private val _state = MutableStateFlow(WithdrawUiState())
    val state: StateFlow<WithdrawUiState> = _state

    fun loadData() {
        if (_state.value.isLoading || _state.value.isSubmitting) return
        _state.update { it.copy(isLoading = true, hasLoadedData = false, errorMessage = null) }
        viewModelScope.launch {
            var balance = BigDecimal.ZERO
            var pendingCount = 0L

            when (val result = getBalanceUseCase()) {
                is ApiResponse.Success -> balance = result.data.withdrawableBalance
                is ApiResponse.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                    return@launch
                }
                else -> Unit
            }

            when (val result = getPendingWithdrawUseCase()) {
                is ApiResponse.Success -> pendingCount = result.data
                is ApiResponse.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                    return@launch
                }
                else -> Unit
            }

            _state.update {
                it.copy(isLoading = false, hasLoadedData = true, withdrawableBalance = balance, pendingCount = pendingCount)
            }
        }
    }

    fun onAmountChange(value: String) {
        val cleaned = value.filter { it.isDigit() }
        _state.update { it.copy(amount = cleaned, errorMessage = null) }
    }

    fun submit(convertToBalance: Boolean = false, onSuccess: () -> Unit) {
        val state = _state.value
        if (state.isSubmitting || state.isLoading || !state.hasLoadedData) return
        if (state.hasPendingRequest) {
            _state.update { it.copy(errorMessage = "Masih ada permintaan penarikan yang diproses") }
            return
        }
        val amount = state.amountValue
        when {
            amount < if (convertToBalance) MINIMUM_CONVERT else MINIMUM_WITHDRAW -> {
                _state.update { it.copy(errorMessage = if (convertToBalance) "Minimum konversi ${MINIMUM_CONVERT.toRupiahPlain()}" else "Masukkan nominal penarikan") }
                return
            }
            !convertToBalance && amount > MAXIMUM_WITHDRAW -> {
                _state.update { it.copy(errorMessage = "Maksimum penarikan ${MAXIMUM_WITHDRAW.toRupiahPlain()} per permintaan") }
                return
            }
            amount > state.withdrawableBalance -> {
                _state.update { it.copy(errorMessage = "Jumlah melebihi saldo yang bisa ditarik") }
                return
            }
        }

        _state.update { it.copy(isSubmitting = true, wasConversion = convertToBalance, errorMessage = null) }
        viewModelScope.launch {
            when (val result = submitWithdrawUseCase(amount, convertToBalance)) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                else -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}

data class WithdrawUiState(
    val withdrawableBalance: BigDecimal = BigDecimal.ZERO,
    val pendingCount: Long = 0L,
    val amount: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val hasLoadedData: Boolean = false,
    val wasConversion: Boolean = false
) {
    val hasPendingRequest: Boolean get() = pendingCount > 0
    val amountValue: BigDecimal get() = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
}

private fun BigDecimal.toRupiahPlain(): String {
    return "Rp ${this.toLong().toString().reversed().chunked(3).joinToString(".").reversed()}"
}
