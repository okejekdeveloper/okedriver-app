package id.okejack.okedriver.feature.order.domain.model

data class ChatMessage(
    val id: Long,
    val content: String,
    val type: Int,
    val createdAt: String,
    val senderName: String
) {
    val isFromDriver: Boolean get() = type == TYPE_DRIVER
    val isFromUser: Boolean get() = type == TYPE_USER

    companion object {
        const val TYPE_ADMIN = 0
        const val TYPE_USER = 1
        const val TYPE_DRIVER = 2
    }
}
