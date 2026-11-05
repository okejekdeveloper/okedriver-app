package id.okejack.okedriver.feature.order.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.okejack.okedriver.core.network.model.ApiResponse
import id.okejack.okedriver.feature.order.domain.model.ChatMessage
import id.okejack.okedriver.feature.order.domain.usecase.GetChatMessagesUseCase
import id.okejack.okedriver.feature.order.domain.usecase.SubmitChatMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val submitChatMessageUseCase: SubmitChatMessageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state

    fun loadMessages(orderId: Long) {
        if (orderId <= 0 || _state.value.isLoading || _state.value.isSending) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch { fetchMessages(orderId) }
    }

    private suspend fun fetchMessages(orderId: Long) {
        when (val result = getChatMessagesUseCase(orderId)) {
            is ApiResponse.Success -> _state.update { it.copy(messages = result.data, isLoading = false) }
            is ApiResponse.Error -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            ApiResponse.Loading -> Unit
        }
    }

    fun sendMessage(orderId: Long, content: String) {
        // Not blocked by a poll that happens to be in flight: that would drop the driver's tap without a word.
        if (content.isBlank() || orderId <= 0 || _state.value.isSending) return
        _state.update { it.copy(isSending = true, sendError = null) }
        viewModelScope.launch {
            when (val result = submitChatMessageUseCase(orderId, content)) {
                is ApiResponse.Success -> {
                    _state.update { it.copy(isSending = false, draftText = if (it.draftText == content) "" else it.draftText) }
                    // The submit reply is only an acknowledgement; like the legacy app, read the conversation back
                    // so the new message appears exactly as the server stored it.
                    fetchMessages(orderId)
                }
                is ApiResponse.Error -> _state.update {
                    it.copy(isSending = false, sendError = result.message)
                }
                ApiResponse.Loading -> Unit
            }
        }
    }

    fun setDraft(text: String) {
        _state.update { it.copy(draftText = text) }
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val draftText: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val sendError: String? = null
)
