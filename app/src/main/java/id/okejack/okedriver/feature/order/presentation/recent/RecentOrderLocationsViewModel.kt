package id.okejack.okedriver.feature.order.presentation.recent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.OrderCoordinate
import id.okejack.okedriver.feature.order.domain.usecase.GetRecentOrderLocationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecentOrderLocationsViewModel @Inject constructor(
    private val getRecentOrderLocationsUseCase: GetRecentOrderLocationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RecentOrderLocationsUiState())
    val state: StateFlow<RecentOrderLocationsUiState> = _state

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getRecentOrderLocationsUseCase()) {
                is ApiResponse.Success -> _state.update {
                    it.copy(
                        locations = result.data,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class RecentOrderLocationsUiState(
    val locations: List<OrderCoordinate> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
