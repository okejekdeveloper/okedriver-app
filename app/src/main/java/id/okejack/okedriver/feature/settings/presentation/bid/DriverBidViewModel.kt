package id.okejack.okedriver.feature.settings.presentation.bid

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.settings.domain.model.DriverBidPreferences
import id.okejack.okedriver.feature.settings.domain.usecase.GetDriverSettingsUseCase
import id.okejack.okedriver.feature.settings.domain.usecase.UpdateDriverBidUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DriverBidViewModel @Inject constructor(
    private val getSettings: GetDriverSettingsUseCase,
    private val updateBid: UpdateDriverBidUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(DriverBidUiState())
    val state: StateFlow<DriverBidUiState> = _state

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        val paymentRange = PAYMENT_LIMITS
        val distanceRange = DISTANCE_LIMITS
        _state.update { it.copy(paymentLimits = paymentRange, distanceLimits = distanceRange) }
        when (val result = getSettings()) {
            is ApiResponse.Success -> _state.update {
                it.copy(
                    isLoading = false,
                    driverType = result.data.driverType,
                    preferences = result.data.bid
                )
            }
            is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun setPaymentIndex(index: Int) = updatePreferences {
        copy(preferredPayment = _state.value.paymentLimits[index.coerceIn(_state.value.paymentLimits.indices)])
    }

    fun setDistanceIndex(index: Int) = updatePreferences {
        copy(preferredDistance = _state.value.distanceLimits[index.coerceIn(_state.value.distanceLimits.indices)])
    }

    fun setRide(enabled: Boolean) = updatePreferences { copy(rideEnabled = enabled) }
    fun setDelivery(enabled: Boolean) = updatePreferences { copy(deliveryEnabled = enabled) }
    fun setShopping(enabled: Boolean) = updatePreferences { copy(shoppingEnabled = enabled) }
    fun setFood(enabled: Boolean) = updatePreferences { copy(foodEnabled = enabled) }

    fun selectAllServices() = updatePreferences {
        copy(rideEnabled = true, deliveryEnabled = true, shoppingEnabled = true, foodEnabled = true)
    }

    fun submit() = viewModelScope.launch {
        _state.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
        val current = _state.value
        when (val result = updateBid(current.preferences, current.isRideDriver)) {
            is ApiResponse.Success -> _state.update {
                it.copy(isSaving = false, preferences = result.data.bid, successMessage = "Preferensi tersimpan")
            }
            is ApiResponse.Error -> _state.update { it.copy(isSaving = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun clearMessage() = _state.update { it.copy(errorMessage = null, successMessage = null) }

    private fun updatePreferences(transform: DriverBidPreferences.() -> DriverBidPreferences) {
        _state.update { it.copy(preferences = it.preferences.transform(), errorMessage = null) }
    }

    companion object {
        val PAYMENT_LIMITS = listOf(50_000_000, 25_000, 80_000, 125_000, 250_000, 500_000, 1_000_000, 1_500_000, 2_000_000)
        val DISTANCE_LIMITS = listOf(1_000, 3, 6, 10, 25)
    }
}

data class DriverBidUiState(
    val driverType: Int = 0,
    val preferences: DriverBidPreferences = DriverBidPreferences(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val paymentLimits: List<Int> = DriverBidViewModel.PAYMENT_LIMITS,
    val distanceLimits: List<Int> = DriverBidViewModel.DISTANCE_LIMITS
) {
    val isRideDriver: Boolean get() = driverType == 0
}
