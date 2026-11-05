package id.okejack.okedriver.feature.order.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatDtoTest {
    private fun messages(json: String) = Gson().fromJson(json, ChatMessagesResponseDto::class.java).toMessageList()

    @Test fun readsTheMessageListUnderTheKeyTheServerUses() {
        val list = messages(
            """{"success":true,"data":{"order_chat_messages":[
                {"content":"Saya sudah di lokasi","type":2,"created_at":"2026-11-04 12:00:00","user":{"name":"Driver"}},
                {"content":"Oke","type":1,"created_at":"2026-11-04 12:01:00","user":{"name":"Budi"}}]}}"""
        )
        assertEquals(listOf("Saya sudah di lokasi", "Oke"), list.map { it.content })
        assertTrue(list[0].isFromDriver)
        assertTrue(list[1].isFromUser)
    }

    @Test fun messagesWithoutAnIdStillRead() {
        // The legacy chat model has no id, so the server may not send one.
        val list = messages("""{"success":true,"data":{"order_chat_messages":[{"content":"a","type":2},{"content":"b","type":2}]}}""")
        assertEquals(2, list.size)
        assertTrue(list.all { it.id == 0L })
    }

    @Test fun anAcknowledgementWithoutMessagesIsAnEmptyListNotAnError() {
        assertTrue(messages("""{"success":true}""").isEmpty())
    }
}
