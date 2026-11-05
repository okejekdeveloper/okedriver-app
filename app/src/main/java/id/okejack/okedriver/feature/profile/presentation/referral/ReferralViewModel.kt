package id.okejack.okedriver.feature.profile.presentation.referral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.profile.domain.model.ReferralInfo
import id.okejack.okedriver.feature.profile.domain.usecase.GetReferralInfoUseCase
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val getReferralInfoUseCase: GetReferralInfoUseCase
) : ViewModel() {

    private val calendar = Calendar.getInstance()
    private val _state = MutableStateFlow(
        ReferralUiState(
            month = calendar.get(Calendar.MONTH) + 1,
            year = calendar.get(Calendar.YEAR)
        )
    )
    val state: StateFlow<ReferralUiState> = _state

    init {
        loadReferral()
    }

    fun loadReferral() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getReferralInfoUseCase(current.month, current.year)) {
                is ApiResponse.Success -> _state.update {
                    it.copy(info = result.data, isLoading = false)
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun previousMonth() {
        val current = _state.value
        val newMonth = if (current.month == 1) 12 else current.month - 1
        val newYear = if (current.month == 1) current.year - 1 else current.year
        _state.update { it.copy(month = newMonth, year = newYear) }
        loadReferral()
    }

    fun nextMonth() {
        val current = _state.value
        val newMonth = if (current.month == 12) 1 else current.month + 1
        val newYear = if (current.month == 12) current.year + 1 else current.year
        _state.update { it.copy(month = newMonth, year = newYear) }
        loadReferral()
    }
}

data class ReferralUiState(
    val month: Int,
    val year: Int,
    val info: ReferralInfo = ReferralInfo(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val periodLabel: String get() = "${monthName(month)} $year"
}

private fun monthName(month: Int): String =
    listOf(
        "Januari",
        "Februari",
        "Maret",
        "April",
        "Mei",
        "Juni",
        "Juli",
        "Agustus",
        "September",
        "Oktober",
        "November",
        "Desember"
    ).getOrElse(month - 1) { "-" }
