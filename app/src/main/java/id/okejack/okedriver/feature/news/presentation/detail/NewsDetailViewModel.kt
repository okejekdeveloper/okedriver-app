package id.okejack.okedriver.feature.news.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.news.domain.model.DriverNews
import id.okejack.okedriver.feature.news.domain.usecase.GetNewsDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsDetailViewModel @Inject constructor(
    private val getNewsDetail: GetNewsDetailUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(NewsDetailUiState())
    val state: StateFlow<NewsDetailUiState> = _state
    private var loadedId = 0

    fun load(id: Int, force: Boolean = false) {
        if (!force && loadedId == id) return
        loadedId = id
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getNewsDetail(id)) {
                is ApiResponse.Success -> _state.update { it.copy(isLoading = false, news = result.data) }
                is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                ApiResponse.Loading -> Unit
            }
        }
    }
}

data class NewsDetailUiState(
    val news: DriverNews? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
