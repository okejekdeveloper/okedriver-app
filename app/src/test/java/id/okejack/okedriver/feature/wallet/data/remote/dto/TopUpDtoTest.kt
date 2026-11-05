package id.okejack.okedriver.feature.wallet.data.remote.dto

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class TopUpDtoTest {
    private val gson = Gson()

    @Test
    fun `QRIS request survives status responses without QR data`() {
        val created = gson.fromJson("""{"deposit_id":42,"payment_method":"qris","qr_code_url":"https://example.test/qr.png","qr_string":"payload","status_deposit":"Pending"}""", TopUpDepositDto::class.java).toDomain()
        assertTrue(created.isQris)
        assertFalse(created.isTerminal)
        val paid = TopUpDepositDto(depositId = 42, status = "Berhasil").toDomain().mergeQr(created)
        assertEquals(created.qrCodeUrl, paid.qrCodeUrl)
        assertTrue(paid.isPaid)
        assertTrue(paid.isTerminal)
        val otherDeposit = TopUpDepositDto(depositId = 43).toDomain().mergeQr(created)
        assertEquals("", otherDeposit.qrCodeUrl)
        for (status in listOf("Canceled", "Expired", "Kadaluarsa")) {
            assertTrue(TopUpDepositDto(status = status).toDomain().isTerminal)
        }
    }

    @Test
    fun `maps legacy top up response to domain`() {
        val response = gson.fromJson(
            """
            {
              "success": true,
              "data": {
                "topup": {
                  "deposit_id": 42,
                  "trans_id": "TRX-42",
                  "harga": "100000",
                  "payment": "Bank Transfer",
                  "payment_method": "5",
                  "status_deposit": "Pending",
                  "total": "101000"
                }
              }
            }
            """.trimIndent(),
            TopUpResponseDto::class.java
        )

        val deposit = requireNotNull(response.data?.topup).toDomain()
        assertEquals(42L, deposit.id)
        assertEquals(5, deposit.paymentMethod)
        assertEquals("101000", deposit.total)
    }

    @Test
    fun `maps payment methods and virtual account`() {
        val response = gson.fromJson(
            """
            {
              "status": true,
              "data": {
                "payment_methods": [{"type": 7, "name": "BCA Virtual Account"}],
                "vadata": {
                  "order_id": "TOP-1",
                  "va_number": "1234567890",
                  "bank": "BCA",
                  "payment_method": 7,
                  "account_name": "OKE DRIVER"
                }
              }
            }
            """.trimIndent(),
            TopUpResponseDto::class.java
        )

        assertEquals(7, response.data?.paymentMethods?.single()?.toDomain()?.id)
        assertEquals("1234567890", response.data?.vadata?.toDomain()?.number)
    }
}
