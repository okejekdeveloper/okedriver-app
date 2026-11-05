package id.okejack.okedriver.feature.wallet.presentation.topup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.PaymentMethod
import id.okejack.okedriver.feature.wallet.domain.usecase.RequestTopUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import id.okejack.okedriver.feature.wallet.domain.model.TopUpDeposit
import id.okejack.okedriver.feature.wallet.domain.model.TopUpCatalog

@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val requestTopUp: RequestTopUpUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(TopUpUiState())
    val state: StateFlow<TopUpUiState> = _state

    init { loadPaymentMethods() }

    fun loadPaymentMethods() {
        _state.update { it.copy(isLoading = false, paymentMethods = TopUpCatalog.methods,
            selectedPaymentMethod = it.selectedPaymentMethod ?: TopUpCatalog.methods.first()) }
    }

    fun selectAmount(amount: Long) = _state.update { it.copy(amount = amount, errorMessage = null) }
    fun selectPaymentMethod(method: PaymentMethod) {
        if (_state.value.isSubmitting) return
        if (!TopUpCatalog.isAvailable(method.id)) {
            _state.update { it.copy(errorMessage = "Transfer bank hanya tersedia pukul 05.00–21.00 WIB. Pilih QRIS atau Virtual Account.") }
        } else _state.update { it.copy(selectedPaymentMethod = method, errorMessage = null) }
    }

    fun submit(onCreated: (TopUpDeposit) -> Unit) {
        val current = _state.value
        if (current.isSubmitting) return
        val method = current.selectedPaymentMethod ?: run {
            _state.update { it.copy(errorMessage = "Pilih metode pembayaran") }
            return
        }
        if (current.amount <= 0) {
            _state.update { it.copy(errorMessage = "Pilih nominal top up") }
            return
        }
        if (!TopUpCatalog.isAvailable(method.id)) {
            _state.update { it.copy(errorMessage = "Metode ini sedang tidak tersedia. Pilih QRIS atau Virtual Account.") }
            return
        }
        _state.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = requestTopUp(method.id, current.amount)) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(isSubmitting = false) }
                    if (result.data.id > 0) onCreated(result.data)
                    else _state.update { it.copy(errorMessage = "Data deposit tidak valid") }
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isSubmitting = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class TopUpUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val amount: Long = 100_000,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)
