package id.okejack.okedriver.feature.wallet.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BonusBoxDtoTest {
    @Test
    fun `maps opened bonus box response`() {
        val response = Gson().fromJson(
            """
            {
              "success": true,
              "data": {
                "bonus_box": {
                  "id": 9,
                  "type": 1,
                  "message": "Bonus harian",
                  "content": 25000,
                  "redeemed": true
                }
              }
            }
            """.trimIndent(),
            BonusBoxResponseDto::class.java
        )

        val box = requireNotNull(response.data?.bonusBox).toDomain()
        assertEquals(9, box.id)
        assertEquals(25_000L, box.amount.toLong())
        assertTrue(box.redeemed)
    }
}
