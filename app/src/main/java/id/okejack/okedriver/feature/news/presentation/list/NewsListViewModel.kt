package id.okejack.okedriver.feature.news.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.news.domain.model.DriverNews
import id.okejack.okedriver.feature.news.domain.usecase.GetNewsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsListViewModel @Inject constructor(
    private val getNews: GetNewsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(NewsListUiState())
    val state: StateFlow<NewsListUiState> = _state
    private var nextPage = 1

    init { loadNextPage() }

    fun refresh() {
        if (_state.value.isRefreshing) return
        nextPage = 1
        loadPage(1)
    }

    fun loadNextPage() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || current.isRefreshing || !current.hasMore) return
        loadPage(nextPage)
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }

    private fun loadPage(page: Int) = viewModelScope.launch {
        _state.update {
            when {
                page == 1 && it.items.isEmpty() -> it.copy(isLoading = true, errorMessage = null)
                page == 1 -> it.copy(isRefreshing = true, errorMessage = null)
                else -> it.copy(isLoadingMore = true, errorMessage = null)
            }
        }

        when (val result = getNews(page)) {
            is ApiResponse.Success -> {
                val pageData = result.data
                _state.update { current ->
                    val merged = if (page == 1) pageData.items
                    else (current.items + pageData.items).distinctBy { it.id }
                    current.copy(
                        items = merged,
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        hasMore = pageData.hasMore
                    )
                }
                nextPage = pageData.currentPage + 1
            }
            is ApiResponse.Error -> _state.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = result.message
                )
            }
            ApiResponse.Loading -> Unit
        }
    }
}

data class NewsListUiState(
    val items: List<DriverNews> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null
)
