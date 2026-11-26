package id.okejack.okedriver.feature.order.data.remote.dto

import com.google.gson.Gson
import id.okejack.okedriver.core.network.model.ApiResponse
import org.junit.Assert.*
import org.junit.Test

class EditShoppingItemsDtoTest {
    private val gson = Gson()

    @Test fun successfulAcknowledgementDoesNotRequireAnOrderPayload() {
        for (json in listOf("""{"success":true}""", """{"status":true,"data":null}""",
            """{"success":true,"data":{}}""")) {
            val response = gson.fromJson(json, EditShoppingItemsResponseDto::class.java)
            assertEquals(ApiResponse.Success(Unit), response.toSaveResult())
        }
    }

    @Test fun rejectedEditPreservesServerErrorAndNeverReportsSuccess() {
        val response = gson.fromJson("""{"success":false,"error":"max_shopping_edit"}""",
            EditShoppingItemsResponseDto::class.java)
        val result = response.toSaveResult()
        assertTrue(result is ApiResponse.Error)
        assertEquals("max_shopping_edit", (result as ApiResponse.Error).errorCode)
    }

    @Test fun fullOrderResponseStillAcknowledgesSave() {
        val response = gson.fromJson("""{"success":true,"data":{"order":{"id":42,"status":2}}}""",
            EditShoppingItemsResponseDto::class.java)
        assertEquals(ApiResponse.Success(Unit), response.toSaveResult())
    }
}
