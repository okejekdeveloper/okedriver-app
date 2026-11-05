package id.okejack.okedriver.core.service

enum class NotificationKind { ORDER, CHAT, NEWS, BONUS, GENERAL }

data class NotificationTarget(val kind: NotificationKind, val id: Long = 0) {
    val isValid: Boolean get() = when (kind) {
        NotificationKind.ORDER, NotificationKind.CHAT -> id > 0
        NotificationKind.NEWS -> id in 1..Int.MAX_VALUE.toLong()
        NotificationKind.BONUS, NotificationKind.GENERAL -> true
    }

    companion object {
        fun fromData(data: Map<String, String>): NotificationTarget? {
            val kind = when (data["__type"]) {
                "job_received" -> NotificationKind.ORDER
                "chat" -> NotificationKind.CHAT
                "news" -> NotificationKind.NEWS
                "bonus" -> NotificationKind.BONUS
                null -> return null
                else -> NotificationKind.GENERAL
            }
            val id = when (kind) {
                NotificationKind.ORDER, NotificationKind.CHAT -> data["order_id"]?.toLongOrNull() ?: 0
                NotificationKind.NEWS -> data["news_id"]?.toLongOrNull() ?: 0
                else -> 0
            }
            return NotificationTarget(kind, id).takeIf { it.isValid }
        }
    }
}
