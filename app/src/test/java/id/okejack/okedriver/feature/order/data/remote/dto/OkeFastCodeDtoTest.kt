package id.okejack.okedriver.feature.order.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OkeFastCodeDtoTest {

    @Test
    fun response_isSuccessful_acceptsLegacySuccessOrBaseStatus() {
        assertTrue(OkeFastCodeResponseDto(success = true).isSuccessful)
        assertTrue(OkeFastCodeResponseDto(status = true).isSuccessful)
        assertFalse(OkeFastCodeResponseDto().isSuccessful)
    }

    @Test
    fun data_keepsGeneratedCode() {
        val response = OkeFastCodeResponseDto(
            success = true,
            data = OkeFastCodeDataDto(code = "123456")
        )

        assertEquals("123456", response.data?.code)
    }
}
