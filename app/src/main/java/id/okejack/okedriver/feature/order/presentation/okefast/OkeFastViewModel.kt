package id.okejack.okedriver.feature.order.presentation.okefast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.usecase.GenerateOkeFastCodeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OkeFastViewModel @Inject constructor(
    private val generateOkeFastCodeUseCase: GenerateOkeFastCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OkeFastUiState())
    val state: StateFlow<OkeFastUiState> = _state

    fun generateCode() {
        viewModelScope.launch {
            _state.update { it.copy(isGenerating = true, errorMessage = null) }
            when (val result = generateOkeFastCodeUseCase()) {
                is ApiResponse.Success -> _state.update {
                    it.copy(
                        code = result.data,
                        isGenerating = false,
                        errorMessage = null
                    )
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isGenerating = false, errorMessage = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}

data class OkeFastUiState(
    val code: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
) {
    val formattedCode: String
        get() = if (code.length == 6) "${code.take(3)} ${code.drop(3)}" else code
}
