package id.okejack.okedriver.feature.wallet.presentation.bonus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.wallet.domain.model.BonusBox
import id.okejack.okedriver.feature.wallet.domain.usecase.GetBonusBoxesUseCase
import id.okejack.okedriver.feature.wallet.domain.usecase.OpenBonusBoxUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BonusBoxViewModel @Inject constructor(
    private val getBonusBoxes: GetBonusBoxesUseCase,
    private val openBonusBox: OpenBonusBoxUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(BonusBoxUiState())
    val state: StateFlow<BonusBoxUiState> = _state

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        when (val result = getBonusBoxes()) {
            is ApiResponse.Success -> _state.update { it.copy(isLoading = false, boxes = result.data) }
            is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun open(id: Int) = viewModelScope.launch {
        _state.update { it.copy(openingId = id, errorMessage = null) }
        when (val result = openBonusBox(id)) {
            is ApiResponse.Success -> _state.update { state ->
                state.copy(
                    openingId = null,
                    boxes = state.boxes.map { if (it.id == id) result.data else it },
                    revealedBox = result.data
                )
            }
            is ApiResponse.Error -> _state.update { it.copy(openingId = null, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun dismissResult() = _state.update { it.copy(revealedBox = null) }
}

data class BonusBoxUiState(
    val boxes: List<BonusBox> = emptyList(),
    val revealedBox: BonusBox? = null,
    val openingId: Int? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
