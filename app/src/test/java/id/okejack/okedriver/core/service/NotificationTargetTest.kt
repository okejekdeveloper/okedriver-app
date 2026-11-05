package id.okejack.okedriver.core.service

import org.junit.Assert.*
import org.junit.Test

class NotificationTargetTest {
    @Test fun routesAllLegacyPushTypes() {
        listOf("job_received" to NotificationKind.ORDER, "chat" to NotificationKind.CHAT,
            "news" to NotificationKind.NEWS, "bonus" to NotificationKind.BONUS).forEach { (type, kind) ->
            val target = NotificationTarget.fromData(mapOf("__type" to type, "order_id" to "42", "news_id" to "42"))
            assertEquals(kind, target?.kind)
        }
    }

    @Test fun malformedOrMissingIdsAreRejected() {
        listOf("", "abc", "-1", "0", "9223372036854775808").forEach { id ->
            assertNull(NotificationTarget.fromData(mapOf("__type" to "job_received", "order_id" to id)))
            assertNull(NotificationTarget.fromData(mapOf("__type" to "chat", "order_id" to id)))
            assertNull(NotificationTarget.fromData(mapOf("__type" to "news", "news_id" to id)))
        }
        assertNull(NotificationTarget.fromData(mapOf("__type" to "news", "news_id" to "2147483648")))
        assertNull(NotificationTarget.fromData(emptyMap()))
    }

    @Test fun unrecognizedTypedMessagesUseGeneralNotification() {
        assertEquals(NotificationKind.GENERAL, NotificationTarget.fromData(mapOf("__type" to "message"))?.kind)
    }
}
