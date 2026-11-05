package id.okejack.okedriver.feature.order.data.remote.dto

import id.okejack.okedriver.core.network.model.ServerErrors

import com.google.gson.annotations.SerializedName
import id.okejack.okedriver.feature.order.domain.model.ChatMessage

data class ChatMessagesResponseDto(
    val success: Boolean = false,
    val status: Boolean = false,
    val error: String = "",
    val message: String = "",
    val data: ChatDataDto? = null
) {
    val isSuccessful: Boolean get() = success || status
    val errorMessage: String get() = ServerErrors.message(error, message, "Gagal memuat chat")
}

data class ChatDataDto(
    /** The key the server actually uses for the message list (the legacy app reads `data.order_chat_messages`). */
    @SerializedName("order_chat_messages") val orderChatMessages: List<ChatMessageDto>? = null,
    val chats: List<ChatMessageDto>? = null,
    val messages: List<ChatMessageDto>? = null
)

data class ChatMessageDto(
    val id: Long? = null,
    val content: String? = null,
    val type: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val user: ChatUserDto? = null
)

data class ChatUserDto(
    val id: Long? = null,
    val name: String? = null
)

fun ChatMessageDto.toDomain(): ChatMessage =
    ChatMessage(
        id = id ?: 0L,
        content = content.orEmpty(),
        type = type ?: ChatMessage.TYPE_USER,
        createdAt = createdAt.orEmpty(),
        senderName = user?.name.orEmpty()
    )

fun ChatMessagesResponseDto.toMessageList(): List<ChatMessage> =
    (data?.orderChatMessages ?: data?.chats ?: data?.messages).orEmpty()
        .map { it.toDomain() }
